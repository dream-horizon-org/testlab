package com.ascend.testlab.annotations.validator;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Generic validator for enum values.
 *
 * <p>This validator checks if a field value matches one of the valid enum values by invoking a
 * specified method on the enum constants and comparing the results.
 *
 * <p>It supports:
 *
 * <ul>
 *   <li>Direct enum instance validation
 *   <li>String value validation against enum method results
 *   <li>Custom error messages with valid enum values listed
 * </ul>
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class EnumValueValidator implements ConstraintValidator<ValidEnumValue, Object> {
  private Class<? extends Enum<?>> enumClass;
  private String methodName;
  private String customMessage;

  @Override
  public void initialize(ValidEnumValue annotation) {
    this.enumClass = annotation.enumClass();
    this.methodName = annotation.method();
    this.customMessage = annotation.message();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Null values are handled by @NotNull
    }

    // If the value is already an enum instance of the expected type, it's valid
    if (enumClass.isInstance(value)) {
      return true;
    }

    // Otherwise, validate string value against enum method results
    boolean isValid = invokeMethodAndValidateValue(value);

    // If invalid, provide a detailed error message with valid values
    if (!isValid) {
      context.disableDefaultConstraintViolation();
      String validValues = getValidEnumValues();
      String errorMessage =
          String.format(
              "%s. Received: '%s'. Valid values are: [%s]", customMessage, value, validValues);
      context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
    }

    return isValid;
  }

  /**
   * Invokes the specified method on all enum constants and validates if the value matches any
   * result.
   *
   * @param value the value to validate
   * @return true if the value matches any enum constant's method result
   */
  private boolean invokeMethodAndValidateValue(Object value) {
    try {
      Method method = enumClass.getMethod(methodName);
      return validateValue(method, value);
    } catch (Exception e) {
      throw new RestException(ErrorEnum.ENUM_VALIDATION_FAILED, e);
    }
  }

  /**
   * Validates if the value matches any of the enum constant's method results.
   *
   * @param method the method to invoke on enum constants
   * @param value the value to validate
   * @return true if the value matches any result
   */
  private boolean validateValue(Method method, Object value) {
    return Arrays.stream(enumClass.getEnumConstants())
        .map(
            enumConstant -> {
              try {
                return method.invoke(enumConstant);
              } catch (Exception e) {
                throw new RestException(ErrorEnum.ENUM_VALIDATION_FAILED, e);
              }
            })
        .anyMatch(enumValue -> enumValue.equals(value));
  }

  /**
   * Gets a comma-separated string of all valid enum values.
   *
   * @return comma-separated valid enum values
   */
  private String getValidEnumValues() {
    try {
      Method method = enumClass.getMethod(methodName);
      return Arrays.stream(enumClass.getEnumConstants())
          .map(
              enumConstant -> {
                try {
                  Object result = method.invoke(enumConstant);
                  return result != null ? result.toString() : "null";
                } catch (Exception e) {
                  return enumConstant.toString();
                }
              })
          .collect(Collectors.joining(", "));
    } catch (Exception e) {
      // Fallback to enum constant names
      return Arrays.stream(enumClass.getEnumConstants())
          .map(Enum::name)
          .collect(Collectors.joining(", "));
    }
  }
}
