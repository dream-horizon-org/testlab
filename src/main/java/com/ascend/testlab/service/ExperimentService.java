package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
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
      UUID tenantId, UUID projectKey, CreateExperimentRequest request);

  /**
   * Updates experiment fields partially with validation.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> update(
      UUID tenantId, UUID projectKey, UUID experimentId, Map<String, Object> request);

  /**
   * Assigns experiment to user based on status criteria.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier from header
   * @param userId user identifier for assignment
   * @param status list of experiment statuses to filter
   * @return Single emitting assigned experiment request
   */
  Single<CreateExperimentRequest> assignExperiment(
      UUID tenantId, UUID projectKey, UUID userId, List<String> status);
}
