package com.ascend.testlab.validation;

import com.ascend.testlab.entity.Variant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator for variant map keys.
 *
 * <p>Ensures that variant keys follow the naming convention: control_variant1, control_variant2,
 * etc. in increasing sequential order starting from 1.
 *
 * <p>Validation rules:
 *
 * <ul>
 *   <li>Keys must match the pattern: control_variant{number}
 *   <li>Numbers must start from 1 and be sequential (1, 2, 3, ...)
 *   <li>No gaps in the sequence are allowed
 *   <li>Keys must be in sorted order
 * </ul>
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariantKeysValidator
    implements ConstraintValidator<ValidVariantKeys, Map<String, Variant>> {

  private static final Pattern VARIANT_KEY_PATTERN = Pattern.compile("^control_variant(\\d+)$");

  @Override
  public void initialize(ValidVariantKeys constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Map<String, Variant> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true; // @NotNull and @NotEmpty handle nullability and emptiness
    }

    // Extract and validate all keys
    int expectedNumber = 1;
    for (String key : value.keySet()) {
      if (key == null || key.isBlank()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Variant key cannot be null or blank")
            .addConstraintViolation();
        return false;
      }

      Matcher matcher = VARIANT_KEY_PATTERN.matcher(key);
      if (!matcher.matches()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant key '"
                    + key
                    + "' does not match the required pattern 'control_variant{number}'")
            .addConstraintViolation();
        return false;
      }

      int number = Integer.parseInt(matcher.group(1));
      if (number != expectedNumber) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant keys must be sequential starting from 1. Expected 'control_variant"
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
