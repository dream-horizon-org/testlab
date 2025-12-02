package com.ascend.testlab.service.impl;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.DeleteExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.validator.UpdateExperimentValidator;
import com.ascend.testlab.util.CommonUtil;
import com.ascend.testlab.util.DbExceptionUtil;
import com.ascend.testlab.util.ExperimentMergeUtil;
import com.dream11.rest.exception.RestException;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentService. Handles business logic for experiment retrieval and
 * filtering operations, including error handling, pagination, and combining filters for tags and
 * owners.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentService
 * @see ExperimentDAO
 */
@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;
  private final AdminDAO adminDAO;

  /**
   * Constructor for ExperimentServiceImpl.
   *
   * @param experimentDAO the experiment DAO to use for data access
   * @param adminDAO the admin DAO to use for experiment key checks
   */
  @Inject
  public ExperimentServiceImpl(ExperimentDAO experimentDAO, AdminDAO adminDAO) {
    this.experimentDAO = experimentDAO;
    this.adminDAO = adminDAO;
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
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
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

  /** {@inheritDoc}* */
  @Override
  public Single<DeleteExperimentResponse> deleteExperiment(String projectKey, String experimentId) {
    return experimentDAO
        .getExperiment(projectKey, experimentId)
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(experiment -> experimentDAO.deleteExperiment(projectKey, experiment))
        .map(isDeleted -> new DeleteExperimentResponse(experimentId, isDeleted))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error in deleting experiment for project {}: {}", projectKey, experimentId);
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED, err)));
            });
  }

  /**
   * Creates a new experiment with tags and owners in a transaction.
   *
   * <p>Sets projectKey and experimentId from headers, creates experiment, tags, and owners in a
   * single transaction. If any operation fails, all changes are rolled back.
   *
   * @param projectKey project identifier from header
   * @param request experiment creation request with all experiment details, tags, and owner
   * @return Single emitting CreateExperimentResponse with id, status, and message
   */
  @Override
  public Single<CreateExperimentResponse> createExperiment(String projectKey, Experiment request) {
    log.info(
        "Creating experiment for projectKey: {}, experimentName: {}",
        projectKey,
        request.getName());

    generateExperimentId(request);
    return experimentDAO
        .createExperiment(projectKey, request)
        .map(status -> new CreateExperimentResponse(request.getExperimentId(), status))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to create experiment for projectKey: {}, experimentName: {}, error: {}",
                  projectKey,
                  request.getName(),
                  err.getMessage(),
                  err);

              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_CREATION_FAILED));
            });
  }

  private void generateExperimentId(Experiment request) {
    UUID experimentId = UUID.randomUUID();
    request.setExperimentId(experimentId);
    if (Objects.isNull(request.getExperimentKey())) {
      request.setExperimentKey(CommonUtil.generateExperimentKey(request.getName()));
    }
  }

  /**
   * Updates experiment fields partially with validation.
   *
   * <p>Flow: Get → Validate Key Uniqueness → Validate → Merge → Update in transaction
   *
   * <p>Validation rules enforced:
   *
   * <ul>
   *   <li>No changes in CONCLUDED/TERMINATED state
   *   <li>Cohort type (COHORT/STRATIFIED) cannot change
   *   <li>Stratified cohorts must exist in cohorts list
   *   <li>Existing variants: only value can change in LIVE/PAUSED
   *   <li>Variable keys cannot be added in LIVE/PAUSED
   *   <li>Conditions cannot be removed in LIVE/PAUSED
   *   <li>experiment_key, hypothesis, cohorts, startTime: DRAFT only
   *   <li>experiment_key must be unique within project
   *   <li>startTime/endTime must be in future
   * </ul>
   *
   * @param projectKey project identifier from header
   * @param experimentId experiment identifier
   * @param request validated update experiment request DTO
   * @return Single emitting UpdateExperimentResponse with status
   */
  @Override
  public Single<UpdateExperimentResponse> updateExperiment(
      String projectKey, UUID experimentId, UpdateExperimentRequest request) {

    log.info("Updating experiment for projectKey: {}, experimentId: {}", projectKey, experimentId);

    return experimentDAO
        .getExperiment(projectKey, experimentId.toString())
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(
            existing ->
                validateExperimentKeyUniqueness(projectKey, existing, request)
                    .flatMap(
                        isValid -> {
                          UpdateExperimentValidator.validate(existing, request);
                          Experiment updated = mergeUpdate(existing, request);
                          return experimentDAO.updateExperiment(projectKey, existing, updated);
                        }))
        .map(success -> new UpdateExperimentResponse(experimentId, success))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to update experiment for projectKey: {}, experimentId: {}, error: {}",
                  projectKey,
                  experimentId,
                  err.getMessage(),
                  err);
              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_UPDATE_FAILED));
            });
  }

  /**
   * Validates that the experiment_key is unique within the project.
   *
   * <p>Only checks if the request contains an experiment_key that is different from the existing
   * one.
   *
   * @param projectKey project identifier
   * @param existing the existing experiment
   * @param request the update request
   * @return Single emitting true if valid, or error if key already exists
   */
  private Single<Boolean> validateExperimentKeyUniqueness(
      String projectKey, Experiment existing, UpdateExperimentRequest request) {

    String newKey = request.getExperimentKey();

    // No experiment_key in request or same as existing - no validation needed
    if (newKey == null || newKey.equals(existing.getExperimentKey())) {
      return Single.just(true);
    }

    return adminDAO
        .isExperimentKeyAvailable(projectKey, newKey)
        .flatMap(
            isAvailable -> {
              if (!isAvailable) {
                return Single.error(new RestException(ErrorEnum.EXPERIMENT_KEY_ALREADY_EXISTS));
              }
              return Single.just(true);
            });
  }

  /**
   * Merges the update request into the existing experiment.
   *
   * <p>Only non-null fields from request are applied. Tags, owners, and metrics are replaced
   * entirely (not merged).
   *
   * @param existing the existing experiment
   * @param request the update request
   * @return merged experiment
   */
  private Experiment mergeUpdate(Experiment existing, UpdateExperimentRequest request) {
    return Experiment.builder()
        .experimentId(existing.getExperimentId())
        .projectKey(existing.getProjectKey())
        .name(getOrDefault(request.getName(), existing.getName()))
        .experimentKey(getOrDefault(request.getExperimentKey(), existing.getExperimentKey()))
        .description(getOrDefault(request.getDescription(), existing.getDescription()))
        .hypothesis(getOrDefault(request.getHypothesis(), existing.getHypothesis()))
        .status(getOrDefault(ExperimentStatus.fromValue(request.getStatus()), existing.getStatus()))
        .type(existing.getType())
        .guardrailHealthStatus(
            getOrDefault(request.getGuardrailHealthStatus(), existing.getGuardrailHealthStatus()))
        .cohorts(getOrDefault(request.getCohorts(), existing.getCohorts()))
        .variantWeights(
            ExperimentMergeUtil.mergeVariantWeights(
                existing.getVariantWeights(), request.getVariantWeights()))
        .variants(ExperimentMergeUtil.mergeVariants(existing.getVariants(), request.getVariants()))
        .ruleAttributes(
            ExperimentMergeUtil.mergeRuleAttributes(
                existing.getRuleAttributes(), request.getRuleAttributes()))
        .distributionStrategy(existing.getDistributionStrategy())
        .assignmentDomain(existing.getAssignmentDomain())
        .overrides(getOrDefault(request.getOverrides(), existing.getOverrides()))
        .winningVariant(getOrDefault(request.getWinningVariant(), existing.getWinningVariant()))
        .exposure(getOrDefault(request.getExposure(), existing.getExposure()))
        .threshold(getOrDefault(request.getThreshold(), existing.getThreshold()))
        .startTime(getOrDefault(request.getStartTime(), existing.getStartTime()))
        .endTime(getOrDefault(request.getEndTime(), existing.getEndTime()))
        .createdBy(existing.getCreatedBy())
        .updatedBy(getOrDefault(request.getUpdatedBy(), Constants.SYSTEM))
        .createdAt(existing.getCreatedAt())
        .tags(getOrDefault(request.getTags(), existing.getTags()))
        .owners(getOrDefault(request.getOwner(), existing.getOwners()))
        .metrics(getOrDefault(request.getMetrics(), existing.getMetrics()))
        .build();
  }

  private <T> T getOrDefault(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }
}
