package com.ascend.testlab.service;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Interface for experiment operations.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see Experiment
 * @see FilterExperimentsRequest
 * @see FilterExperimentsResponse
 */
public interface ExperimentService {
  /**
   * Retrieves a single experiment by project ID and experiment ID.
   *
   * @param projectId the project ID that contains the experiment
   * @param experimentId the unique identifier of the experiment to retrieve
   * @return a Single containing the Experiment object if found, or an error if not found or on
   *     failure
   */
  Single<Experiment> getExperiment(String projectId, String experimentId);

  /**
   * Filters experiments based on the provided criteria and returns paginated results.
   *
   * @param projectId the project ID to filter experiments within
   * @param request the filter criteria including status, type, name, tags, owners, and pagination
   *     parameters
   * @return a Single containing FilterExperimentsResponse with filtered and paginated experiments
   *     along with pagination metadata
   */
  Single<FilterExperimentsResponse> filterExperiments(
      String projectId, FilterExperimentsRequest request);
}
