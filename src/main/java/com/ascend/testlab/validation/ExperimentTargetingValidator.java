package com.ascend.testlab.validation;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.validation.annotations.ValidExperimentTargeting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator to ensure that either cohorts or rule_attributes is provided in experiment creation.
 *
 * <p>This validator checks that at least one targeting mechanism is specified: - Cohorts: List of
 * user cohorts to target - Rule Attributes: List of rules with conditions to target users
 *
 * <p>Both cannot be empty/null at the same time as the experiment needs a targeting mechanism.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class ExperimentTargetingValidator
    implements ConstraintValidator<ValidExperimentTargeting, CreateExperimentRequest> {

  @Override
  public void initialize(ValidExperimentTargeting constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(CreateExperimentRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Let @NotNull handle null validation
    }

    boolean hasValidCohorts = request.getCohorts() != null && !request.getCohorts().isEmpty();
    boolean hasValidRuleAttributes =
        request.getRuleAttributes() != null && !request.getRuleAttributes().isEmpty();

    // At least one must be provided
    if (!hasValidCohorts && !hasValidRuleAttributes) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Either cohorts or rule_attributes must be provided for experiment targeting")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
