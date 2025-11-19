package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.CreateStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure experiment can only be created with LIVE or DRAFT status.
 *
 * <p>This annotation validates that the status field is either LIVE or DRAFT during experiment
 * creation. Other statuses like PAUSED, CONCLUDED, TERMINATED are not allowed for new experiments.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateStatusValidator.class)
@Documented
public @interface ValidCreateStatus {
  String message() default
      "Experiment can only be created with status LIVE or DRAFT. Other statuses are not allowed for new experiments.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
