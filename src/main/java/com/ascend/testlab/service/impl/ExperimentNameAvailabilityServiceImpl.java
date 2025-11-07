package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentNameAvailabilityDAO;
import com.ascend.testlab.dto.response.ExperimentNameAvailabilityResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentNameAvailabilityService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the ExperimentNameAvailabilityService interface.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentNameAvailabilityServiceImpl implements ExperimentNameAvailabilityService {

  /** The experiment name availability DAO. */
  private final ExperimentNameAvailabilityDAO experimentNameAvailabilityDAO;

  /**
   * Constructor for the ExperimentNameAvailabilityServiceImpl.
   *
   * @param experimentNameAvailabilityDAO the experiment name availability DAO
   */
  @Inject
  public ExperimentNameAvailabilityServiceImpl(
      ExperimentNameAvailabilityDAO experimentNameAvailabilityDAO) {
    this.experimentNameAvailabilityDAO = experimentNameAvailabilityDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<ExperimentNameAvailabilityResponse> isExperimentNameAvailable(
      String projectKey, String experimentName) {
    return experimentNameAvailabilityDAO
        .isExperimentNameAvailable(projectKey, experimentName)
        .map(
            isAvailable -> {
              if (isAvailable) {
                return new ExperimentNameAvailabilityResponse(
                    true,
                    String.format(
                        "Experiment name '%s' is available in project '%s'",
                        experimentName, projectKey));
              } else {
                return new ExperimentNameAvailabilityResponse(
                    false,
                    String.format(
                        "Experiment name '%s' already exists in project '%s'",
                        experimentName, projectKey));
              }
            })
        .doOnSuccess(
            response ->
                log.info(
                    "Checked experiment name availability for projectKey={} and experimentName={}",
                    projectKey,
                    experimentName))
        .doOnError(
            error ->
                log.error(
                    "Error checking experiment name availability for projectKey={} and experimentName={}: {}",
                    projectKey,
                    experimentName,
                    error.getMessage(),
                    error))
        .onErrorResumeNext(
            error ->
                Single.error(
                    new RestException(
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getErrorCode(),
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getErrorMessage(),
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getHttpStatusCode(),
                        error)));
  }
}
