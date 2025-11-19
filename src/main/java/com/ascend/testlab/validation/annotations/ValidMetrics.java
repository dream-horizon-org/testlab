package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.MetricsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that metrics map contains only 'primary' and 'secondary' keys.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MetricsValidator.class)
@Documented
public @interface ValidMetrics {
  String message() default "Metrics must contain exactly 'primary' and 'secondary' keys";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
