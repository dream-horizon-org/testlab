package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.WeightSumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure the sum of weights equals 100.
 *
 * <p>This annotation is used to validate that the weights in variant_weights sum to exactly 100.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WeightSumValidator.class)
@Documented
public @interface ValidWeightSum {
  String message() default "Sum of weights must equal 100";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
