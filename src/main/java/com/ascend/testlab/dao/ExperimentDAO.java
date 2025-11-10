package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Interface for experiment-related database operations.
 */
public interface ExperimentDAO {
  /**
   * Retrieves a single experiment by project ID and experiment ID.
   *
   * @param projectId the project ID that owns the experiment
   * @param experimentId the unique identifier of the experiment
   * @return a Single that emits the Experiment if found, or an error if not found
   */
  Single<Experiment> getExperiment(String projectId, String experimentId);

  /**
   * Filters experiments based on the provided criteria and returns paginated results.
   * Supports filtering by various attributes such as name, tags, type, status, and owner.
   *
   * @param projectId the project ID to filter experiments within
   * @param req the filter request containing filter criteria, pagination, and sorting options
   * @return a Single that emits a FilterExperimentsResponse containing the filtered experiments
   *         and pagination metadata
   */
  Single<FilterExperimentsResponse> filterExperiments(
      String projectId, FilterExperimentsRequest req);
}
