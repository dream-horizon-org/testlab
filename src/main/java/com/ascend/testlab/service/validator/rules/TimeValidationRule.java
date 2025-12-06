package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.time.Instant;

/**
 * Validates time-related constraints.
 *
 * <ul>
 *   <li>startTime must be in future (only validated in DRAFT mode)
 *   <li>endTime must always be in future when provided
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class TimeValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus status = existing.getStatus();
    long now = Instant.now().toEpochMilli();

    if (request.getStartTime() != null && status == ExperimentStatus.DRAFT) {
      if (request.getStartTime() <= now) {
        throw new RestException(ErrorEnum.START_TIME_MUST_BE_FUTURE);
      }
    }

    if (request.getEndTime() != null && request.getEndTime() <= now) {
      throw new RestException(ErrorEnum.END_TIME_MUST_BE_FUTURE);
    }
  }
}
