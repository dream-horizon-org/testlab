package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.ExperimentNameAvailabilityResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Service interface for checking if an experiment name is available.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentNameAvailabilityService {

  /**
   * Checks if an experiment name is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentName the experiment name
   * @return a Single that emits the experiment name availability response if the experiment name is
   *     available, false otherwise
   * @throws com.dream11.rest.exception.RestException if the experiment name is not available
   */
  Single<ExperimentNameAvailabilityResponse> isExperimentNameAvailable(
      String projectKey, String experimentName);
}
