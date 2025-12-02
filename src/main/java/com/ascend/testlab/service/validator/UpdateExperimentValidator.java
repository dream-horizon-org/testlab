package com.ascend.testlab.service.validator;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.validator.rules.*;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
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

  /**
   * Validates the update request against the existing experiment.
   *
   * <p>Applies all validation rules in sequence including async validations (experiment key
   * uniqueness). Each rule may throw a {@link com.dream11.rest.exception.RestException} if
   * validation fails.
   *
   * @param projectKey the project key for DB validations
   * @param existing the current experiment state
   * @param request the update request
   * @param adminDAO the admin DAO for async validations
   * @return Single emitting true if all validations pass
   * @throws com.dream11.rest.exception.RestException if any validation fails
   */
  public static Single<Boolean> validate(
      String projectKey, Experiment existing, UpdateExperimentRequest request, AdminDAO adminDAO) {

    log.debug("Validating update request for experiment: {}", existing.getExperimentId());

    for (UpdateValidationRule rule : VALIDATION_RULES) {
      if (rule.appliesTo(existing, request)) {
        log.trace("Applying rule: {}", rule.getClass().getSimpleName());
        rule.validate(existing, request);
      }
    }

    return validateExperimentKeyUniqueness(projectKey, existing, request, adminDAO)
        .doOnSuccess(
            success ->
                log.debug(
                    "Update validation passed for experiment: {}", existing.getExperimentId()));
  }

  /**
   * Validates that the experiment_key is unique within the project.
   *
   * <p>Only checks if the request contains an experiment_key that is different from the existing
   * one.
   */
  private static Single<Boolean> validateExperimentKeyUniqueness(
      String projectKey, Experiment existing, UpdateExperimentRequest request, AdminDAO adminDAO) {

    String newKey = request.getExperimentKey();

    if (newKey == null || newKey.equals(existing.getExperimentKey())) {
      return Single.just(true);
    }

    log.debug(
        "Validating experiment key uniqueness for projectKey: {}, newKey: {}", projectKey, newKey);

    return adminDAO
        .isExperimentKeyAvailable(projectKey, newKey)
        .flatMap(
            isAvailable -> {
              if (!isAvailable) {
                log.warn("Experiment key '{}' already exists in project '{}'", newKey, projectKey);
                return Single.error(new RestException(ErrorEnum.EXPERIMENT_KEY_ALREADY_EXISTS));
              }
              return Single.just(true);
            });
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
