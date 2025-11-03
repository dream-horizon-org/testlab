package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import java.util.UUID;

public interface ExperimentDAO {
  Single<Long> create(UUID tenantId, UUID projectKey, CreateExperimentRequest request);

  Single<Boolean> updatePartial(UUID projectKey, UUID experimentId, Map<String, Object> request);
}
