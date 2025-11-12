package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidVariantWeightKeys;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator for variant weight keys.
 *
 * <p>Validates that the keys in the weights map follow the pattern: 'control', 'variant1',
 * 'variant2', etc. in sequential order.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariantWeightKeysValidator
    implements ConstraintValidator<ValidVariantWeightKeys, Map<String, Double>> {

  private static final String CONTROL_KEY = "control";
  private static final Pattern VARIANT_KEY_PATTERN = Pattern.compile("^variant(\\d+)$");

  @Override
  public boolean isValid(Map<String, Double> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }

    if (!value.containsKey(CONTROL_KEY)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Variant weights must contain a 'control' key as the first variant")
          .addConstraintViolation();
      return false;
    }

    int expectedNumber = 1;
    for (String key : value.keySet()) {
      if (key == null || key.isBlank()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Variant weight key cannot be null or blank")
            .addConstraintViolation();
        return false;
      }

      if (key.equals(CONTROL_KEY)) {
        continue;
      }

      Matcher matcher = VARIANT_KEY_PATTERN.matcher(key);
      if (!matcher.matches()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant weight key '"
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
                "Variant weight keys must be sequential starting from 1. Expected 'variant"
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
