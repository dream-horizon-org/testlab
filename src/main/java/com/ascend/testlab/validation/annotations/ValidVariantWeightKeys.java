package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.VariantWeightKeysValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for variant weight keys.
 *
 * <p>Validates that the keys in the weights map follow the pattern: 'control', 'variant1',
 * 'variant2', etc. in sequential order.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VariantWeightKeysValidator.class)
@Documented
public @interface ValidVariantWeightKeys {
  String message() default
      "Variant weight keys must follow the pattern 'control', 'variant1', 'variant2', etc. in increasing sequential order";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
