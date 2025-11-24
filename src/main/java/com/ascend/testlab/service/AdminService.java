package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.ExperimentKeyAvailabilityResponse;
import com.ascend.testlab.dto.response.TagsResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Service interface for admin operations including tags and key availability.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsResponse
 * @see ExperimentKeyAvailabilityResponse
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
   * Checks if an experiment key is available for a given project key.
   *
   * @param projectKey the project key
   * @param experimentKey the experiment key
   * @return a Single that emits the experiment key availability response of the experiment,
   *     indicating whether the experiment key is available
   * @throws com.dream11.rest.exception.RestException if the experiment key is not available
   */
  Single<ExperimentKeyAvailabilityResponse> isExperimentKeyAvailable(
      String projectKey, String experimentKey);

  /**
   * Fetches the history of an experiment with pagination support.
   *
   * <p>Pagination defaults to limit=20 and page=1 if not specified. Page numbers start at 1.
   *
   * @param request the experiment history request containing project key, experiment id, limit, and
   *     page
   * @return a Single containing the experiment history response with pagination metadata
   * @throws com.dream11.rest.exception.RestException if the operation fails
   */
  Single<ExperimentHistoryResponse> getExperimentHistory(ExperimentHistoryRequest request);
}
