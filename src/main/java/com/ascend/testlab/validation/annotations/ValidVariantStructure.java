package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.VariantStructureConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that variant structure updates only modify variable values.
 *
 * <p>This annotation ensures that when updating variants:
 *
 * <ul>
 *   <li>Variant keys remain the same (control, variant1, etc.)
 *   <li>Variable keys remain the same (button_color, feature_enabled, etc.)
 *   <li>Variable data types remain the same (STRING, BOOL, NUMBER, etc.)
 *   <li>Only variable values can be changed
 * </ul>
 *
 * <p>Note: This validator requires access to existing experiment data, which must be provided
 * through the validation context before validation.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Documented
@Constraint(validatedBy = VariantStructureConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVariantStructure {

  /**
   * Error message when validation fails.
   *
   * @return the error message
   */
  String message() default
      "Variant structure cannot be changed. Only variable values can be updated.";

  /**
   * Validation groups.
   *
   * @return the groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload for clients.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};
}
