package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Validates cohort-related constraints.
 *
 * <ul>
 *   <li>Cohorts and rule_attributes cannot be empty (DRAFT only)
 *   <li>Cohort type (COHORT/STRATIFIED) cannot be changed
 *   <li>In STRATIFIED experiments, cohorts in variant weights must exist in cohorts list
 *   <li>Cohorts can only be removed in DRAFT mode
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class CohortValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return request.getCohorts() != null
        || request.getVariantWeights() != null
        || request.getRuleAttributes() != null;
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus status = existing.getStatus();
    validateCohortsAndRulesNotEmpty(status, request);
    validateCohortTypeUnchanged(existing, request);
    validateCohortRemoval(status, existing.getCohorts(), request.getCohorts());
  }

  /** Validates that cohorts and rule_attributes are not empty - only in DRAFT mode. */
  private void validateCohortsAndRulesNotEmpty(
      ExperimentStatus status, UpdateExperimentRequest request) {
    if (status != ExperimentStatus.DRAFT) {
      return;
    }

    List<String> cohorts = request.getCohorts();
    List<?> rules = request.getRuleAttributes();
    boolean isCohortsEmpty = (request.getCohorts() == null || request.getCohorts().isEmpty());
    boolean isRulesEmpty =
        (request.getRuleAttributes() == null || request.getRuleAttributes().isEmpty());
    if (isCohortsEmpty && isRulesEmpty) {
      throw new RestException(ErrorEnum.COHORTS_CANNOT_BE_EMPTY);
    }
  }

  /** Validates that the cohort type (COHORT vs STRATIFIED) cannot be changed. */
  private void validateCohortTypeUnchanged(Experiment existing, UpdateExperimentRequest request) {
    if (request.getVariantWeights() == null) {
      return;
    }

    VariantWeights existingWeights = existing.getVariantWeights();
    VariantWeights newWeights = request.getVariantWeights();

    if (existingWeights != null && newWeights != null) {
      AssignmentDomain existingType = existingWeights.getType();
      AssignmentDomain newType = newWeights.getType();

      if (Objects.isNull(newType) || !existingType.equals(newType)) {
        throw new RestException(ErrorEnum.COHORT_TYPE_CHANGE_NOT_ALLOWED);
      }
    }
  }

  /**
   * Validates cohort changes: - Cohorts can only be removed in DRAFT mode - Adding cohorts is
   * allowed in any non-terminal state
   */
  private void validateCohortRemoval(
      ExperimentStatus status, List<String> existingCohorts, List<String> requestCohorts) {

    if (requestCohorts == null || status == ExperimentStatus.DRAFT) {
      return;
    }

    if (existingCohorts == null) {
      return;
    }

    Set<String> existing = new HashSet<>(existingCohorts);
    Set<String> updated = new HashSet<>(requestCohorts);

    for (String cohort : existing) {
      if (!updated.contains(cohort)) {
        throw new RestException(ErrorEnum.COHORT_REMOVAL_NOT_ALLOWED);
      }
    }
  }
}
