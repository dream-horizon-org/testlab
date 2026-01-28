package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.DeleteExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AllocationService;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.validator.UpdateExperimentValidator;
import com.ascend.testlab.util.CommonUtil;
import com.ascend.testlab.util.DbExceptionUtil;
import com.ascend.testlab.util.ExperimentMergeUtil;
import com.dream11.rest.exception.RestException;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentService for experiment CRUD operations.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;
  private final AdminDAO adminDAO;
  private final AllocationDAO allocationDAO;
  private final AllocationService allocationService;

  /**
   * Constructs ExperimentServiceImpl with required dependencies.
   *
   * @param experimentDAO the experiment data access object
   * @param adminDAO the admin data access object
   * @param allocationDAO the allocation data access object
   * @param allocationService the allocation service for applying overrides
   */
  @Inject
  public ExperimentServiceImpl(
      ExperimentDAO experimentDAO,
      AdminDAO adminDAO,
      AllocationDAO allocationDAO,
      AllocationService allocationService) {
    this.experimentDAO = experimentDAO;
    this.adminDAO = adminDAO;
    this.allocationDAO = allocationDAO;
    this.allocationService = allocationService;
  }

  @Override
  public Single<Experiment> getExperiment(String projectKey, String experimentId) {
    return experimentDAO
        .getExperiment(projectKey, experimentId)
        .switchIfEmpty(Single.error(new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(experiment -> enrichWithVariantCounts(projectKey, experiment))
        .onErrorResumeNext(err -> handleError(err, ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED));
  }

  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest request) {
    return experimentDAO
        .filterExperiments(projectKey, request)
        .onErrorResumeNext(err -> handleError(err, ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED));
  }

  @Override
  public Single<DeleteExperimentResponse> deleteExperiment(String projectKey, String experimentId) {
    return experimentDAO
        .getExperiment(projectKey, experimentId)
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(experiment -> experimentDAO.deleteExperiment(projectKey, experiment))
        .map(deleted -> new DeleteExperimentResponse(experimentId, deleted))
        .onErrorResumeNext(err -> handleError(err, ErrorEnum.REST_DELETE_EXPERIMENT_FAILED));
  }

  @Override
  public Single<CreateExperimentResponse> createExperiment(
      String projectKey, CreateExperimentRequest request) {
    log.info("Creating experiment: projectKey={}, name={}", projectKey, request.getName());

    Experiment experiment = Experiment.fromRequest(request);
    initializeExperiment(experiment);

    Overrides overrides = request.getOverrides();

    return experimentDAO
        .createExperiment(projectKey, experiment)
        .flatMap(
            status -> {
              // Apply overrides if present
              if (Objects.nonNull(overrides) && !overrides.isEmpty()) {
                log.info(
                    "Applying overrides for newly created experiment {}",
                    experiment.getExperimentId());
                return allocationService
                    .applyOverrides(projectKey, experiment, overrides)
                    .map(
                        appliedOverrides -> {
                          log.info(
                              "Applied {} overrides for experiment {}",
                              appliedOverrides.size(),
                              experiment.getExperimentId());
                          return new CreateExperimentResponse(experiment.getExperimentId(), status);
                        })
                    .onErrorResumeNext(
                        overrideErr -> {
                          log.warn(
                              "Failed to apply overrides for experiment {}, but experiment was created: {}",
                              experiment.getExperimentId(),
                              overrideErr.getMessage());
                          // Return success since experiment was created, even if overrides failed
                          return Single.just(
                              new CreateExperimentResponse(experiment.getExperimentId(), status));
                        });
              }
              return Single.just(
                  new CreateExperimentResponse(experiment.getExperimentId(), status));
            })
        .onErrorResumeNext(
            err -> {
              log.error("Failed to create experiment: {}", err.getMessage(), err);
              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_CREATION_FAILED));
            });
  }

  @Override
  public Single<UpdateExperimentResponse> updateExperiment(
      String projectKey, UUID experimentId, UpdateExperimentRequest request) {

    log.info("Updating experiment: projectKey={}, experimentId={}", projectKey, experimentId);

    Overrides overrides = request.getOverrides();

    return experimentDAO
        .getExperiment(projectKey, experimentId.toString())
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(existing -> validateAndUpdate(projectKey, existing, request))
        .flatMap(
            updateResult -> {
              Boolean success = updateResult.getKey();
              Experiment mergedExperiment = updateResult.getValue();

              // Apply overrides if present
              if (Objects.nonNull(overrides) && !overrides.isEmpty()) {
                log.info("Applying overrides for updated experiment {}", experimentId);
                return allocationService
                    .applyOverrides(projectKey, mergedExperiment, overrides)
                    .map(
                        appliedOverrides -> {
                          log.info(
                              "Applied {} overrides for experiment {}",
                              appliedOverrides.size(),
                              experimentId);
                          return new UpdateExperimentResponse(experimentId, success);
                        })
                    .onErrorResumeNext(
                        overrideErr -> {
                          log.warn(
                              "Failed to apply overrides for experiment {}, but experiment was updated: {}",
                              experimentId,
                              overrideErr.getMessage());
                          // Return success since experiment was updated, even if overrides failed
                          return Single.just(new UpdateExperimentResponse(experimentId, success));
                        });
              }
              return Single.just(new UpdateExperimentResponse(experimentId, success));
            })
        .onErrorResumeNext(
            err -> {
              log.error("Failed to update experiment: {}", err.getMessage(), err);
              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_UPDATE_FAILED));
            });
  }

  private Single<Map.Entry<Boolean, Experiment>> validateAndUpdate(
      String projectKey, Experiment existing, UpdateExperimentRequest request) {
    return UpdateExperimentValidator.validate(projectKey, existing, request, adminDAO)
        .flatMap(
            valid -> {
              Experiment merged = ExperimentMergeUtil.merge(existing, request);
              return experimentDAO
                  .updateExperiment(projectKey, existing, merged)
                  .map(success -> Map.entry(success, merged));
            });
  }

  private Single<Experiment> enrichWithVariantCounts(String projectKey, Experiment experiment) {
    return adminDAO
        .getVariantCount(projectKey, experiment)
        .map(
            counts -> {
              experiment.setVariantCounts(counts.isEmpty() ? null : counts);
              return experiment;
            });
  }

  private void initializeExperiment(Experiment request) {
    request.setExperimentId(UUID.randomUUID());
    if (Objects.isNull(request.getExperimentKey())) {
      request.setExperimentKey(CommonUtil.generateExperimentKey(request.getName()));
    }
  }

  private <T> Single<T> handleError(Throwable err, ErrorEnum defaultError) {
    log.error("Operation failed: {}", err.getMessage());
    return Single.error(ErrorEnum.handleException(err, new RestException(defaultError, err)));
  }
}
