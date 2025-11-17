package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
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

  /**
   * Constructs ExperimentServiceImpl with experiment DAO and PostgreSQL writer client.
   *
   * @param experimentDAO experiment data access object
   * @param pgWriterClient PostgreSQL writer client for transactional operations
   */
  @Inject
  public ExperimentServiceImpl(ExperimentDAO experimentDAO, PgWriterClient pgWriterClient) {
    this.experimentDAO = experimentDAO;
    this.pgWriterClient = pgWriterClient;
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
        request.getName(),
        request != null ? request.getTags() : null,
        request != null ? request.getOwner() : null);

    // Set project_key and experiment_id - both come from header
    UUID experimentId = UUID.randomUUID();
    request.setProjectKey(projectKey);
    request.setExperimentId(experimentId);

    // Generate experiment_key from name: replace spaces and hyphens with underscores
    String experimentKey = generateExperimentKey(request.getName());
    request.setExperimentKey(experimentKey);

    log.debug(
        "Using tenantId: {}, projectKey: {} from header, generated experimentId: {}, experimentKey: {} for experiment: {}",
        tenantId,
        projectKey,
        experimentId,
        experimentKey,
        request.getName());

    // Execute all insert operations in a transaction (delegated to DAO)
    return experimentDAO
        .createWithRelatedData(
            tenantId, projectKey, experimentId, request, request.getTags(), request.getOwner())
        .map(id -> new CreateExperimentResponse(experimentId, true, "created"));
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
   * @param request validated update experiment request DTO
   * @return Single emitting UpdateExperimentResponse with status and message
   */
  @Override
  public Single<UpdateExperimentResponse> update(
      UUID tenantId, UUID projectKey, UUID experimentId, UpdateExperimentRequest request) {
    log.info(
        "Updating experiment for tenantId: {}, projectKey: {}, experimentId: {}",
        tenantId,
        projectKey,
        experimentId);

    UpdateContext context = extractUpdateContext(request, experimentId);

    if (!context.hasUpdates()) {
      log.warn("No fields to update for experimentId: {}", experimentId);
      return Single.just(new UpdateExperimentResponse(experimentId, true, "No updates provided"));
    }

    return executeTransactionalUpdate(tenantId, projectKey, experimentId, context)
        .map(
            success ->
                new UpdateExperimentResponse(
                    experimentId, success, success ? "updated" : "Update failed"))
        .doOnSuccess(
            response -> logSuccess(tenantId, projectKey, experimentId, response.isStatus()));
  }

  /**
   * Extracts update context from DTO, separating experiment fields, tags, and metadata.
   *
   * @param request validated update experiment request DTO
   * @param experimentId experiment identifier for logging
   * @return UpdateContext containing separated fields
   */
  private UpdateContext extractUpdateContext(UpdateExperimentRequest request, UUID experimentId) {
    Map<String, Object> experimentFields = convertDtoToMap(request);

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
            previousData -> {
              // Validate status transition if status is being updated
              if (context.experimentFields.containsKey("status")) {
                Object currentStatusObj = previousData.get("status");
                Object newStatusObj = context.experimentFields.get("status");

                String currentStatus =
                    currentStatusObj != null ? currentStatusObj.toString() : null;
                String newStatus = newStatusObj != null ? newStatusObj.toString() : null;

                log.debug(
                    "Status transition check - current: {}, new: {}, experimentId: {}",
                    currentStatus,
                    newStatus,
                    experimentId);

                try {
                  validateStatusTransition(currentStatus, newStatus, experimentId);
                } catch (IllegalArgumentException e) {
                  log.error("Status transition validation failed: {}", e.getMessage());
                  return Single.error(e);
                }
              }

              return experimentDAO.updateWithTransaction(
                  projectKey,
                  experimentId,
                  context.experimentFields,
                  context.tags,
                  previousData,
                  context.updatedBy);
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
    return experimentDAO.batchInsertTags(connection, projectKey, experimentId, tags);
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

    return experimentDAO
        .getTags(connection, projectKey, experimentId)
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
                      : experimentDAO.deleteTags(
                          connection, projectKey, experimentId, tagsToRemove);

              Single<Boolean> insertNew =
                  tagsToAdd.isEmpty()
                      ? Single.just(true)
                      : experimentDAO.batchInsertTags(
                          connection, projectKey, experimentId, tagsToAdd);

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
    return experimentDAO.insertOwner(connection, projectKey, experimentId, owner);
  }

  /**
   * Generates experiment key from experiment name by replacing spaces and hyphens with underscores.
   *
   * @param name experiment name
   * @return experiment key with underscores
   */
  private String generateExperimentKey(String name) {
    if (name == null || name.isEmpty()) {
      return "";
    }
    return name.replaceAll("[ -]", "_");
  }

  /**
   * Validates status transition based on business rules.
   *
   * <p>Status transition rules:
   *
   * <ul>
   *   <li>DRAFT → LIVE, PAUSED, CONCLUDED, TERMINATED
   *   <li>LIVE → PAUSED, CONCLUDED, TERMINATED
   *   <li>PAUSED → LIVE, CONCLUDED, TERMINATED
   *   <li>CONCLUDED → No transitions allowed (terminal state)
   *   <li>TERMINATED → No transitions allowed (terminal state)
   * </ul>
   *
   * @param currentStatus current experiment status
   * @param newStatus new status to transition to
   * @param experimentId experiment identifier for logging
   * @throws IllegalArgumentException if transition is not allowed
   */
  private void validateStatusTransition(String currentStatus, String newStatus, UUID experimentId) {
    if (currentStatus == null || newStatus == null) {
      log.warn(
          "Status validation skipped - currentStatus: {}, newStatus: {}, experimentId: {}",
          currentStatus,
          newStatus,
          experimentId);
      return;
    }

    // If status is not changing, allow it
    if (currentStatus.equals(newStatus)) {
      log.debug(
          "Status not changing for experimentId: {}, status: {}", experimentId, currentStatus);
      return;
    }

    log.info(
        "Validating status transition for experimentId: {}, from: {} to: {}",
        experimentId,
        currentStatus,
        newStatus);

    ExperimentStatus current = ExperimentStatus.valueOf(currentStatus);
    ExperimentStatus target = ExperimentStatus.valueOf(newStatus);

    boolean isValidTransition = false;
    String errorMessage = null;

    switch (current) {
      case DRAFT:
        // DRAFT can transition to LIVE, PAUSED, CONCLUDED, TERMINATED
        isValidTransition =
            target == ExperimentStatus.LIVE
                || target == ExperimentStatus.PAUSED
                || target == ExperimentStatus.CONCLUDED
                || target == ExperimentStatus.TERMINATED;
        if (!isValidTransition) {
          errorMessage =
              String.format(
                  "Invalid status transition from DRAFT to %s. Allowed transitions: LIVE, PAUSED, CONCLUDED, TERMINATED",
                  target);
        }
        break;

      case LIVE:
        // LIVE can transition to PAUSED, CONCLUDED, TERMINATED
        isValidTransition =
            target == ExperimentStatus.PAUSED
                || target == ExperimentStatus.CONCLUDED
                || target == ExperimentStatus.TERMINATED;
        if (!isValidTransition) {
          errorMessage =
              String.format(
                  "Invalid status transition from LIVE to %s. Allowed transitions: PAUSED, CONCLUDED, TERMINATED",
                  target);
        }
        break;

      case PAUSED:
        // PAUSED can transition to LIVE, CONCLUDED, TERMINATED
        isValidTransition =
            target == ExperimentStatus.LIVE
                || target == ExperimentStatus.CONCLUDED
                || target == ExperimentStatus.TERMINATED;
        if (!isValidTransition) {
          errorMessage =
              String.format(
                  "Invalid status transition from PAUSED to %s. Allowed transitions: LIVE, CONCLUDED, TERMINATED",
                  target);
        }
        break;

      case CONCLUDED:
      case TERMINATED:
        // Terminal states - no transitions allowed
        errorMessage =
            String.format(
                "Cannot update status from %s. This is a terminal state and cannot be changed.",
                current);
        break;

      default:
        errorMessage = String.format("Unknown current status: %s", current);
        break;
    }

    if (!isValidTransition) {
      log.error(
          "Status transition validation failed for experimentId: {}, error: {}",
          experimentId,
          errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }

    log.info(
        "Status transition validated successfully for experimentId: {}, from: {} to: {}",
        experimentId,
        currentStatus,
        newStatus);
  }

  /**
   * Converts UpdateExperimentRequest DTO to Map for DAO layer.
   *
   * <p>Serializes the DTO to JSON string and then deserializes to Map to preserve @JsonProperty
   * annotations and remove null values.
   *
   * @param request update experiment request DTO
   * @return map of field names to values (excluding null values)
   */
  private Map<String, Object> convertDtoToMap(UpdateExperimentRequest request) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      // Serialize to JSON string to respect @JsonProperty annotations
      String jsonString = mapper.writeValueAsString(request);
      // Deserialize back to Map
      @SuppressWarnings("unchecked")
      Map<String, Object> map = mapper.readValue(jsonString, Map.class);

      // Remove null values
      map.values().removeIf(value -> value == null);

      return map;
    } catch (Exception e) {
      log.error("Failed to convert DTO to Map: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to convert DTO to Map", e);
    }
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
