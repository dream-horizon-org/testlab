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
import java.util.List;
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
   * <p>Delegates to DAO for dynamic partial updates. If tags are included in the request, handles
   * them separately in a transaction (marks removed tags as inactive, inserts new tags). Also logs
   * the update in experiment_update_log table with previous and current data.
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
      UpdateContext context = extractUpdateContext(request, experimentId);

      if (!context.hasUpdates()) {
        log.warn("No fields to update for experimentId: {}", experimentId);
        return Single.just(true);
      }

      return executeTransactionalUpdate(tenantId, projectKey, experimentId, context)
          .doOnSuccess(success -> logSuccess(tenantId, projectKey, experimentId, success))
          .doOnError(error -> logError(tenantId, projectKey, experimentId, error))
          .onErrorReturn(error -> handleError(tenantId, projectKey, experimentId, error));

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

  /**
   * Extracts update context from request, separating experiment fields, tags, and metadata.
   *
   * @param request raw update request
   * @param experimentId experiment identifier for logging
   * @return UpdateContext containing separated fields
   */
  private UpdateContext extractUpdateContext(Map<String, Object> request, UUID experimentId) {
    Map<String, Object> experimentFields = new HashMap<>(request);

    List<String> tags = extractField(experimentFields, "tags", List.class);
    if (tags != null) {
      log.debug("Tags found in update request for experimentId: {}, tags: {}", experimentId, tags);
    }

    String updatedBy = extractField(experimentFields, "updated_by", String.class);
    if (updatedBy != null) {
      log.debug("updated_by found in update request: {}", updatedBy);
    }

    return new UpdateContext(experimentFields, tags, updatedBy != null ? updatedBy : "system");
  }

  /**
   * Extracts and removes a field from map with type safety.
   *
   * @param map source map
   * @param key field key
   * @param type expected type
   * @return extracted value or null
   */
  @SuppressWarnings("unchecked")
  private <T> T extractField(Map<String, Object> map, String key, Class<T> type) {
    Object value = map.remove(key);
    return type.isInstance(value) ? (T) value : null;
  }

  /**
   * Executes transactional update including experiment fields, tags, and update log.
   *
   * @param tenantId tenant identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param context update context with separated fields
   * @return Single emitting true on success
   */
  private Single<Boolean> executeTransactionalUpdate(
      UUID tenantId, UUID projectKey, UUID experimentId, UpdateContext context) {

    log.info(
        "Executing transactional update for experimentId: {}, projectKey: {}",
        experimentId,
        projectKey);

    return experimentDAO
        .getExperimentData(projectKey, experimentId)
        .doOnSuccess(
            previousData ->
                log.debug(
                    "Retrieved previous data for experimentId: {}, fields: {}",
                    experimentId,
                    previousData.keySet()))
        .flatMap(
            previousData ->
                executeUpdateTransaction(projectKey, experimentId, context, previousData)
                    .toSingle());
  }

  /**
   * Executes the actual update transaction with all operations.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param context update context
   * @param previousData experiment data before update
   * @return Maybe emitting true on success
   */
  private Maybe<Boolean> executeUpdateTransaction(
      UUID projectKey, UUID experimentId, UpdateContext context, Map<String, Object> previousData) {

    return pgWriterClient.executeWithTransaction(
        connection ->
            updateExperimentFields(projectKey, experimentId, context.experimentFields)
                .flatMap(success -> validateSuccess(success, "experiment fields"))
                .flatMap(
                    unused ->
                        updateExperimentTags(connection, projectKey, experimentId, context.tags))
                .flatMap(success -> validateSuccess(success, "tags"))
                .flatMapMaybe(
                    unused ->
                        getAndLogUpdate(
                            connection,
                            projectKey,
                            experimentId,
                            previousData,
                            context.updatedBy)));
  }

  /**
   * Updates experiment fields if present.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param fields fields to update
   * @return Single emitting true on success
   */
  private Single<Boolean> updateExperimentFields(
      UUID projectKey, UUID experimentId, Map<String, Object> fields) {
    return fields.isEmpty()
        ? Single.just(true)
        : experimentDAO.updatePartial(projectKey, experimentId, fields);
  }

  /**
   * Updates tags if present.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param tags tags to update
   * @return Single emitting true on success
   */
  private Single<Boolean> updateExperimentTags(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      List<String> tags) {
    return tags == null
        ? Single.just(true)
        : tagService.updateTags(connection, projectKey, experimentId, tags);
  }

  /**
   * Validates operation success and continues or errors.
   *
   * @param success operation result
   * @param operation operation name for error message
   * @return Single emitting true on success, error otherwise
   */
  private Single<Boolean> validateSuccess(Boolean success, String operation) {
    return success
        ? Single.just(true)
        : Single.error(new RuntimeException("Failed to update " + operation));
  }

  /**
   * Gets current data and logs the update.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param previousData data before update
   * @param updatedBy user who performed update
   * @return Maybe emitting true on success
   */
  private Maybe<Boolean> getAndLogUpdate(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      String updatedBy) {

    return experimentDAO
        .getExperimentData(projectKey, experimentId)
        .doOnSuccess(
            currentData ->
                log.debug(
                    "Retrieved current data for experimentId: {}, fields: {}",
                    experimentId,
                    currentData.keySet()))
        .flatMapMaybe(
            currentData ->
                insertUpdateLog(
                    connection, projectKey, experimentId, previousData, currentData, updatedBy));
  }

  /**
   * Inserts update log entry.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param previousData data before update
   * @param currentData data after update
   * @param updatedBy user who performed update
   * @return Maybe emitting true on success
   */
  private Maybe<Boolean> insertUpdateLog(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      Map<String, Object> currentData,
      String updatedBy) {

    return experimentUpdateLogDAO
        .insertUpdateLog(connection, projectKey, experimentId, previousData, currentData, updatedBy)
        .flatMapMaybe(
            success -> {
              if (!success) {
                return Maybe.error(new RuntimeException("Failed to insert update log"));
              }
              log.info(
                  "Successfully updated experiment, tags, and logged update for experimentId: {}, projectKey: {}",
                  experimentId,
                  projectKey);
              return Maybe.just(true);
            });
  }

  /**
   * Logs successful update.
   *
   * @param tenantId tenant identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param success operation result
   */
  private void logSuccess(UUID tenantId, UUID projectKey, UUID experimentId, Boolean success) {
    log.info(
        "Successfully updated experiment with update log, tenantId: {}, projectKey: {}, experimentId: {}, success: {}",
        tenantId,
        projectKey,
        experimentId,
        success);
  }

  /**
   * Logs update error.
   *
   * @param tenantId tenant identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param error error that occurred
   */
  private void logError(UUID tenantId, UUID projectKey, UUID experimentId, Throwable error) {
    log.error(
        "Failed to update experiment for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
        tenantId,
        projectKey,
        experimentId,
        error.getMessage(),
        error);
  }

  /**
   * Handles update error and returns false.
   *
   * @param tenantId tenant identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param error error that occurred
   * @return false to indicate failure
   */
  private Boolean handleError(UUID tenantId, UUID projectKey, UUID experimentId, Throwable error) {
    log.error(
        "Returning false for experiment update, tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
        tenantId,
        projectKey,
        experimentId,
        error.getMessage());
    return false;
  }

  /** Inner class to hold update context with separated fields. */
  private static class UpdateContext {
    final Map<String, Object> experimentFields;
    final List<String> tags;
    final String updatedBy;

    UpdateContext(Map<String, Object> experimentFields, List<String> tags, String updatedBy) {
      this.experimentFields = experimentFields;
      this.tags = tags;
      this.updatedBy = updatedBy;
    }

    boolean hasUpdates() {
      return !experimentFields.isEmpty() || tags != null;
    }
  }
}
