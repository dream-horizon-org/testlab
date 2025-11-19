package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidMetrics;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validator for metrics map structure.
 *
 * <p>Validates that:
 *
 * <ul>
 *   <li>Map contains exactly 'primary' and 'secondary' keys
 *   <li>Both keys are present
 *   <li>Values are non-empty lists
 * </ul>
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class MetricsValidator
    implements ConstraintValidator<ValidMetrics, Map<String, List<String>>> {

  private static final Set<String> REQUIRED_KEYS = Set.of("primary", "secondary");

  @Override
  public boolean isValid(Map<String, List<String>> metrics, ConstraintValidatorContext context) {
    if (metrics == null) {
      return true; // Use @NotNull for null checks
    }

    context.disableDefaultConstraintViolation();

    // Check if map has exactly 2 keys
    if (metrics.size() != 2) {
      context
          .buildConstraintViolationWithTemplate(
              "Metrics must contain exactly 2 keys: 'primary' and 'secondary'")
          .addConstraintViolation();
      return false;
    }

    // Check if both required keys are present
    if (!metrics.keySet().equals(REQUIRED_KEYS)) {
      context
          .buildConstraintViolationWithTemplate(
              "Metrics must contain only 'primary' and 'secondary' keys. Found: "
                  + metrics.keySet())
          .addConstraintViolation();
      return false;
    }

    // Check if primary metrics list is not empty
    List<String> primaryMetrics = metrics.get("primary");
    if (primaryMetrics == null || primaryMetrics.isEmpty()) {
      context
          .buildConstraintViolationWithTemplate("Primary metrics list cannot be empty")
          .addConstraintViolation();
      return false;
    }

    // Check if secondary metrics list is not empty
    List<String> secondaryMetrics = metrics.get("secondary");
    if (secondaryMetrics == null || secondaryMetrics.isEmpty()) {
      context
          .buildConstraintViolationWithTemplate("Secondary metrics list cannot be empty")
          .addConstraintViolation();
      return false;
    }

    // Check if any metric value is blank
    for (String metric : primaryMetrics) {
      if (metric == null || metric.trim().isEmpty()) {
        context
            .buildConstraintViolationWithTemplate("Primary metrics cannot contain blank values")
            .addConstraintViolation();
        return false;
      }
    }

    for (String metric : secondaryMetrics) {
      if (metric == null || metric.trim().isEmpty()) {
        context
            .buildConstraintViolationWithTemplate("Secondary metrics cannot contain blank values")
            .addConstraintViolation();
        return false;
      }
    }

    return true;
  }
}
