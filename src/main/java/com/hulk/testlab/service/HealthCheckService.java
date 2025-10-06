package com.hulk.testlab.service;

import com.hulk.testlab.dto.response.HealthCheckResponse;
import io.reactivex.rxjava3.core.Single;

public interface HealthCheckService {
  Single<HealthCheckResponse> healthCheck();
}
