package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
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
}
