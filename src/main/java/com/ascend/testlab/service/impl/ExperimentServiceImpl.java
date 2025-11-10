package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentAnalysisDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.ExperimentUpdateLogDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.OwnerService;
import com.ascend.testlab.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentService for experiment business logic.
 *
 * <p>This class handles experiment creation, updates, and assignments with proper validation, error
 * handling, and logging. Manages transactional operations for experiment, tags, and owners.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;
  private final PgWriterClient pgWriterClient;
  private final TagService tagService;
  private final OwnerService ownerService;
  private final ExperimentUpdateLogDAO experimentUpdateLogDAO;
  private final ExperimentAnalysisDAO experimentAnalysisDAO;
  private final ObjectMapper objectMapper;

  /**
   * Constructs ExperimentServiceImpl with experiment DAO, PostgreSQL writer client, tag service,
   * owner service, update log DAO, and analysis DAO.
   *
   * @param experimentDAO experiment data access object
   * @param pgWriterClient PostgreSQL writer client for transactional operations
   * @param tagService tag service for tag operations
   * @param ownerService owner service for owner operations
   * @param experimentUpdateLogDAO experiment update log DAO
   * @param experimentAnalysisDAO experiment analysis DAO
   */
  @Inject
  public ExperimentServiceImpl(
      ExperimentDAO experimentDAO,
      PgWriterClient pgWriterClient,
      TagService tagService,
      OwnerService ownerService,
      ExperimentUpdateLogDAO experimentUpdateLogDAO,
      ExperimentAnalysisDAO experimentAnalysisDAO) {
    this.experimentDAO = experimentDAO;
    this.pgWriterClient = pgWriterClient;
    this.tagService = tagService;
    this.ownerService = ownerService;
    this.experimentUpdateLogDAO = experimentUpdateLogDAO;
    this.experimentAnalysisDAO = experimentAnalysisDAO;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Creates a new experiment with tags and owners in a transaction.
   *
   * <p>Sets projectKey and experimentId from headers, creates experiment, tags, and owners in a
   * single transaction. If any operation fails, all changes are rolled back.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param request experiment creation request with all experiment details, tags, and owner
   * @return Single emitting CreateExperimentResponse with id, status, and message
   */
  @Override
  public Single<CreateExperimentResponse> create(
      UUID tenantId, UUID projectKey, CreateExperimentRequest request) {
    log.info(
        "Creating experiment for tenantId: {}, projectKey: {}, experimentName: {}, tags: {}, owner: {}",
        tenantId,
        projectKey,
        request != null ? request.getName() : "null",
        request != null ? request.getTags() : null,
        request != null ? request.getOwner() : null);

    try {
      // Set project_key and experiment_id - both come from header
      UUID experimentId = UUID.randomUUID();
      request.setProjectKey(projectKey);
      request.setExperimentId(experimentId);

      log.debug(
          "Using tenantId: {}, projectKey: {} from header, generated experimentId: {} for experiment: {}",
          tenantId,
          projectKey,
          experimentId,
          request.getName());

      // 1. Create partitions BEFORE transaction (DDL should be outside transaction)
      return createPartitionsIfNotExist(projectKey)
          .flatMap(
              partitionsCreated -> {
                if (!partitionsCreated) {
                  log.error("Failed to create partitions for projectKey: {}", projectKey);
                  return Single.just(
                      new CreateExperimentResponse(
                          0L, false, "Failed to create partitions for project"));
                }

                log.info(
                    "Partitions verified/created for projectKey: {}, proceeding with transaction",
                    projectKey);

                // 2. Execute all insert operations in a transaction
                return pgWriterClient
                    .executeWithTransaction(
                        connection -> {
                          // Create experiment
                          return experimentDAO
                              .create(tenantId, projectKey, request)
                              .flatMapMaybe(
                                  id -> {
                                    if (id <= 0) {
                                      log.error("Failed to create experiment - id is 0");
                                      return Maybe.error(
                                          new RuntimeException("Failed to insert experiment"));
                                    }

                                    log.info(
                                        "Experiment created with id: {}, now inserting tags and owner",
                                        id);

                                    // 3. Insert tags using TagService
                                    final Single<Boolean> tagsInsert =
                                        tagService.insertTags(
                                            connection,
                                            projectKey,
                                            experimentId,
                                            request.getTags());

                                    // 4. Insert owner using OwnerService
                                    final Single<Boolean> ownerInsert =
                                        ownerService.insertOwner(
                                            connection,
                                            projectKey,
                                            experimentId,
                                            request.getOwner());

                                    // 5. Insert update log (for create operation, previous_data is
                                    // null)
                                    final Single<Boolean> updateLogInsert =
                                        experimentUpdateLogDAO.insertUpdateLog(
                                            connection,
                                            projectKey,
                                            experimentId,
                                            null, // previous_data is null for create
                                            convertRequestToMap(request),
                                            request.getCreatedBy());

                                    // 6. Insert analysis entry (with default/null values)
                                    final Single<Boolean> analysisInsert =
                                        experimentAnalysisDAO.insertAnalysis(
                                            connection, projectKey, experimentId);

                                    // Execute all inserts sequentially
                                    return tagsInsert
                                        .flatMap(
                                            tagsSuccess -> {
                                              if (!tagsSuccess) {
                                                return Single.error(
                                                    new RuntimeException("Failed to insert tags"));
                                              }
                                              return ownerInsert;
                                            })
                                        .flatMap(
                                            ownerSuccess -> {
                                              if (!ownerSuccess) {
                                                return Single.error(
                                                    new RuntimeException("Failed to insert owner"));
                                              }
                                              return updateLogInsert;
                                            })
                                        .flatMap(
                                            updateLogSuccess -> {
                                              if (!updateLogSuccess) {
                                                return Single.error(
                                                    new RuntimeException(
                                                        "Failed to insert update log"));
                                              }
                                              return analysisInsert;
                                            })
                                        .flatMapMaybe(
                                            analysisSuccess -> {
                                              if (!analysisSuccess) {
                                                return Maybe.error(
                                                    new RuntimeException(
                                                        "Failed to insert analysis"));
                                              }
                                              log.info(
                                                  "Successfully created experiment with id: {}, experimentId: {}, projectKey: {}, tags: {}, owner: {}, update_log: true, analysis: true",
                                                  id,
                                                  experimentId,
                                                  projectKey,
                                                  request.getTags() != null
                                                      ? request.getTags().size()
                                                      : 0,
                                                  request.getOwner());
                                              return Maybe.just(id);
                                            });
                                  });
                        })
                    .map(id -> new CreateExperimentResponse(id, true, "created"))
                    .toSingle();
              })
          .onErrorReturn(
              error -> {
                log.error(
                    "Transaction failed during experiment creation, tenantId: {}, projectKey: {}, error: {}",
                    tenantId,
                    projectKey,
                    error.getMessage(),
                    error);
                return new CreateExperimentResponse(0L, false, "Failed: " + error.getMessage());
              });
    } catch (Exception e) {
      log.error(
          "Exception in create experiment service for tenantId: {}, projectKey: {}, error: {}",
          tenantId,
          projectKey,
          e.getMessage(),
          e);
      return Single.just(new CreateExperimentResponse(0L, false, "Failed: " + e.getMessage()));
    }
  }

  /**
   * Converts CreateExperimentRequest to Map for storing in update log.
   *
   * @param request experiment creation request
   * @return Map representation of the request
   */
  private Map<String, Object> convertRequestToMap(CreateExperimentRequest request) {
    try {
      String json = objectMapper.writeValueAsString(request);
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      log.error("Failed to convert request to map: {}", e.getMessage(), e);
      return new HashMap<>();
    }
  }

  /**
   * Creates partitions for experiments, tags, and owners tables if they don't exist.
   *
   * <p>Dynamically creates partitions for the given project_key to avoid "no partition found"
   * errors. Uses CREATE TABLE IF NOT EXISTS to safely handle concurrent creation attempts. This
   * method executes OUTSIDE of the main transaction since DDL statements should not be in
   * transactions.
   *
   * @param projectKey project identifier for partition
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> createPartitionsIfNotExist(UUID projectKey) {
    // Sanitize project_key for table name (replace hyphens with underscores)
    String sanitizedKey = projectKey.toString().replace("-", "_");

    log.info("Creating partitions for projectKey: {} (sanitized: {})", projectKey, sanitizedKey);

    // Build partition creation queries
    String createExperimentsPartition =
        String.format(WriteQuery.CREATE_EXPERIMENTS_PARTITION, sanitizedKey, projectKey);
    String createTagsPartition =
        String.format(WriteQuery.CREATE_TAGS_PARTITION, sanitizedKey, projectKey);
    String createOwnersPartition =
        String.format(WriteQuery.CREATE_OWNERS_PARTITION, sanitizedKey, projectKey);
    String createUpdateLogPartition =
        String.format(WriteQuery.CREATE_UPDATE_LOG_PARTITION, sanitizedKey, projectKey);
    String createAnalysisPartition =
        String.format(WriteQuery.CREATE_ANALYSIS_PARTITION, sanitizedKey, projectKey);

    // Execute partition creation queries sequentially (without transaction)
    return pgWriterClient
        .execute(createExperimentsPartition)
        .doOnSuccess(
            success ->
                log.debug("Experiments partition created/exists for projectKey: {}", projectKey))
        .doOnError(
            error ->
                log.error(
                    "Failed to create experiments partition for projectKey: {}, error: {}",
                    projectKey,
                    error.getMessage()))
        .flatMap(
            success ->
                pgWriterClient
                    .execute(createTagsPartition)
                    .doOnSuccess(
                        s ->
                            log.debug(
                                "Tags partition created/exists for projectKey: {}", projectKey))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to create tags partition for projectKey: {}, error: {}",
                                projectKey,
                                error.getMessage())))
        .flatMap(
            success ->
                pgWriterClient
                    .execute(createOwnersPartition)
                    .doOnSuccess(
                        s ->
                            log.debug(
                                "Owners partition created/exists for projectKey: {}", projectKey))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to create owners partition for projectKey: {}, error: {}",
                                projectKey,
                                error.getMessage())))
        .flatMap(
            success ->
                pgWriterClient
                    .execute(createUpdateLogPartition)
                    .doOnSuccess(
                        s ->
                            log.debug(
                                "Update log partition created/exists for projectKey: {}",
                                projectKey))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to create update log partition for projectKey: {}, error: {}",
                                projectKey,
                                error.getMessage())))
        .flatMap(
            success ->
                pgWriterClient
                    .execute(createAnalysisPartition)
                    .doOnSuccess(
                        s ->
                            log.info(
                                "All partitions created/verified for projectKey: {}", projectKey))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to create analysis partition for projectKey: {}, error: {}",
                                projectKey,
                                error.getMessage())))
        .onErrorReturn(
            error -> {
              log.error(
                  "Error creating partitions for projectKey: {}, error: {}",
                  projectKey,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Updates experiment fields partially with validation.
   *
   * <p>Delegates to DAO for dynamic partial updates and returns success status.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> update(
      UUID tenantId, UUID projectKey, UUID experimentId, Map<String, Object> request) {
    log.info(
        "Updating experiment for tenantId: {}, projectKey: {}, experimentId: {}, fields: {}",
        tenantId,
        projectKey,
        experimentId,
        request != null ? request.keySet() : "null");

    try {
      return experimentDAO
          .updatePartial(projectKey, experimentId, request)
          .doOnSuccess(
              success ->
                  log.info(
                      "Successfully updated experiment, tenantId: {}, projectKey: {}, experimentId: {}, success: {}",
                      tenantId,
                      projectKey,
                      experimentId,
                      success))
          .doOnError(
              error ->
                  log.error(
                      "Failed to update experiment for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
                      tenantId,
                      projectKey,
                      experimentId,
                      error.getMessage(),
                      error))
          .onErrorReturn(
              error -> {
                log.error(
                    "Returning false for experiment update, tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
                    tenantId,
                    projectKey,
                    experimentId,
                    error.getMessage());
                return false;
              });
    } catch (Exception e) {
      log.error(
          "Exception in update experiment service for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
          tenantId,
          projectKey,
          experimentId,
          e.getMessage(),
          e);
      return Single.just(false);
    }
  }
}
