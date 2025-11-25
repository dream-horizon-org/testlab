package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/** Interface for experiment-related database operations. */
public interface ExperimentDAO {
  /**
   * Retrieves a single experiment by project Key and experiment ID.
   *
   * @param projectKey the project key to fetch experiment
   * @param experimentId the unique identifier of the experiment
   * @return a Maybe that emits the Experiment if found, or else empty
   */
  Maybe<Experiment> getExperiment(String projectKey, String experimentId);

  /**
   * Filters experiments based on the provided criteria and returns paginated results. Supports
   * filtering by various attributes such as name, tags, type, status, and owner.
   *
   * @param projectKey the project Key to filter experiments within
   * @param req the filter request containing filter criteria, pagination, and sorting options
   * @return a Single that emits a FilterExperimentsResponse containing the filtered experiments and
   *     pagination metadata
   */
  Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest req);

  /**
   * Delete experiment and related data for the provided projectKey and experimentId
   *
   * @param projectKey the project Key
   * @param experiment the experiment data
   * @return a Single that emits true if deletion is successful, false otherwise
   */
  Single<Boolean> deleteExperiment(String projectKey, Experiment experiment);
}
