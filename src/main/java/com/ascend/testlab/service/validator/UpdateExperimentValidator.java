package com.ascend.testlab.service.validator;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.service.validator.rules.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates experiment update requests using Chain of Responsibility pattern.
 *
 * <p>Each validation rule is a separate class implementing {@link UpdateValidationRule}, making the
 * validation logic modular, testable, and extensible.
 *
 * <h2>Validation Rules (in order):</h2>
 *
 * <ol>
 *   <li><b>TerminalStateRule</b> - No updates allowed in CONCLUDED/TERMINATED states
 *   <li><b>StatusTransitionRule</b> - Valid status transitions only
 *   <li><b>ImmutableFieldsRule</b> - type, assignment_domain, distribution_strategy cannot change
 *   <li><b>DraftOnlyFieldsRule</b> - experiment_key, hypothesis, start_time only in DRAFT
 *   <li><b>TimeValidationRule</b> - start_time and end_time must be future
 *   <li><b>CohortValidationRule</b> - Cohort type, removal, stratified validation
 *   <li><b>VariantValidationRule</b> - Variant naming, removal, variable consistency
 *   <li><b>VariantWeightsValidationRule</b> - Weight keys match variants, sum to 100
 *   <li><b>RuleAttributesValidationRule</b> - Rule/condition update constraints
 *   <li><b>WinningVariantValidationRule</b> - Only on CONCLUDED transition
 * </ol>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public final class UpdateExperimentValidator {

  private static final List<UpdateValidationRule> VALIDATION_RULES =
      List.of(
          new TerminalStateRule(),
          new StatusTransitionRule(),
          new ImmutableFieldsRule(),
          new DraftOnlyFieldsRule(),
          new TimeValidationRule(),
          new CohortValidationRule(),
          new VariantValidationRule(),
          new VariantWeightsValidationRule(),
          new RuleAttributesValidationRule(),
          new WinningVariantValidationRule());

  private UpdateExperimentValidator() {
    // Utility class
  }

  /**
   * Validates the update request against the existing experiment.
   *
   * <p>Applies all validation rules in sequence. Each rule may throw a {@link
   * com.dream11.rest.exception.RestException} if validation fails.
   *
   * @param existing the current experiment state
   * @param request the update request
   * @throws com.dream11.rest.exception.RestException if any validation fails
   */
  public static void validate(Experiment existing, UpdateExperimentRequest request) {
    log.debug("Validating update request for experiment: {}", existing.getExperimentId());

    for (UpdateValidationRule rule : VALIDATION_RULES) {
      if (rule.appliesTo(existing, request)) {
        log.trace("Applying rule: {}", rule.getClass().getSimpleName());
        rule.validate(existing, request);
      }
    }

    log.debug("Update validation passed for experiment: {}", existing.getExperimentId());
  }

  /**
   * Returns the list of validation rules for testing purposes.
   *
   * @return immutable list of validation rules
   */
  public static List<UpdateValidationRule> getRules() {
    return VALIDATION_RULES;
  }
}
