package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.ManualVariantKeysValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for manual variant weight keys.
 *
 * <p>Validates that the keys in the manual weights map follow the pattern: 'control', 'variant1',
 * 'variant2', etc. in sequential order.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ManualVariantKeysValidator.class)
@Documented
public @interface ValidManualVariantKeys {
  String message() default
      "Manual variant keys must follow the pattern 'control', 'variant1', 'variant2', etc. in increasing sequential order";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
