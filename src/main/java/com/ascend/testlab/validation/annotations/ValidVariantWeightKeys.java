package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.VariantWeightKeysValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure variant keys in variants match with variant_weights keys.
 *
 * <p>This annotation validates that: - All variant keys in variants exist in variant_weights - All
 * variant keys in variant_weights exist in variants - The order of keys is the same in both
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VariantWeightKeysValidator.class)
public @interface ValidVariantWeightKeys {
  String message() default
      "Variant keys in variants must match with variant_weights keys in the same order";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
