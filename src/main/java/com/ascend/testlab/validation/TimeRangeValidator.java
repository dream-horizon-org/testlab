package com.ascend.testlab.validation;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.validation.annotations.ValidTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to ensure end_time is greater than start_time in experiment creation.
 *
 * <p>This validator checks that: - Both start_time and end_time are provided - end_time is strictly
 * greater than start_time
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class TimeRangeValidator
    implements ConstraintValidator<ValidTimeRange, CreateExperimentRequest> {

  @Override
  public void initialize(ValidTimeRange constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Let @NotNull handle null validation
    }

    long startTime = request.getStartTime();
    long endTime = request.getEndTime();

    // If both are 0 or not set, skip validation
    if (startTime == 0 && endTime == 0) {
      return true;
    }

    // If only one is set, that's valid (optional fields)
    if (startTime == 0 || endTime == 0) {
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
  }
}
