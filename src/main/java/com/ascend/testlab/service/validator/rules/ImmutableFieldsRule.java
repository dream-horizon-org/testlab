package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Objects;

/**
 * Validates that immutable fields cannot be changed after creation.
 *
 * <p>Immutable fields:
 *
 * <ul>
 *   <li>type
 *   <li>assignment_domain
 *   <li>distribution_strategy
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class ImmutableFieldsRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {

    if (request.getType() != null
        && !Objects.equals(ExperimentType.fromValue(request.getType()), existing.getType())) {
      throw new RestException(ErrorEnum.TYPE_CHANGE_NOT_ALLOWED);
    }

    if (request.getAssignmentDomain() != null
        && !Objects.equals(
            AssignmentDomain.valueOf(request.getAssignmentDomain()),
            existing.getAssignmentDomain())) {
      throw new RestException(ErrorEnum.ASSIGNMENT_DOMAIN_CHANGE_NOT_ALLOWED);
    }

    if (request.getDistributionStrategy() != null
        && !Objects.equals(
            DistributionStrategy.valueOf(request.getDistributionStrategy()),
            existing.getDistributionStrategy())) {
      throw new RestException(ErrorEnum.DISTRIBUTION_STRATEGY_CHANGE_NOT_ALLOWED);
    }
  }
}
