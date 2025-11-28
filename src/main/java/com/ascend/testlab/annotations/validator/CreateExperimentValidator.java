package com.ascend.testlab.annotations.validator;

import com.ascend.testlab.annotations.ValidCreateExperiment;
import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.*;

public class CreateExperimentValidator
    implements ConstraintValidator<ValidCreateExperiment, CreateExperimentRequest> {

  @Override
  public boolean isValid(CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request == null) return true;

    context.disableDefaultConstraintViolation();
    boolean isValid = validateTime(request, context);

    if (!validateTargeting(request, context)) isValid = false;

    Set<String> weightKeys = validateAndGetWeightKeys(request, context);
    if (Objects.isNull(weightKeys)) {
      return false;
    }

    if (!validateVariants(request, weightKeys, context)) isValid = false;

    if (!statusCheck(context, request.getStatus())) {
      isValid = false;
    }

    return isValid;
  }

  private boolean validateTime(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getStartTime() != null && request.getStartTime() < System.currentTimeMillis()) {
      addError(context, "Start time must be in the future", "startTime");
      return false;
    }

    if (request.getEndTime() != null && request.getStartTime() >= request.getEndTime()) {
      addError(context, "End time must be greater than start time", "endTime");
      return false;
    }
    return true;
  }

  private boolean validateTargeting(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    boolean isCohortsEmpty = (request.getCohorts() == null || request.getCohorts().isEmpty());
    boolean isRulesEmpty =
        (request.getRuleAttributes() == null || request.getRuleAttributes().isEmpty());

    if (isCohortsEmpty && isRulesEmpty) {
      addError(
          context,
          "Experiment must have at least one targeting criteria (cohorts or rules).",
          "cohorts");
      return false;
    }
    return true;
  }

  private Set<String> validateAndGetWeightKeys(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getVariantWeights() == null) return Collections.emptySet();

    try {
      AssignmentDomain declaredDomain = AssignmentDomain.valueOf(request.getAssignmentDomain());
      VariantWeights weights = request.getVariantWeights();

      if (weights.getType() != null && !declaredDomain.equals(weights.getType())) {
        addError(
            context,
            String.format(
                "Variant weights type '%s' does not match assignment domain '%s'",
                weights.getType(), declaredDomain),
            "variantWeights");
        return null;
      }

      if (AssignmentDomain.COHORT.equals(declaredDomain)) {
        return validateCohortWeights(weights, context);
      } else {
        return validateStratifiedWeights(weights, declaredDomain, context);
      }

    } catch (IllegalArgumentException e) {
      return Collections.emptySet();
    }
  }

  private Set<String> validateCohortWeights(
      VariantWeights weights, ConstraintValidatorContext context) {
    if (!(weights instanceof CohortVariantWeights)) {
      addError(
          context,
          "For COHORT domain, variant weights must be of type Map<String, Double>.",
          "variantWeights");
      return null;
    }

    Map<String, Double> map = ((CohortVariantWeights) weights).getWeights();
    if (map == null) return Collections.emptySet();

    double sum = map.values().stream().mapToDouble(d -> d == null ? 0.0 : d).sum();
    if (Math.abs(sum - 100.0) > 0.001) {
      addError(
          context, "For COHORT domain, variant weights must sum to exactly 100", "variantWeights");
    }
    return map.keySet();
  }

  private Set<String> validateStratifiedWeights(
      VariantWeights weights, AssignmentDomain domain, ConstraintValidatorContext context) {
    if (!(weights instanceof StratifiedVariantWeights)) {
      addError(
          context,
          "For " + domain + " domain, variant weights must be of type Map<String, List<String>>.",
          "variantWeights");
      return null;
    }

    Map<String, List<String>> map = ((StratifiedVariantWeights) weights).getWeights();
    if (map == null) return Collections.emptySet();

    boolean anyListEmpty = map.values().stream().anyMatch(list -> list == null || list.isEmpty());
    if (anyListEmpty) {
      addError(context, "Stratified lists cannot be empty", "variantWeights");
    }
    return map.keySet();
  }

  private boolean validateVariants(
      CreateExperimentRequest request, Set<String> weightKeys, ConstraintValidatorContext context) {
    if (request.getVariants() == null || weightKeys.isEmpty()) return true;

    Set<String> variantKeys = request.getVariants().keySet();
    boolean isValid = true;

    if (!weightKeys.equals(variantKeys)) {
      addError(
          context, "Keys in 'variantWeights' must match keys in 'variants' exactly", "variants");
      isValid = false;
    }

    if (!checkVariantNamingSequence(variantKeys)) {
      addError(
          context, "Variants must be named 'control', 'variant1', 'variant2', etc.", "variants");
      isValid = false;
    }
    return isValid;
  }

  private boolean checkVariantNamingSequence(Set<String> keys) {
    if (keys == null || keys.isEmpty()) return true;
    if (!keys.contains("control")) return false;

    int expectedNumberedVariants = keys.size() - 1;
    for (int i = 1; i <= expectedNumberedVariants; i++) {
      if (!keys.contains("variant" + i)) return false;
    }
    return true;
  }

  private boolean statusCheck(ConstraintValidatorContext context, String experimentStatus) {
    if (experimentStatus.equals(ExperimentStatus.LIVE.name())
        || experimentStatus.equals(ExperimentStatus.DRAFT.name())) {
      return true;
    }
    addError(context, "Experiment status must be LIVE or DRAFT", "experimentStatus");
    return false;
  }

  private void addError(ConstraintValidatorContext context, String message, String fieldName) {
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(fieldName)
        .addConstraintViolation();
  }
}
