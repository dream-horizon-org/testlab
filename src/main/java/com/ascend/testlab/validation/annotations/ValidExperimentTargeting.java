package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.ExperimentTargetingValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure that either cohorts or rule_attributes is provided.
 *
 * <p>At least one of cohorts or rule_attributes must be non-empty for proper experiment targeting.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExperimentTargetingValidator.class)
public @interface ValidExperimentTargeting {
  String message() default "Either cohorts or rule_attributes must be provided";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
