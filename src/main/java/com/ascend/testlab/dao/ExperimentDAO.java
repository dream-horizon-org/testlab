package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.Map;
import java.util.UUID;

public interface ExperimentDAO {
  Single<Long> create(UUID tenantId, CreateExperimentRequest request);

  Single<Boolean> updatePartial(
      SqlConnection connection, UUID tenantId, UUID experimentId, Map<String, Object> request);
}
