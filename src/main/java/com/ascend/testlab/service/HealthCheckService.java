package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.HealthCheckResponse;
import io.reactivex.rxjava3.core.Single;

public interface HealthCheckService {
  Single<HealthCheckResponse> healthCheck();
}
