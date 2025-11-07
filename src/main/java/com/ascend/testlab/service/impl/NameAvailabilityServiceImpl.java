package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.NameAvailabilityDAO;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.NameAvailabilityService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the NameAvailabilityService interface.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class NameAvailabilityServiceImpl implements NameAvailabilityService {

  /** The name availability DAO. */
  private final NameAvailabilityDAO nameAvailabilityDAO;

  /**
   * Constructor for the NameAvailabilityServiceImpl.
   *
   * @param nameAvailabilityDAO the name availability DAO
   */
  @Inject
  public NameAvailabilityServiceImpl(NameAvailabilityDAO nameAvailabilityDAO) {
    this.nameAvailabilityDAO = nameAvailabilityDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<NameAvailabilityResponse> isExperimentNameAvailable(
      String projectKey, String experimentName) {
    return nameAvailabilityDAO
        .isExperimentNameAvailable(projectKey, experimentName)
        .map(
            isAvailable -> {
              if (isAvailable) {
                return new NameAvailabilityResponse(
                    true,
                    String.format(
                        "Experiment name '%s' is available in project '%s'",
                        experimentName, projectKey));
              } else {
                return new NameAvailabilityResponse(
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
