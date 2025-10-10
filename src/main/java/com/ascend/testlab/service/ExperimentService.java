package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import io.reactivex.rxjava3.core.Single;

public interface ExperimentService {
  Single<CreateExperimentResponse> create(String tenantId, CreateExperimentRequest request);
}
