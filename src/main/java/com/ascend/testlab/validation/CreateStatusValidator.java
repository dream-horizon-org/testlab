package com.ascend.testlab.validation;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.validation.annotations.ValidCreateStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to ensure experiment can only be created with LIVE or DRAFT status.
 *
 * <p>This validator checks that the status field is either LIVE or DRAFT during experiment
 * creation. Other statuses like PAUSED, CONCLUDED, TERMINATED are not allowed for new experiments
 * as they represent states that can only be reached through updates.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class CreateStatusValidator
    implements ConstraintValidator<ValidCreateStatus, ExperimentStatus> {

  @Override
  public void initialize(ValidCreateStatus constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(ExperimentStatus status, ConstraintValidatorContext context) {
    if (status == null) {
      return true; // Null values are handled by @NotNull
    }

    // Only LIVE and DRAFT are allowed for new experiments
    boolean isValid = status == ExperimentStatus.LIVE || status == ExperimentStatus.DRAFT;

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Cannot create experiment with status '%s'. Only LIVE or DRAFT status is allowed for new experiments. "
                      + "Statuses like PAUSED, CONCLUDED, and TERMINATED can only be set through updates.",
                  status))
          .addConstraintViolation();
    }

    return isValid;
  }
}
