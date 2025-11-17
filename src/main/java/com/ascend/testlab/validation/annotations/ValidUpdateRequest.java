package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.UpdateRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that only updatable fields are present in the update request.
 *
 * <p>Ensures that non-updatable fields like name, experiment_key, etc. are not included in the
 * update request.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UpdateRequestValidator.class)
@Documented
public @interface ValidUpdateRequest {
  String message() default "Request contains non-updatable fields";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
