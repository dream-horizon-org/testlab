package com.ascend.testlab.validation.annotations;

import com.ascend.testlab.validation.UpdateRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure that non-updatable fields are not present in an update request.
 *
 * <p>This annotation validates that fields marked as non-updatable (such as primary keys,
 * auto-managed fields, and immutable fields) are not included in the update request.
 *
 * <p>The list of non-updatable fields is defined in {@link
 * com.ascend.testlab.constants.Constants#NON_UPDATABLE_FIELDS}.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;ValidUpdateRequest
 * public class UpdateExperimentRequest {
 *   // fields...
 * }
 * </pre>
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

  /**
   * The error message to display when validation fails.
   *
   * @return the error message
   */
  String message() default
      "The following fields cannot be updated: name, experiment_key, project_key, experiment_id, created_by, created_at";

  /**
   * Validation groups.
   *
   * @return the groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload for clients to assign custom payload objects to a constraint.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * The names of the fields to check for non-updatable values.
   *
   * <p>These field names should correspond to the JSON property names in the DTO.
   *
   * @return array of field names to validate
   */
  String[] nonUpdatableFields() default {
    "name", "experimentKey", "projectKey", "experimentId", "createdBy", "createdAt"
  };
}
