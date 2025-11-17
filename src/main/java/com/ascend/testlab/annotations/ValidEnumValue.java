package com.ascend.testlab.annotations;

import com.ascend.testlab.annotations.validator.EnumValueValidator;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating enum values in fields, methods, parameters, or annotations. This
 * annotation validates that a given value matches one of the values in the specified enum class.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {
  /**
   * The error message to be used when validation fails.
   *
   * @return the error message
   */
  String message() default ErrorMessages.INVALID_ENUM_VALUE;

  /**
   * The validation groups to which this constraint belongs.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * The payload that can be associated with this constraint.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * The enum class against which the value should be validated.
   *
   * @return the enum class
   */
  Class<? extends Enum<?>> enumClass();

  /**
   * The method name to be used for retrieving the enum value for comparison. Defaults to "name"
   * which uses the enum constant name.
   *
   * @return the method name
   */
  String method() default "name";
}
