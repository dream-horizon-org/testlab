package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public interface ExperimentHistoryService {
  Single<GetExperimentHistoryResponse> getExperimentHistory(UUID projectId, UUID experimentId);
}
