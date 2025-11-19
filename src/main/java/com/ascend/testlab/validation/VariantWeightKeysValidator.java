package com.ascend.testlab.validation;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.validation.annotations.ValidVariantWeightKeys;
import com.ascend.testlab.variantWeights.CohortVariantWeights;
import com.ascend.testlab.variantWeights.ManualVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.*;

/**
 * Validator to ensure variant keys in variants match with variant_weights keys.
 *
 * <p>This validator checks that: - All variant keys in variants exist in variant_weights - All
 * variant keys in variant_weights exist in variants - The order of keys is the same in both
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariantWeightKeysValidator
    implements ConstraintValidator<ValidVariantWeightKeys, CreateExperimentRequest> {

  @Override
  public void initialize(ValidVariantWeightKeys constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Let @NotNull handle null validation
    }

    Map<String, Variant> variants = request.getVariants();
    VariantWeights variantWeights = request.getVariantWeights();

    // If either is null, skip validation (let @NotNull handle it)
    if (variants == null || variantWeights == null) {
      return true;
    }

    // Get variant keys from variants map (maintains insertion order with LinkedHashMap)
    List<String> variantKeys = new ArrayList<>(variants.keySet());

    // Get variant keys from variant_weights based on type
    List<String> weightKeys = getWeightKeys(variantWeights);

    if (weightKeys == null) {
      // Unknown variant weights type, skip validation
      return true;
    }

    // Check if count matches first
    if (variantKeys.size() != weightKeys.size()) {
      context.disableDefaultConstraintViolation();

      StringBuilder errorMessage = new StringBuilder();
      errorMessage.append("Count of variants must match count of variant_weights. ");
      errorMessage.append("Variants count: ").append(variantKeys.size());
      errorMessage.append(", variant_weights count: ").append(weightKeys.size());

      context
          .buildConstraintViolationWithTemplate(errorMessage.toString())
          .addPropertyNode("variant_weights")
          .addConstraintViolation();
      return false;
    }

    // Check if keys match in the same order
    if (!variantKeys.equals(weightKeys)) {
      context.disableDefaultConstraintViolation();

      // Build detailed error message
      StringBuilder errorMessage = new StringBuilder();
      errorMessage.append(
          "Variant keys in 'variants' must match with 'variant_weights' keys in the same order. ");
      errorMessage.append("Expected order: ").append(variantKeys);
      errorMessage.append(", but found: ").append(weightKeys);

      // Check for missing keys
      List<String> missingInWeights = new ArrayList<>(variantKeys);
      missingInWeights.removeAll(weightKeys);
      if (!missingInWeights.isEmpty()) {
        errorMessage.append(". Missing in variant_weights: ").append(missingInWeights);
      }

      // Check for extra keys
      List<String> extraInWeights = new ArrayList<>(weightKeys);
      extraInWeights.removeAll(variantKeys);
      if (!extraInWeights.isEmpty()) {
        errorMessage.append(". Extra in variant_weights: ").append(extraInWeights);
      }

      context
          .buildConstraintViolationWithTemplate(errorMessage.toString())
          .addPropertyNode("variant_weights")
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  /**
   * Extracts variant keys from VariantWeights based on its type.
   *
   * @param variantWeights the variant weights object
   * @return list of variant keys in order, or null if type is unknown
   */
  private List<String> getWeightKeys(VariantWeights variantWeights) {
    if (variantWeights instanceof CohortVariantWeights) {
      CohortVariantWeights cohortWeights = (CohortVariantWeights) variantWeights;
      Map<String, Double> weights = cohortWeights.getWeights();
      return weights != null ? new ArrayList<>(weights.keySet()) : new ArrayList<>();
    } else if (variantWeights instanceof ManualVariantWeights) {
      ManualVariantWeights manualWeights = (ManualVariantWeights) variantWeights;
      Map<String, List<String>> weights = manualWeights.getWeights();
      return weights != null ? new ArrayList<>(weights.keySet()) : new ArrayList<>();
    }
    return null; // Unknown type
  }
}
