package com.ascend.testlab.annotations.validator;

import com.ascend.testlab.annotations.ValidEnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;

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

    // If the value is already an enum instance of the expected type, it's valid
    if (enumClass.isInstance(value)) {
      return true;
    }

    // Otherwise, validate string value against enum method results
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
