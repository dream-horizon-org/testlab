package com.ascend.testlab.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for VariantWeights to ensure weights map is not empty.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VariantWeightsValidator.class)
@Documented
public @interface ValidVariantWeights {

  String message() default "Variant weights must contain at least one entry";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
