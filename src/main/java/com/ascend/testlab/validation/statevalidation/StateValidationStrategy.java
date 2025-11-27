package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import java.util.UUID;

/**
 * Strategy interface for state-based validation of experiment updates.
 *
 * <p>Implementations define validation rules for specific experiment states.
 */
public interface StateValidationStrategy {

  /**
   * Checks if this strategy applies to the given experiment state.
   *
   * @param currentStatus the current experiment status
   * @return true if this strategy should validate the request
   */
  boolean appliesTo(String currentStatus);

  /**
   * Validates the update request based on the experiment's current state.
   *
   * @param request the update request containing fields to update
   * @param currentStatus the current experiment status
   * @param experimentId the experiment identifier for logging
   * @throws IllegalArgumentException if validation fails
   */
  void validate(UpdateExperimentRequest request, String currentStatus, UUID experimentId);
}
