package com.ascend.testlab.validation;

import com.ascend.testlab.dto.entity.experiment.Variables;
import com.ascend.testlab.validation.annotations.ValidVariableDataType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to ensure dataType matches the actual value type in Variables.
 *
 * <p>This validator checks that: - NUMBER: value is a valid integer - DECIMAL: value is a valid
 * double - BOOL: value is "true" or "false" - STRING: value is a string - OBJECT/LIST: no specific
 * validation
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class VariableDataTypeValidator
    implements ConstraintValidator<ValidVariableDataType, Variables> {

  @Override
  public void initialize(ValidVariableDataType constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Variables variable, ConstraintValidatorContext context) {
    if (variable == null) {
      return true; // Let @NotNull handle null validation
    }

    String value = variable.getValue();
    String dataType = variable.getDataType();

    // If either is null, skip validation (let @NotBlank handle it)
    if (value == null || dataType == null || dataType.isEmpty()) {
      return true;
    }

    boolean isValid = true;
    String errorMessage = null;

    switch (dataType.toUpperCase()) {
      case "NUMBER":
        // Check if value is a valid integer
        try {
          Long.parseLong(value);
        } catch (NumberFormatException e) {
          isValid = false;
          errorMessage =
              String.format(
                  "DataType is NUMBER but value '%s' is not a valid integer for key '%s'",
                  value, variable.getKey());
        }
        break;

      case "DECIMAL":
        // Check if value is a valid double
        try {
          Double.parseDouble(value);
        } catch (NumberFormatException e) {
          isValid = false;
          errorMessage =
              String.format(
                  "DataType is DECIMAL but value '%s' is not a valid decimal number for key '%s'",
                  value, variable.getKey());
        }
        break;

      case "BOOL":
        // Check if value is true or false
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
          isValid = false;
          errorMessage =
              String.format(
                  "DataType is BOOL but value '%s' is not 'true' or 'false' for key '%s'",
                  value, variable.getKey());
        }
        break;

      case "STRING":
      case "SEMVER_STRING":
      case "OBJECT":
      case "LIST":
        // No specific validation for these types
        break;

      default:
        // Unknown dataType - let @ValidEnumValue handle it
        break;
    }

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
    }

    return isValid;
  }
}
