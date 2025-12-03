package com.ascend.testlab.service.validator;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.validator.rules.*;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates experiment update requests using Chain of Responsibility pattern.
 *
 * <p>Validation rules applied in order:
 *
 * <ol>
 *   <li>TerminalStateRule - No updates in CONCLUDED/TERMINATED
 *   <li>StatusTransitionRule - Valid status transitions
 *   <li>ImmutableFieldsRule - type, assignment_domain, distribution_strategy immutable
 *   <li>DraftOnlyFieldsRule - experiment_key, hypothesis, start_time only in DRAFT
 *   <li>TimeValidationRule - start_time/end_time must be future
 *   <li>CohortValidationRule - Cohort type/removal validation
 *   <li>VariantValidationRule - Variant naming/removal/variable consistency
 *   <li>VariantWeightsValidationRule - Weight keys match variants, sum to 100
 *   <li>RuleAttributesValidationRule - Rule/condition update constraints
 *   <li>WinningVariantValidationRule - Only on CONCLUDED transition
 * </ol>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateExperimentValidator {

  private static final List<UpdateValidationRule> RULES =
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
   * @param projectKey the project key
   * @param existing the current experiment state
   * @param request the update request
   * @param adminDAO the admin DAO for async validations
   * @return Single emitting true if all validations pass
   * @throws RestException if any validation fails
   */
  public static Single<Boolean> validate(
      String projectKey, Experiment existing, UpdateExperimentRequest request, AdminDAO adminDAO) {

    log.debug("Validating update for experiment: {}", existing.getExperimentId());

    for (UpdateValidationRule rule : RULES) {
      if (rule.appliesTo(existing, request)) {
        rule.validate(existing, request);
      }
    }

    return checkExperimentKeyUniqueness(projectKey, existing, request, adminDAO);
  }

  private static Single<Boolean> checkExperimentKeyUniqueness(
      String projectKey, Experiment existing, UpdateExperimentRequest request, AdminDAO adminDAO) {

    String newKey = request.getExperimentKey();

    if (newKey == null || newKey.equals(existing.getExperimentKey())) {
      return Single.just(true);
    }

    return adminDAO
        .isExperimentKeyAvailable(projectKey, newKey)
        .flatMap(
            available -> {
              if (!available) {
                log.warn("Experiment key '{}' already exists in project '{}'", newKey, projectKey);
                return Single.error(new RestException(ErrorEnum.EXPERIMENT_KEY_ALREADY_EXISTS));
              }
              return Single.just(true);
            });
  }

  /**
   * Returns validation rules for testing.
   *
   * @return list of update validation rules
   */
  public static List<UpdateValidationRule> getRules() {
    return RULES;
  }
}
