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
   * @return a Single
   */
  Single<Boolean> deleteExperiment(
      String projectKey, com.ascend.testlab.dto.entity.experiment.Experiment experiment);

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * <p>All necessary data (projectKey, experimentId, tags, owner) is extracted from the request
   * object.
   *
   * @param request experiment creation request with all experiment details including projectKey,
   *     experimentId, tags, and owner
   * @return Single emitting experiment ID as String on success
   */
  Single<Boolean> createExperiment(String projectKey, Experiment request);

  /**
   * Updates an experiment with tags, owners, metrics, and update log in a transaction.
   *
   * <p>Performs a full update of the merged experiment data. Tags, owners, and metrics are replaced
   * entirely. An update log entry is created with previous and current state.
   *
   * @param projectKey project identifier for partitioning
   * @param previousExperiment the experiment state before update
   * @param updatedExperiment the merged experiment with updates applied
   * @return Single emitting true on success
   */
  Single<Boolean> updateExperiment(
      String projectKey, Experiment previousExperiment, Experiment updatedExperiment);
}
