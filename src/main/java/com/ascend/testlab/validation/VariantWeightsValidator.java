package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidVariantWeights;
import com.ascend.testlab.variantWeights.CohortVariantWeights;
import com.ascend.testlab.variantWeights.StratifiedVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for ValidVariantWeights annotation.
 *
 * <p>Validates that VariantWeights has a non-empty weights map.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariantWeightsValidator
    implements ConstraintValidator<ValidVariantWeights, VariantWeights> {

  @Override
  public void initialize(ValidVariantWeights constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(VariantWeights value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Use @NotNull for null checks
    }

    // Validate based on the type
    if (value instanceof CohortVariantWeights) {
      CohortVariantWeights cohortWeights = (CohortVariantWeights) value;
      if (cohortWeights.getWeights() == null || cohortWeights.getWeights().isEmpty()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Cohort variant weights must contain at least one cohort with weight")
            .addConstraintViolation();
        return false;
      }
      // Validate that all weights are positive
      for (Double weight : cohortWeights.getWeights().values()) {
        if (weight == null || weight < 0 || weight > 100) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate("All cohort weights must be between 0 and 100")
              .addConstraintViolation();
          return false;
        }
      }
    } else if (value instanceof StratifiedVariantWeights) {
      StratifiedVariantWeights manualWeights = (StratifiedVariantWeights) value;
      if (manualWeights.getWeights() == null || manualWeights.getWeights().isEmpty()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Manual variant weights must contain at least one variant with user assignments")
            .addConstraintViolation();
        return false;
      }
      // Validate that each variant has at least one user
      for (var entry : manualWeights.getWeights().entrySet()) {
        if (entry.getValue() == null || entry.getValue().isEmpty()) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate(
                  "Each variant must have at least one user assigned")
              .addConstraintViolation();
          return false;
        }
      }
    }

    return true;
  }
}
