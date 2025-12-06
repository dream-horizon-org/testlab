package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;

/**
 * Validates that experiments in terminal states (CONCLUDED, TERMINATED) cannot be updated.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class TerminalStateRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus currentStatus = existing.getStatus();
    if (currentStatus == ExperimentStatus.TERMINATED) {
      throw new RestException(ErrorEnum.UPDATE_NOT_ALLOWED_IN_TERMINAL_STATE);
    }
  }
}
