package com.ascend.testlab.validation;

import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.validation.annotations.ValidVariantKeys;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator for variant map keys.
 *
 * <p>Ensures that variant keys follow the naming convention: control, variant1, variant2, etc. in
 * increasing sequential order starting from 1.
 *
 * <p>Validation rules:
 *
 * <ul>
 *   <li>First key must be 'control'
 *   <li>Subsequent keys must match the pattern: variant{number}
 *   <li>Numbers must start from 1 and be sequential (1, 2, 3, ...)
 *   <li>No gaps in the sequence are allowed
 * </ul>
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariantKeysValidator
    implements ConstraintValidator<ValidVariantKeys, Map<String, Variant>> {

  private static final String CONTROL_KEY = "control";
  private static final Pattern VARIANT_KEY_PATTERN = Pattern.compile("^variant(\\d+)$");

  @Override
  public void initialize(ValidVariantKeys constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Map<String, Variant> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true; // @NotNull and @NotEmpty handle nullability and emptiness
    }

    // Check if 'control' key exists
    if (!value.containsKey(CONTROL_KEY)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Variants must contain a 'control' key as the first variant")
          .addConstraintViolation();
      return false;
    }

    // Validate other keys
    int expectedNumber = 1;
    for (String key : value.keySet()) {
      if (key == null || key.isBlank()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Variant key cannot be null or blank")
            .addConstraintViolation();
        return false;
      }

      // Skip 'control' key
      if (key.equals(CONTROL_KEY)) {
        continue;
      }

      Matcher matcher = VARIANT_KEY_PATTERN.matcher(key);
      if (!matcher.matches()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant key '"
                    + key
                    + "' does not match the required pattern 'variant{number}' or 'control'")
            .addConstraintViolation();
        return false;
      }

      int number = Integer.parseInt(matcher.group(1));
      if (number != expectedNumber) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant keys must be sequential starting from 1. Expected 'variant"
                    + expectedNumber
                    + "' but found '"
                    + key
                    + "'")
            .addConstraintViolation();
        return false;
      }

      expectedNumber++;
    }

    return true;
  }
}
