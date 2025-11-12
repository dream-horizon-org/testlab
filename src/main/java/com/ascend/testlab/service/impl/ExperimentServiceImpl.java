package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.ExperimentUpdateLogDAO;
import com.ascend.testlab.dao.OwnerDAO;
import com.ascend.testlab.dao.TagDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
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
  private final TagDAO tagDAO;
  private final OwnerDAO ownerDAO;
  private final ExperimentUpdateLogDAO experimentUpdateLogDAO;

  /**
   * Constructs ExperimentServiceImpl with experiment DAO, PostgreSQL writer client, tag DAO, owner
   * DAO, and update log DAO.
   *
   * @param experimentDAO experiment data access object
   * @param pgWriterClient PostgreSQL writer client for transactional operations
   * @param tagDAO tag data access object
   * @param ownerDAO owner data access object
   * @param experimentUpdateLogDAO experiment update log DAO for logging updates
   */
  @Inject
  public ExperimentServiceImpl(
      ExperimentDAO experimentDAO,
      PgWriterClient pgWriterClient,
      TagDAO tagDAO,
      OwnerDAO ownerDAO,
      ExperimentUpdateLogDAO experimentUpdateLogDAO) {
    this.experimentDAO = experimentDAO;
    this.pgWriterClient = pgWriterClient;
    this.tagDAO = tagDAO;
    this.ownerDAO = ownerDAO;
    this.experimentUpdateLogDAO = experimentUpdateLogDAO;
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

      // Execute all insert operations in a transaction (delegated to DAO)
      return experimentDAO
          .createWithRelatedData(
              tenantId, projectKey, experimentId, request, request.getTags(), request.getOwner())
          .map(id -> new CreateExperimentResponse(experimentId, true, "created"))
          .toSingle()
          .onErrorReturn(
              error -> {
                log.error(
                    "Transaction failed during experiment creation, tenantId: {}, projectKey: {}, error: {}",
                    tenantId,
                    projectKey,
                    error.getMessage(),
                    error);
                return new CreateExperimentResponse(
                    experimentId, false, "Failed: " + error.getMessage());
              });
    } catch (Exception e) {
      log.error(
          "Exception in create experiment service for tenantId: {}, projectKey: {}, error: {}",
          tenantId,
          projectKey,
          e.getMessage(),
          e);
      return Single.just(new CreateExperimentResponse(null, false, "Failed: " + e.getMessage()));
    }
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
                experimentDAO
                    .updateWithTransaction(
                        projectKey,
                        experimentId,
                        context.experimentFields,
                        context.tags,
                        previousData,
                        context.updatedBy)
                    .toSingle());
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
  // ==================== Tag Management Methods ====================

  /**
   * Batch inserts tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return Single.just(true);
    }
    return tagDAO.batchInsertTags(connection, projectKey, experimentId, tags);
  }

  /**
   * Updates tags for an experiment by merging with existing tags.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param newTags list of new tags to set
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> updateTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> newTags) {
    if (newTags == null || newTags.isEmpty()) {
      return Single.just(true);
    }

    return tagDAO
        .getActiveTags(connection, projectKey, experimentId)
        .flatMap(
            existingTags -> {
              List<String> tagsToRemove =
                  existingTags.stream()
                      .filter(tag -> !newTags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              List<String> tagsToAdd =
                  newTags.stream()
                      .filter(tag -> !existingTags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              Single<Boolean> markInactive =
                  tagsToRemove.isEmpty()
                      ? Single.just(true)
                      : tagDAO.markTagsInactive(connection, projectKey, experimentId, tagsToRemove);

              Single<Boolean> insertNew =
                  tagsToAdd.isEmpty()
                      ? Single.just(true)
                      : tagDAO.batchInsertTags(connection, projectKey, experimentId, tagsToAdd);

              return markInactive.flatMap(
                  markSuccess -> {
                    if (!markSuccess) {
                      return Single.error(new RuntimeException("Failed to mark tags as inactive"));
                    }
                    return insertNew;
                  });
            });
  }

  // ==================== Owner Management Methods ====================

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param owner owner name/email
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertOwner(
      SqlConnection connection, UUID projectKey, UUID experimentId, String owner) {
    if (owner == null || owner.isEmpty()) {
      return Single.just(true);
    }
    return ownerDAO.insertOwner(connection, projectKey, experimentId, owner);
  }

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
