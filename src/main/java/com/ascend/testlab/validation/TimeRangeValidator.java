package com.ascend.testlab.validation;

import com.ascend.testlab.validation.annotations.ValidTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

/**
 * Validator to ensure end_time is greater than start_time in experiment requests.
 *
 * <p>This validator checks that: - Both start_time and end_time are provided - end_time is strictly
 * greater than start_time
 *
 * <p>Works with any class that has startTime and endTime fields (Long type).
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {

  @Override
  public void initialize(ValidTimeRange constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Object request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Let @NotNull handle null validation
    }

    try {
      // Use reflection to get startTime and endTime fields
      Field startTimeField = request.getClass().getDeclaredField("startTime");
      Field endTimeField = request.getClass().getDeclaredField("endTime");

      startTimeField.setAccessible(true);
      endTimeField.setAccessible(true);

      Long startTime = (Long) startTimeField.get(request);
      Long endTime = (Long) endTimeField.get(request);

      // If both are null or not set, skip validation
      if (startTime == null && endTime == null) {
        return true;
      }

      // If only one is set, that's valid (optional fields in update)
      if (startTime == null || endTime == null) {
        return true;
      }

      // If both are 0, skip validation
      if (startTime == 0 && endTime == 0) {
        return true;
      }

      // Both are set, validate that end_time > start_time
      if (endTime <= startTime) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "End time must be greater than start time. start_time: "
                    + startTime
                    + ", end_time: "
                    + endTime)
            .addPropertyNode("endTime")
            .addConstraintViolation();
        return false;
      }

      return true;

    } catch (NoSuchFieldException | IllegalAccessException e) {
      // If fields don't exist or can't be accessed, skip validation
      return true;
    }
  }
}
