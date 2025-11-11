package com.ascend.testlab.annotations;

import com.ascend.testlab.annotations.validator.EnumValueValidator;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {
  String message() default ErrorMessages.INVALID_ENUM_VALUE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  Class<? extends Enum<?>> enumClass();

  String method() default "name";
}
