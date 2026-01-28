package com.ascend.testlab.annotations.validator;

import com.ascend.testlab.annotations.ValidCreateExperiment;
import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Variables;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Validator for create experiment requests.
 *
 * <p>Validates:
 *
 * <ul>
 *   <li>Time constraints (end time must be greater than start time)
 *   <li>Targeting criteria (must have cohorts or rules)
 *   <li>Variant weights consistency with assignment domain
 *   <li>Variant naming sequence (control, variant1, variant2, ...)
 *   <li>Status must be LIVE or DRAFT
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class CreateExperimentValidator
    implements ConstraintValidator<ValidCreateExperiment, CreateExperimentRequest> {

  /** {@inheritDoc} */
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

    if (!validateOverrides(request, context)) {
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
      addError(context, ErrorMessages.INVALID_TARGET_CRITERIA, "cohorts");
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
      return null;
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
      return null;
    }

    // Validate that no cohort appears in multiple variants
    if (!validateNoDuplicateCohortAcrossVariants(map, context)) {
      return null;
    }

    return map.keySet();
  }

  /**
   * Validates that no cohort appears in more than one variant's list for stratified weights.
   *
   * @param variantCohortMap map of variant names to their cohort lists
   * @param context the constraint validator context
   * @return true if valid (no duplicates), false otherwise
   */
  private boolean validateNoDuplicateCohortAcrossVariants(
      Map<String, List<String>> variantCohortMap, ConstraintValidatorContext context) {

    Set<String> seenCohorts = new HashSet<>();
    Set<String> duplicateCohorts = new HashSet<>();

    for (List<String> cohorts : variantCohortMap.values()) {
      if (cohorts == null) continue;
      for (String cohort : cohorts) {
        if (!seenCohorts.add(cohort)) {
          duplicateCohorts.add(cohort);
        }
      }
    }

    if (!duplicateCohorts.isEmpty()) {
      addError(
          context,
          String.format(
              "Cohorts %s appear in multiple variants. Each cohort must be assigned to only one variant.",
              duplicateCohorts),
          "variantWeights");
      return false;
    }
    return true;
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

    if (!validateVariantVariables(request, context)) {
      isValid = false;
    }

    return isValid;
  }

  private boolean validateVariantVariables(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getVariants() == null) return true;

    boolean isValid = true;
    for (Map.Entry<String, Variant> entry : request.getVariants().entrySet()) {
      Variant variant = entry.getValue();
      if (variant.getVariables() == null || variant.getVariables().isEmpty()) {
        addError(
            context,
            String.format("Variant '%s' must have at least 1 variable", entry.getKey()),
            "variants");
        isValid = false;
      }
    }

    if (!validateVariableDataTypeConsistency(request, context)) {
      isValid = false;
    }

    if (!validateVariableKeysConsistency(request, context)) {
      isValid = false;
    }

    if (!validateVariableValuesNotEmpty(request, context)) {
      isValid = false;
    }

    return isValid;
  }

  /**
   * Validates that no variable has an empty value.
   *
   * @param request the create experiment request
   * @param context the constraint validator context
   * @return true if valid, false otherwise
   */
  private boolean validateVariableValuesNotEmpty(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getVariants() == null) return true;

    for (Map.Entry<String, Variant> entry : request.getVariants().entrySet()) {
      Variant variant = entry.getValue();
      if (variant.getVariables() == null) continue;

      for (Variables var : variant.getVariables()) {
        if (var.getValue() == null || var.getValue().isBlank()) {
          addError(
              context,
              String.format(
                  "Variable '%s' in variant '%s' cannot have an empty value",
                  var.getKey(), entry.getKey()),
              "variants");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Validates that all variants have the same variable keys.
   *
   * @param request the create experiment request
   * @param context the constraint validator context
   * @return true if valid, false otherwise
   */
  private boolean validateVariableKeysConsistency(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getVariants() == null || request.getVariants().isEmpty()) return true;

    // Collect all unique variable keys across all variants
    Set<String> allKeys = new HashSet<>();
    for (com.ascend.testlab.dto.entity.experiment.Variant variant :
        request.getVariants().values()) {
      if (variant.getVariables() != null) {
        for (com.ascend.testlab.dto.entity.experiment.Variables var : variant.getVariables()) {
          allKeys.add(var.getKey());
        }
      }
    }

    // Check that each variant has exactly the same keys
    for (Map.Entry<String, com.ascend.testlab.dto.entity.experiment.Variant> entry :
        request.getVariants().entrySet()) {
      Set<String> variantKeys = new HashSet<>();
      if (entry.getValue().getVariables() != null) {
        for (com.ascend.testlab.dto.entity.experiment.Variables var :
            entry.getValue().getVariables()) {
          variantKeys.add(var.getKey());
        }
      }

      if (!variantKeys.equals(allKeys)) {
        addError(context, "All variants must have the same variable keys", "variants");
        return false;
      }
    }
    return true;
  }

  /**
   * Validates that the same variable key has the same dataType across all variants.
   *
   * @param request the create experiment request
   * @param context the constraint validator context
   * @return true if valid, false otherwise
   */
  private boolean validateVariableDataTypeConsistency(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getVariants() == null) return true;

    Map<String, String> keyDataTypes = new HashMap<>();

    for (Variant variant : request.getVariants().values()) {
      if (variant.getVariables() == null) continue;

      for (Variables var : variant.getVariables()) {
        String key = var.getKey();
        String dataType = var.getDataType();

        if (keyDataTypes.containsKey(key)) {
          if (!Objects.equals(keyDataTypes.get(key), dataType)) {
            addError(
                context,
                "Same variable key must have the same dataType across all variants",
                "variants");
            return false;
          }
        } else {
          keyDataTypes.put(key, dataType);
        }
      }
    }
    return true;
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
        || experimentStatus.equals(ExperimentStatus.DRAFT.name())
        || experimentStatus.equals(ExperimentStatus.TEST.name())) {
      return true;
    }
    addError(context, "Experiment status must be LIVE, DRAFT, or TEST", "experimentStatus");
    return false;
  }

  /**
   * Validates that if overrides are provided, all variant names exist in the variants map.
   *
   * @param request the create experiment request
   * @param context the constraint validator context
   * @return true if valid, false otherwise
   */
  private boolean validateOverrides(
      CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request.getOverrides() == null || request.getOverrides().isEmpty()) {
      return true;
    }

    Map<String, List<String>> variantUserMap = request.getOverrides().getOverrideIds();

    Map<String, ?> variants = request.getVariants();
    if (variants == null || variants.isEmpty()) {
      addError(context, "Overrides specified but no variants defined", "overrides");
      return false;
    }

    Set<String> invalidVariants = new HashSet<>();
    for (String variantName : variantUserMap.keySet()) {
      if (!variants.containsKey(variantName)) {
        invalidVariants.add(variantName);
      }
    }

    if (!invalidVariants.isEmpty()) {
      addError(
          context,
          String.format(
              "Override variant names %s do not exist in variants. Available variants: %s",
              invalidVariants, variants.keySet()),
          "overrides");
      return false;
    }

    return true;
  }

  private void addError(ConstraintValidatorContext context, String message, String fieldName) {
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(fieldName)
        .addConstraintViolation();
  }
}
