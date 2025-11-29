package com.ascend.testlab.annotations;

import com.ascend.testlab.annotations.validator.CreateExperimentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for create experiment requests.
 *
 * <p>Validates the entire CreateExperimentRequest object for consistency.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateExperimentValidator.class)
public @interface ValidCreateExperiment {

  /**
   * The error message to return when validation fails.
   *
   * @return the error message
   */
  String message() default "Invalid experiment request";

  /**
   * The validation groups this constraint belongs to.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * The payload associated with this constraint.
   *
   * @return the payload classes
   */
  Class<? extends Payload>[] payload() default {};
}
