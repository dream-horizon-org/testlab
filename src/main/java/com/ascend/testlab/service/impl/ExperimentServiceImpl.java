package com.ascend.testlab.service.impl;

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

  @Inject
  public ExperimentServiceImpl(ExperimentDAO experimentDAO, AdminDAO adminDAO) {
    this.experimentDAO = experimentDAO;
    this.adminDAO = adminDAO;
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
  public Single<CreateExperimentResponse> createExperiment(String projectKey, Experiment request) {
    log.info("Creating experiment: projectKey={}, name={}", projectKey, request.getName());

    initializeExperiment(request);

    return experimentDAO
        .createExperiment(projectKey, request)
        .map(status -> new CreateExperimentResponse(request.getExperimentId(), status))
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

    return experimentDAO
        .getExperiment(projectKey, experimentId.toString())
        .switchIfEmpty(Single.error(ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .flatMap(existing -> validateAndUpdate(projectKey, existing, request))
        .map(success -> new UpdateExperimentResponse(experimentId, success))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to update experiment: {}", err.getMessage(), err);
              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_UPDATE_FAILED));
            });
  }

  private Single<Boolean> validateAndUpdate(
      String projectKey, Experiment existing, UpdateExperimentRequest request) {
    return UpdateExperimentValidator.validate(projectKey, existing, request, adminDAO)
        .flatMap(
            valid -> {
              Experiment merged = ExperimentMergeUtil.merge(existing, request);
              return experimentDAO.updateExperiment(projectKey, existing, merged);
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
