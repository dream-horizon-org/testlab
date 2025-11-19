package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidWeightSum;
import com.ascend.testlab.variantWeights.CohortVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;

/**
 * Validator to ensure the sum of weights in variant_weights equals 100.
 *
 * <p>This validator checks that all weight values sum to exactly 100.0 for CohortVariantWeights.
 * ManualVariantWeights are not validated as they don't have numeric weights.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class WeightSumValidator implements ConstraintValidator<ValidWeightSum, VariantWeights> {

  @Override
  public void initialize(ValidWeightSum constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(VariantWeights variantWeights, ConstraintValidatorContext context) {
    if (variantWeights == null) {
      return true; // Null values are handled by @NotNull
    }

    // Only validate CohortVariantWeights as ManualVariantWeights don't have numeric weights
    if (variantWeights instanceof CohortVariantWeights) {
      CohortVariantWeights cohortWeights = (CohortVariantWeights) variantWeights;
      Map<String, Double> weights = cohortWeights.getWeights();

      if (weights == null || weights.isEmpty()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Weights map cannot be null or empty")
            .addConstraintViolation();
        return false;
      }

      // Calculate sum of all weights
      double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();

      // Allow small floating point tolerance (0.01)
      if (Math.abs(sum - 100.0) > 0.01) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                String.format(
                    "Sum of weights must equal 100. Current sum: %.2f. Weights: %s", sum, weights))
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
