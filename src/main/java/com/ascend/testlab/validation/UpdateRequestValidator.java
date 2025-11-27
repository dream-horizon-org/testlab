package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for {@link ValidUpdateRequest} annotation.
 *
 * <p>Validates that non-updatable fields are not present (i.e., are null) in the update request.
 * Uses reflection to check the specified fields dynamically.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class UpdateRequestValidator implements ConstraintValidator<ValidUpdateRequest, Object> {

  private String[] nonUpdatableFields;
  private String message;

  @Override
  public void initialize(ValidUpdateRequest constraintAnnotation) {
    this.nonUpdatableFields = constraintAnnotation.nonUpdatableFields();
    this.message = constraintAnnotation.message();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Null objects are considered valid
    }

    List<String> violatedFields = new ArrayList<>();

    // Check each non-updatable field
    for (String fieldName : nonUpdatableFields) {
      try {
        Field field = value.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object fieldValue = field.get(value);

        // If the field is not null, it means the client is trying to update it
        if (fieldValue != null) {
          violatedFields.add(fieldName);
        }
      } catch (NoSuchFieldException e) {
        // Field doesn't exist in the class, skip it
        continue;
      } catch (IllegalAccessException e) {
        log.error(
            "Failed to access field '{}' during validation: {}", fieldName, e.getMessage(), e);
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Internal validation error: Failed to access field '" + fieldName + "'")
            .addConstraintViolation();
        return false;
      }
    }

    // If any non-updatable fields were provided, validation fails
    if (!violatedFields.isEmpty()) {
      context.disableDefaultConstraintViolation();
      String errorMessage =
          String.format(
              "The following fields cannot be updated: %s. Attempted to update: %s",
              String.join(", ", nonUpdatableFields), String.join(", ", violatedFields));
      context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
      return false;
    }

    return true;
  }
}
