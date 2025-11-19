package com.ascend.testlab.service;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

/**
 * Service interface for Experiment business logic.
 *
 * <p>Provides methods for creating, updating, and assigning experiments with proper validation and
 * error handling.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentService {

  /**
   * Creates a new experiment with validation and error handling.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param request experiment creation request with all experiment details
   * @return Single emitting CreateExperimentResponse with id, status, and message
   */
  Single<CreateExperimentResponse> create(
      UUID tenantId, String projectKey, CreateExperimentRequest request);

  /**
   * Updates experiment fields partially with validation.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param experimentId experiment identifier
   * @param request update experiment request DTO with validated fields
   * @return Single emitting UpdateExperimentResponse with id, status, and message
   */
  Single<UpdateExperimentResponse> update(
      UUID tenantId, String projectKey, UUID experimentId, UpdateExperimentRequest request);

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
}
