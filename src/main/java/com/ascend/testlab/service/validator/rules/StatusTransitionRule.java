package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Map;
import java.util.Set;

/**
 * Validates status transitions follow the allowed state machine.
 *
 * <ul>
 *   <li>DRAFT → LIVE
 *   <li>LIVE → PAUSED, CONCLUDED, TERMINATED
 *   <li>PAUSED → LIVE, TERMINATED
 *   <li>CONCLUDED and TERMINATED are terminal states (no transitions allowed)
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class StatusTransitionRule implements UpdateValidationRule {

  /** Map of allowed status transitions from each state. */
  private static final Map<ExperimentStatus, Set<ExperimentStatus>> ALLOWED_TRANSITIONS =
      Map.of(
          ExperimentStatus.DRAFT, Set.of(ExperimentStatus.LIVE),
          ExperimentStatus.LIVE,
              Set.of(
                  ExperimentStatus.PAUSED, ExperimentStatus.CONCLUDED, ExperimentStatus.TERMINATED),
          ExperimentStatus.PAUSED, Set.of(ExperimentStatus.LIVE, ExperimentStatus.TERMINATED));

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return request.getStatus() != null
        && ExperimentStatus.fromValue(request.getStatus()) != existing.getStatus();
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus currentStatus = existing.getStatus();
    ExperimentStatus newStatus = ExperimentStatus.valueOf(request.getStatus());

    Set<ExperimentStatus> allowedNextStates = ALLOWED_TRANSITIONS.get(currentStatus);

    if (allowedNextStates == null || !allowedNextStates.contains(newStatus)) {
      throw new RestException(ErrorEnum.INVALID_STATUS_TRANSITION);
    }
  }
}
