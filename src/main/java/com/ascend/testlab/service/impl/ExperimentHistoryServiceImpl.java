package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentHistoryDAO;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentHistoryService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ExperimentHistoryServiceImpl implements ExperimentHistoryService {

  private final ExperimentHistoryDAO experimentHistoryDAO;

  @Override
  public Single<GetExperimentHistoryResponse> getExperimentHistory(
      UUID projectId, UUID experimentId) {
    return experimentHistoryDAO
        .fetchExperimentHistory(projectId, experimentId)
        .map(
            historyEntries -> {
              return GetExperimentHistoryResponse.builder()
                  .experimentId(experimentId.toString())
                  .history(historyEntries)
                  .totalCount(historyEntries.size())
                  .build();
            })
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error getting experiment history for project {} and experiment {}: {}",
                  projectId,
                  experimentId,
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_GET_EXPERIMENT_HISTORY_FAILED, err)));
            });
  }
}
