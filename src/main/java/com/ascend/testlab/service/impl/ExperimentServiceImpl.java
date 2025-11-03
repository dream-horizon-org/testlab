package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;

  @Inject
  public ExperimentServiceImpl(ExperimentDAO experimentDAO) {
    this.experimentDAO = experimentDAO;
  }

  @Override
  public Single<CreateExperimentResponse> create(
      UUID tenantId, UUID projectKey, CreateExperimentRequest request) {
    log.info(
        "Creating experiment for tenantId: {}, projectKey: {}, experimentName: {}",
        tenantId,
        projectKey,
        request != null ? request.getName() : "null");

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

      return experimentDAO
          .create(tenantId, projectKey, request)
          .flatMap(
              id -> {
                if (id > 0) {
                  log.info(
                      "Successfully created experiment with id: {}, experimentId: {}, projectKey: {}",
                      id,
                      experimentId,
                      projectKey);
                  return Single.just(new CreateExperimentResponse(id, true, "created"));
                } else {
                  log.error(
                      "Failed to create experiment - id is 0, tenantId: {}, projectKey: {}, experimentName: {}",
                      tenantId,
                      projectKey,
                      request.getName());
                  return Single.just(
                      new CreateExperimentResponse(0L, false, "Failed to insert experiment"));
                }
              })
          .onErrorReturn(
              error -> {
                log.error(
                    "Exception during experiment creation, tenantId: {}, projectKey: {}, error: {}",
                    tenantId,
                    projectKey,
                    error.getMessage(),
                    error);
                return new CreateExperimentResponse(0L, false, "Failed: " + error.getMessage());
              });
    } catch (Exception e) {
      log.error(
          "Exception in create experiment service for tenantId: {}, projectKey: {}, error: {}",
          tenantId,
          projectKey,
          e.getMessage(),
          e);
      return Single.just(new CreateExperimentResponse(0L, false, "Failed: " + e.getMessage()));
    }
  }

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
      return experimentDAO
          .updatePartial(projectKey, experimentId, request)
          .doOnSuccess(
              success ->
                  log.info(
                      "Successfully updated experiment, tenantId: {}, projectKey: {}, experimentId: {}, success: {}",
                      tenantId,
                      projectKey,
                      experimentId,
                      success))
          .doOnError(
              error ->
                  log.error(
                      "Failed to update experiment for tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
                      tenantId,
                      projectKey,
                      experimentId,
                      error.getMessage(),
                      error))
          .onErrorReturn(
              error -> {
                log.error(
                    "Returning false for experiment update, tenantId: {}, projectKey: {}, experimentId: {}, error: {}",
                    tenantId,
                    projectKey,
                    experimentId,
                    error.getMessage());
                return false;
              });
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
}
