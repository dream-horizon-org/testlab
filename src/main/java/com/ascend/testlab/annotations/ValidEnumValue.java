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
 * Generic validation annotation for enum values.
 *
 * <p>This annotation can be used to validate that a field value matches one of the valid enum
 * values. It supports custom error messages and can invoke any method on the enum (like getName(),
 * getValue(), etc.) to compare against the field value.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;ValidEnumValue(
 *     enumClass = ExperimentStatus.class,
 *     method = "getName",
 *     message = "Invalid experiment status"
 * )
 * private ExperimentStatus status;
 * </pre>
 */
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {

  /**
   * The error message to display when validation fails. Defaults to a generic invalid enum value
   * message.
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
   * Payload for clients to assign custom payload objects to a constraint.
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
   * < The method name to invoke on the enum to get the value to compare. Defaults to "name" which
   * returns the enum constant name.
   *
   * <p>Common methods:
   *
   * <ul>
   *   <li>"name" - returns the enum constant name (default)
   *   <li>"getName" - custom getter method
   *   <li>"getValue" - custom getter method
   *   <li>"getType" - custom getter method
   * </ul>
   *
   * @return the method name
   */
  String method() default "name";
}
