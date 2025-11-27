package com.ascend.testlab.service;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

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
   * Retrieves a single experiment by project Key and experiment ID.
   *
   * @param projectKey the project Key that contains the experiment
   * @param experimentId the unique identifier of the experiment to retrieve
   * @return a Single containing the Experiment object if found, or an error if not found or on
   *     failure
   */
  Single<Experiment> getExperiment(String projectKey, String experimentId);

  /**
   * Filters experiments based on the provided criteria and returns paginated results.
   *
   * <p>Supports filtering by multiple criteria including status, type, name, tags, and owners.
   * Multiple filter values can be provided as comma-separated strings for status, type, tag, and
   * owner parameters. The name filter supports text search and does not support comma-separated
   * values.
   *
   * <p>Pagination defaults to limit=20 and page=1 if not specified. Page numbers start at 1.
   *
   * @param projectKey the project Key to filter experiments within
   * @param request the filter criteria including status, type, name, tags, owners, and pagination
   *     parameters (limit and page)
   * @return a Single containing FilterExperimentsResponse with filtered and paginated experiments
   *     along with pagination metadata
   */
  Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest request);

  /**
   * Deletes an experiment by project Key and experiment ID.
   *
   * <p>Deletes a single experiment and all its associated data including tags and owner mappings.
   * The deletion is performed in a transaction to ensure data consistency. The previous experiment
   * data is logged in the experiment_log table for audit purposes.
   *
   * @param projectKey the project Key that contains the experiment to delete
   * @param experimentId the unique identifier of the experiment to delete
   * @return a Single containing DeleteExperimentResponse with deletion details, or an error if the
   *     experiment ID is invalid, the experiment does not exist, or on failure
   */
  Single<Boolean> deleteExperiment(String projectKey, String experimentId);

  /**
   * Creates a new experiment with validation and error handling.
   *
   * @param projectKey project identifier from header
   * @param request experiment creation request with all experiment details
   * @return Single emitting CreateExperimentResponse with id, status, and message
   */
  Single<CreateExperimentResponse> createExperiment(String projectKey, Experiment request);

  /**
   * Updates experiment fields partially with validation.
   *
   * @param projectKey project identifier from header
   * @param experimentId experiment identifier
   * @param request update experiment request DTO with validated fields
   * @return Single emitting UpdateExperimentResponse with id, status, and message
   */
  Single<UpdateExperimentResponse> updateExperiment(
      String projectKey, UUID experimentId, UpdateExperimentRequest request);
}
