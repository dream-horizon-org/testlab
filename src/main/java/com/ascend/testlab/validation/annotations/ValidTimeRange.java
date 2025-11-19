package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.TimeRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure end_time is greater than start_time.
 *
 * <p>This annotation should be applied at the class level to validate the relationship between
 * start_time and end_time fields.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface ValidTimeRange {
  String message() default "End time must be greater than start time";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
