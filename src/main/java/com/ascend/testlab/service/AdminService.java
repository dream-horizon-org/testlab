package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.dto.response.TagsResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Service interface for admin operations including tags and name availability.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsResponse
 * @see NameAvailabilityResponse
 */
public interface AdminService {

  /**
   * Retrieves all distinct tags for experiments within the specified project.
   *
   * @param projectKey the project key to fetch tags for
   * @return a Single containing TagsResponse with the list of distinct tags
   * @throws com.dream11.rest.exception.RestException if the operation fails
   */
  Single<TagsResponse> getTags(String projectKey);

  /**
   * Checks if an experiment name is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentName the experiment name
   * @return a Single that emits the name availability response if the experiment name is available,
   *     false otherwise
   * @throws com.dream11.rest.exception.RestException if the experiment name is not available
   */
  Single<NameAvailabilityResponse> isExperimentNameAvailable(
      String projectKey, String experimentName);

  /**
   * Fetches the history of an experiment.
   *
   * @param projectKey the project key
   * @param experimentId the experiment id
   * @return a Single containing the get experiment history response
   * @throws com.dream11.rest.exception.RestException if the operation fails
   */
  Single<GetExperimentHistoryResponse> getExperimentHistory(String projectKey, String experimentId);
}
