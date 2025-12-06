package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.HealthCheckResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Interface for the health check service. Contains method to check the health of the database
 * connections and application status.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface HealthCheckService {
  /**
   * Check the health of the database connections and application status.
   *
   * @return a Single that emits the health check response
   */
  Single<HealthCheckResponse> healthCheck();
}
