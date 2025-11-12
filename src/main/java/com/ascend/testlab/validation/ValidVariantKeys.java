package com.ascend.testlab.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for variant map keys.
 *
 * <p>Validates that variant keys follow the naming convention: control_variant1, control_variant2,
 * etc. in increasing sequential order starting from 1.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VariantKeysValidator.class)
@Documented
public @interface ValidVariantKeys {
  String message() default
      "Variant keys must follow the pattern 'control_variant1', 'control_variant2', etc. in increasing sequential order";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
