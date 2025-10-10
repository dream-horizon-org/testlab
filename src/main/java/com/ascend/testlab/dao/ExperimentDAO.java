package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Single;

public interface ExperimentDAO {
  Single<Long> create(String tenantId, CreateExperimentRequest request);
}
