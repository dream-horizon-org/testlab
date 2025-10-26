package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentNameValidationDAO;
import com.ascend.testlab.dto.response.ValidateExperimentNameResponse;
import com.ascend.testlab.service.ExperimentNameValidationService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ExperimentNameValidationServiceImpl implements ExperimentNameValidationService {

  private final ExperimentNameValidationDAO experimentNameValidationDAO;

  @Override
  public Single<ValidateExperimentNameResponse> validateExperimentName(
      UUID projectId, String name) {
    return experimentNameValidationDAO
        .isExperimentNameExists(projectId, name)
        .map(
            exists -> {
              if (exists) {
                return new ValidateExperimentNameResponse(
                    false,
                    String.format("Experiment name '%s' already exists in this project", name));
              } else {
                return new ValidateExperimentNameResponse(
                    true, String.format("Experiment name '%s' is available", name));
              }
            })
        .doOnSuccess(
            res ->
                log.info(
                    "Got experiment name availability for project-id: {} and name: {}",
                    projectId,
                    name))
        .doOnError(
            error ->
                log.error(
                    "Error in getting experiment name availability for project-id={} and name={} due to: ",
                    projectId,
                    name,
                    error));
  }
}
