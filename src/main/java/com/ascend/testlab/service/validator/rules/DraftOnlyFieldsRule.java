package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Objects;

/**
 * Validates fields that can only be updated in DRAFT mode.
 *
 * <p>DRAFT-only fields:
 *
 * <ul>
 *   <li>experiment_key
 *   <li>hypothesis
 *   <li>start_time
 * </ul>
 *
 * <p>Note: Cohort removal is handled separately by {@link CohortValidationRule}.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class DraftOnlyFieldsRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return existing.getStatus() != ExperimentStatus.DRAFT;
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {

    if (request.getExperimentKey() != null
        && !Objects.equals(request.getExperimentKey(), existing.getExperimentKey())) {
      throw new RestException(ErrorEnum.EXPERIMENT_KEY_UPDATE_NOT_ALLOWED);
    }

    if (request.getHypothesis() != null
        && !Objects.equals(request.getHypothesis(), existing.getHypothesis())) {
      throw new RestException(ErrorEnum.HYPOTHESIS_UPDATE_NOT_ALLOWED);
    }

    if (request.getStartTime() != null
        && !Objects.equals(request.getStartTime(), existing.getStartTime())) {
      throw new RestException(ErrorEnum.START_TIME_UPDATE_NOT_ALLOWED);
    }
  }
}
