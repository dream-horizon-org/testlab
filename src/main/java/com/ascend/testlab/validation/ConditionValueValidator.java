package com.ascend.testlab.validation;

import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.validation.annotations.ValidConditionValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for ValidConditionValue annotation.
 *
 * <p>Validates that the condition value matches the expected format based on operandDataType: -
 * BOOL: "true" or "false" (case-insensitive) - NUMBER: valid integer - DECIMAL: valid decimal
 * number - STRING: any non-empty string - SEMVER_STRING: semantic version format (e.g., "1.2.3") -
 * OBJECT: valid JSON object string - LIST: valid JSON array string
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class ConditionValueValidator
    implements ConstraintValidator<ValidConditionValue, RuleAttributes.Condition> {

  private static final Pattern SEMVER_PATTERN =
      Pattern.compile(
          "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

  @Override
  public void initialize(ValidConditionValue constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(RuleAttributes.Condition condition, ConstraintValidatorContext context) {
    if (condition == null) {
      return true; // Null values are handled by @NotNull
    }

    String operandDataType = condition.getOperandDataType();
    String value = condition.getValue();

    // If either field is null or empty, let @NotNull/@NotEmpty handle it
    if (operandDataType == null || operandDataType.isEmpty() || value == null || value.isEmpty()) {
      return true;
    }

    boolean isValid = validateValueForType(operandDataType, value);

    if (!isValid) {
      // Customize error message based on type
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Invalid value '%s' for operandDataType '%s'. Expected format: %s",
                  value, operandDataType, getExpectedFormat(operandDataType)))
          .addPropertyNode("value")
          .addConstraintViolation();
    }

    return isValid;
  }

  /**
   * Validates the value based on the operandDataType.
   *
   * @param operandDataType the data type
   * @param value the value to validate
   * @return true if valid, false otherwise
   */
  private boolean validateValueForType(String operandDataType, String value) {
    switch (operandDataType.toUpperCase()) {
      case "BOOL":
        return validateBoolean(value);
      case "NUMBER":
        return validateNumber(value);
      case "DECIMAL":
        return validateDecimal(value);
      case "STRING":
        return validateString(value);
      case "SEMVER_STRING":
        return validateSemVer(value);
      case "OBJECT":
        return validateObject(value);
      case "LIST":
        return validateList(value);
      default:
        // If unknown type, allow it (let other validators handle it)
        return true;
    }
  }

  /**
   * Validates boolean value (true/false, case-insensitive).
   *
   * @param value the value to validate
   * @return true if valid boolean
   */
  private boolean validateBoolean(String value) {
    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
  }

  /**
   * Validates integer number.
   *
   * @param value the value to validate
   * @return true if valid integer
   */
  private boolean validateNumber(String value) {
    try {
      Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Validates decimal number.
   *
   * @param value the value to validate
   * @return true if valid decimal
   */
  private boolean validateDecimal(String value) {
    try {
      Double.parseDouble(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Validates string (any non-empty string is valid).
   *
   * @param value the value to validate
   * @return true if non-empty string
   */
  private boolean validateString(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Validates semantic version string (e.g., "1.2.3", "2.0.0-beta.1").
   *
   * @param value the value to validate
   * @return true if valid semver
   */
  private boolean validateSemVer(String value) {
    return SEMVER_PATTERN.matcher(value).matches();
  }

  /**
   * Validates JSON object string (basic check for object structure).
   *
   * @param value the value to validate
   * @return true if looks like JSON object
   */
  private boolean validateObject(String value) {
    String trimmed = value.trim();
    // Basic check: should start with { and end with }
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
      return false;
    }
    // Try to validate it's properly balanced
    return isValidJson(trimmed);
  }

  /**
   * Validates JSON array string (basic check for array structure).
   *
   * @param value the value to validate
   * @return true if looks like JSON array
   */
  private boolean validateList(String value) {
    String trimmed = value.trim();
    // Basic check: should start with [ and end with ]
    if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
      return false;
    }
    // Try to validate it's properly balanced
    return isValidJson(trimmed);
  }

  /**
   * Basic JSON validation (checks for balanced braces/brackets).
   *
   * @param json the JSON string to validate
   * @return true if balanced
   */
  private boolean isValidJson(String json) {
    int braceCount = 0;
    int bracketCount = 0;
    boolean inString = false;
    boolean escaped = false;

    for (char c : json.toCharArray()) {
      if (escaped) {
        escaped = false;
        continue;
      }

      if (c == '\\') {
        escaped = true;
        continue;
      }

      if (c == '"') {
        inString = !inString;
        continue;
      }

      if (!inString) {
        if (c == '{') braceCount++;
        else if (c == '}') braceCount--;
        else if (c == '[') bracketCount++;
        else if (c == ']') bracketCount--;

        if (braceCount < 0 || bracketCount < 0) {
          return false;
        }
      }
    }

    return braceCount == 0 && bracketCount == 0 && !inString;
  }

  /**
   * Gets the expected format description for the given data type.
   *
   * @param operandDataType the data type
   * @return description of expected format
   */
  private String getExpectedFormat(String operandDataType) {
    switch (operandDataType.toUpperCase()) {
      case "BOOL":
        return "true or false";
      case "NUMBER":
        return "integer number (e.g., 123, -456)";
      case "DECIMAL":
        return "decimal number (e.g., 123.45, -67.89)";
      case "STRING":
        return "any non-empty string";
      case "SEMVER_STRING":
        return "semantic version (e.g., 1.2.3, 2.0.0-beta.1)";
      case "OBJECT":
        return "JSON object (e.g., {\"key\": \"value\"})";
      case "LIST":
        return "JSON array (e.g., [\"item1\", \"item2\"])";
      default:
        return "valid value for " + operandDataType;
    }
  }
}
