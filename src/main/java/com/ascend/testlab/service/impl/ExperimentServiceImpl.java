package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
      UUID tenantId, String projectKey, CreateExperimentRequest request) {
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
        .createWithRelatedData(tenantId, request)
        .map(id -> new CreateExperimentResponse(experimentId, true, "created"))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to create experiment for tenantId: {}, projectKey: {}, experimentName: {}, error: {}",
                  tenantId,
                  projectKey,
                  request.getName(),
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.EXPERIMENT_CREATION_FAILED, err)));
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
   * @param request validated update experiment request DTO
   * @return Single emitting UpdateExperimentResponse with status and message
   */
  @Override
  public Single<UpdateExperimentResponse> update(
      UUID tenantId, String projectKey, UUID experimentId, UpdateExperimentRequest request) {
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
            response ->
                log.info(
                    "Update experiment completed - tenantId: {}, projectKey: {}, experimentId: {}, status: {}",
                    tenantId,
                    projectKey,
                    experimentId,
                    response.isStatus()));
  }

  /**
   * Extracts update context from DTO, separating tags, owners, metrics and metadata.
   *
   * @param request validated update experiment request DTO
   * @param experimentId experiment identifier for logging
   * @return UpdateContext containing the request, tags, owners, metrics, and updatedBy
   */
  private UpdateContext extractUpdateContext(UpdateExperimentRequest request, UUID experimentId) {
    List<String> tags = request.getTags();
    if (tags != null) {
      log.debug("Tags found in update request for experimentId: {}, tags: {}", experimentId, tags);
    }

    List<String> owners = request.getOwner();
    if (owners != null) {
      log.debug(
          "Owners found in update request for experimentId: {}, owners: {}", experimentId, owners);
    }

    Map<String, List<String>> metrics = request.getMetrics();
    if (metrics != null) {
      log.debug(
          "Metrics found in update request for experimentId: {}, metrics: {}",
          experimentId,
          metrics);
    }

    String updatedBy = request.getUpdatedBy();
    if (updatedBy != null) {
      log.debug("updated_by found in update request: {}", updatedBy);
    }

    return new UpdateContext(
        request, tags, owners, metrics, updatedBy != null ? updatedBy : "system");
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
      UUID tenantId, String projectKey, UUID experimentId, UpdateContext context) {

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
              // Check if experiment is in a terminal state (CONCLUDED or TERMINATED)
              Object currentStatusObj = previousData.get("status");
              String currentStatus = currentStatusObj != null ? currentStatusObj.toString() : null;

              if (currentStatus != null) {
                ExperimentStatus status = ExperimentStatus.valueOf(currentStatus);
                if (status == ExperimentStatus.CONCLUDED || status == ExperimentStatus.TERMINATED) {
                  String errorMsg =
                      String.format(
                          "Cannot update experiment in %s state. This is a terminal state.",
                          status);
                  log.error(
                      "Update blocked for experimentId: {}, reason: {}", experimentId, errorMsg);
                  return Single.error(
                      new RestException(
                          "INVALID_REQUEST",
                          "Cannot update experiment: " + errorMsg,
                          org.apache.http.HttpStatus.SC_BAD_REQUEST,
                          null));
                }
              }

              // Validate status transition if status is being updated
              if (context.request.getStatus() != null) {
                String newStatus = context.request.getStatus().name();

                log.debug(
                    "Status transition check - current: {}, new: {}, experimentId: {}",
                    currentStatus,
                    newStatus,
                    experimentId);

                try {
                  validateStatusTransition(currentStatus, newStatus, experimentId);
                } catch (IllegalArgumentException e) {
                  log.error("Status transition validation failed: {}", e.getMessage());
                  return Single.error(
                      new RestException(
                          "INVALID_REQUEST",
                          "Invalid status transition: " + e.getMessage(),
                          org.apache.http.HttpStatus.SC_BAD_REQUEST,
                          e));
                }
              }

              return experimentDAO.updateWithTransaction(
                  projectKey,
                  experimentId,
                  context.request,
                  context.tags,
                  context.owners,
                  context.metrics,
                  previousData,
                  context.updatedBy);
            })
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to update experiment for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
                  tenantId,
                  projectKey,
                  experimentId,
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.EXPERIMENT_UPDATE_FAILED, err)));
            });
  }

  /**
   * Logs update error.
   *
   * @param tenantId tenant identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param error error that occurred
   */
  private void logError(UUID tenantId, String projectKey, UUID experimentId, Throwable error) {
    log.error(
        "Failed to update experiment for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
        tenantId,
        projectKey,
        experimentId,
        error.getMessage(),
        error);
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
    return name.replaceAll("[ -]", "_").toLowerCase();
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

  /** Context holder for update operation containing request, tags, and metadata. */
  private static class UpdateContext {
    final UpdateExperimentRequest request;
    final List<String> tags;
    final List<String> owners;
    final Map<String, List<String>> metrics;
    final String updatedBy;

    UpdateContext(
        UpdateExperimentRequest request,
        List<String> tags,
        List<String> owners,
        Map<String, List<String>> metrics,
        String updatedBy) {
      this.request = request;
      this.tags = tags;
      this.owners = owners;
      this.metrics = metrics;
      this.updatedBy = updatedBy;
    }

    boolean hasUpdates() {
      // Check if request has any non-null fields (excluding tags and updatedBy)
      return request.getName() != null
          || request.getDescription() != null
          || request.getHypothesis() != null
          || request.getStatus() != null
          || request.getType() != null
          || request.getGuardrailHealthStatus() != null
          || request.getCohorts() != null
          || request.getVariantWeights() != null
          || request.getVariants() != null
          || request.getDistributionStrategy() != null
          || request.getAssignmentDomain() != null
          || request.getOverrides() != null
          || request.getRuleAttributes() != null
          || request.getWinningVariant() != null
          || request.getExposure() != null
          || request.getThreshold() != null
          || request.getStartTime() != null
          || request.getEndTime() != null
          || tags != null;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to the DAO layer to fetch the experiment. Handles error translation:
   *
   * <ul>
   *   <li>EXPERIMENT_NOT_FOUND: If no experiment is found with the given project Key and experiment
   *       ID.
   *   <li>REST_GET_EXPERIMENT_BY_ID_FAILED: For any other errors encountered during retrieval.
   * </ul>
   */
  @Override
  public Single<Experiment> getExperiment(String projectKey, String experimentId) {
    return experimentDAO
        .getExperiment(projectKey, experimentId)
        .switchIfEmpty(Single.error(new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error in get Experiments for project {} and experimentID {} : {}",
                  projectKey,
                  experimentId,
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED, err)));
            });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates filtering to the DAO layer. The DAO handles all filter combinations including
   * status, type, name, tags, and owners. Multiple filter values can be provided as comma-separated
   * strings for status, type, tag, and owner parameters.
   *
   * <p>Pagination is applied in the SQL query using LIMIT and OFFSET clauses, which is more
   * efficient than in-memory pagination. The total count is obtained using a window function in the
   * same query, eliminating the need for a separate COUNT query. Defaults to limit=20 and page=1 if
   * not specified.
   */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest request) {
    return experimentDAO
        .filterExperiments(projectKey, request)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error in filter Experiments for project {}: {}", projectKey, err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, err)));
            });
  }
}
