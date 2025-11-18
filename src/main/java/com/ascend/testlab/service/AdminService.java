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
   * Fetches the history of an experiment with pagination support.
   *
   * <p>Pagination defaults to limit=20 and page=1 if not specified. Page numbers start at 1.
   *
   * @param projectKey the project key
   * @param experimentId the experiment id
   * @param limit the maximum number of history entries to return (default: 20)
   * @param page the page number (default: 1, 1-indexed)
   * @return a Single containing the get experiment history response with pagination metadata
   * @throws com.dream11.rest.exception.RestException if the operation fails
   */
  Single<GetExperimentHistoryResponse> getExperimentHistory(
      String projectKey, String experimentId, int limit, int page);
}
