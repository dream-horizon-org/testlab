package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentNameAvailabilityDAO;
import com.ascend.testlab.dto.response.ExperimentNameAvailabilityResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentNameAvailabilityService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentNameAvailabilityServiceImpl implements ExperimentNameAvailabilityService {

  private final ExperimentNameAvailabilityDAO experimentNameAvailabilityDAO;

  @Inject
  public ExperimentNameAvailabilityServiceImpl(
      ExperimentNameAvailabilityDAO experimentNameAvailabilityDAO) {
    this.experimentNameAvailabilityDAO = experimentNameAvailabilityDAO;
  }

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
            error -> {
              log.error(
                  "Error checking experiment name availability for projectKey={} and experimentName={}: {}",
                  projectKey,
                  experimentName,
                  error.getMessage(),
                  error);
              throw new RestException(ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED);
            });
  }
}
