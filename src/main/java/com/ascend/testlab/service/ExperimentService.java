package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import java.util.UUID;

public interface ExperimentService {
  Single<CreateExperimentResponse> create(
      UUID tenantId, UUID projectKey, CreateExperimentRequest request);

  Single<Boolean> update(
      UUID tenantId, UUID projectKey, UUID experimentId, Map<String, Object> request);
}
