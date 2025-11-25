package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidManualVariantKeys;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator for manual variant keys and user assignments.
 *
 * <p>Validates that:
 *
 * <ul>
 *   <li>Keys follow the pattern: 'control', 'variant1', 'variant2', etc. in sequential order
 *   <li>Each variant has at least one user assigned
 *   <li>User IDs are not blank
 *   <li>No user is assigned to multiple variants (no duplicates)
 * </ul>
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class ManualVariantKeysValidator
    implements ConstraintValidator<ValidManualVariantKeys, Map<String, List<String>>> {

  private static final String CONTROL_KEY = "control";
  private static final Pattern VARIANT_KEY_PATTERN = Pattern.compile("^variant(\\d+)$");

  @Override
  public boolean isValid(Map<String, List<String>> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }

    // Validate keys
    if (!value.containsKey(CONTROL_KEY)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Manual variant weights must contain a 'control' key as the first variant")
          .addConstraintViolation();
      return false;
    }

    int expectedNumber = 1;
    for (String key : value.keySet()) {
      if (key == null || key.isBlank()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Manual variant key cannot be null or blank")
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
                "Manual variant key '"
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
                "Manual variant keys must be sequential starting from 1. Expected 'variant"
                    + expectedNumber
                    + "' but found '"
                    + key
                    + "'")
            .addConstraintViolation();
        return false;
      }

      expectedNumber++;
    }

    // Validate user assignments
    Set<String> allUserIds = new HashSet<>();
    for (Map.Entry<String, List<String>> entry : value.entrySet()) {
      String variantKey = entry.getKey();
      List<String> userIds = entry.getValue();

      // Check if user list is null or empty
      if (userIds == null || userIds.isEmpty()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Variant '"
                    + variantKey
                    + "' must have at least one user assigned. User list cannot be empty.")
            .addConstraintViolation();
        return false;
      }

      // Check each user ID
      for (String userId : userIds) {
        if (userId == null || userId.isBlank()) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate(
                  "User ID in variant '" + variantKey + "' cannot be null or blank")
              .addConstraintViolation();
          return false;
        }

        // Check for duplicate user IDs across variants
        if (allUserIds.contains(userId)) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate(
                  "User ID '"
                      + userId
                      + "' is assigned to multiple variants. Each user can only be assigned to one variant.")
              .addConstraintViolation();
          return false;
        }

        allUserIds.add(userId);
      }
    }

    return true;
  }
}
