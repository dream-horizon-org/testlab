package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;

/**
 * Interface for update validation rules.
 *
 * <p>Each rule encapsulates a single validation concern and can be composed with other rules using
 * the Chain of Responsibility pattern.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface UpdateValidationRule {

  /**
   * Validates the update request against the existing experiment.
   *
   * @param existing the current experiment state
   * @param request the update request
   * @throws com.dream11.rest.exception.RestException if validation fails
   */
  void validate(Experiment existing, UpdateExperimentRequest request);

  /**
   * Determines if this rule should be applied.
   *
   * @param existing the current experiment state
   * @param request the update request
   * @return true if the rule should be applied, false to skip
   */
  default boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return true;
  }

  /**
   * Chains this rule with another rule.
   *
   * @param next the next rule to apply
   * @return a composite rule that applies both rules
   */
  default UpdateValidationRule andThen(UpdateValidationRule next) {
    return (existing, request) -> {
      if (this.appliesTo(existing, request)) {
        this.validate(existing, request);
      }
      if (next.appliesTo(existing, request)) {
        next.validate(existing, request);
      }
    };
  }
}
