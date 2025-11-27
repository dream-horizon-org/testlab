package com.ascend.testlab.annotations;

import com.ascend.testlab.annotations.validator.CreateExperimentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateExperimentValidator.class)
public @interface ValidCreateExperiment {
  String message() default "Invalid experiment request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
