package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.ConditionValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to validate condition value based on operandDataType.
 *
 * <p>This annotation validates that the value field matches the expected format for the given
 * operandDataType (e.g., "true"/"false" for BOOL, numeric for NUMBER, etc.).
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionValueValidator.class)
@Documented
public @interface ValidConditionValue {

  /**
   * Error message to display when validation fails.
   *
   * @return the error message
   */
  String message() default "Invalid condition value for the given operandDataType";

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
