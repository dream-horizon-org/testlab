package com.ascend.testlab.annotations.validator;

import com.ascend.testlab.annotations.ValidEnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Validator for the EnumValue annotation.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class EnumValueValidator implements ConstraintValidator<ValidEnumValue, Object> {
  private Class<? extends Enum<?>> enumClass;
  private String methodName;

  @Override
  public void initialize(ValidEnumValue annotation) {
    this.enumClass = annotation.enumClass();
    this.methodName = annotation.method();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return invokeMethodAndValidateValue(value);
  }

  private boolean invokeMethodAndValidateValue(Object value) {
    try {
      Method method = enumClass.getMethod(methodName);
      return validateValue(method, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Validates if the value matches any of the enum constant's method results.
   *
   * <p>Streams through all enum constants, invokes the specified method on each, and checks if any
   * result equals the provided value.
   *
   * @param method the method to invoke on each enum constant
   * @param value the value to compare against method results
   * @return true if the value matches any enum constant's method result, false otherwise
   */
  private boolean validateValue(Method method, Object value) {
    return Arrays.stream(enumClass.getEnumConstants())
        .map(
            enumConstant -> {
              try {
                return method.invoke(enumConstant);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .anyMatch(enumValue -> enumValue.equals(value));
  }
}
