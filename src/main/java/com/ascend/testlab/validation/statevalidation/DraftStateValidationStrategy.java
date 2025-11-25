package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation strategy for experiments in DRAFT state.
 *
 * <p>Allows updates to all updatable fields (no restrictions).
 */
@Slf4j
public class DraftStateValidationStrategy implements StateValidationStrategy {

  @Override
  public boolean appliesTo(String currentStatus) {
    if (currentStatus == null) {
      return false;
    }
    try {
      ExperimentStatus status = ExperimentStatus.valueOf(currentStatus);
      return status == ExperimentStatus.DRAFT;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid status '{}', skipping DRAFT state validation", currentStatus);
      return false;
    }
  }

  @Override
  public void validate(UpdateExperimentRequest request, String currentStatus, UUID experimentId) {
    // DRAFT state allows all field updates - no restrictions
    log.debug("Experiment {} is in DRAFT state, all field updates allowed", experimentId);
  }

  @Override
  public int getPriority() {
    return 50; // Lower priority than LIVE state
  }
}
