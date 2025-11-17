package com.ascend.testlab.validation;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.validation.annotations.ValidUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validator for UpdateExperimentRequest to ensure only updatable fields are present.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public class UpdateRequestValidator
    implements ConstraintValidator<ValidUpdateRequest, UpdateExperimentRequest> {

  @Override
  public void initialize(ValidUpdateRequest constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(UpdateExperimentRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Let @NotNull handle null validation
    }

    // Convert DTO to Map to check which fields are present
    ObjectMapper mapper = new ObjectMapper();
    try {
      String jsonString = mapper.writeValueAsString(request);
      @SuppressWarnings("unchecked")
      Map<String, Object> requestMap = mapper.readValue(jsonString, Map.class);

      // Remove null values
      requestMap.values().removeIf(value -> value == null);

      // Find non-updatable fields that are present in the request
      List<String> foundNonUpdatableFields =
          requestMap.keySet().stream()
              .filter(Constants.NON_UPDATABLE_FIELDS::contains)
              .collect(Collectors.toList());

      if (!foundNonUpdatableFields.isEmpty()) {
        // Disable default constraint violation
        context.disableDefaultConstraintViolation();

        // Build custom error message
        String errorMessage =
            String.format(
                "The following fields cannot be updated: %s",
                String.join(", ", foundNonUpdatableFields));

        context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();

        return false;
      }

      return true;
    } catch (Exception e) {
      // If serialization fails, let it pass and let other validations handle it
      return true;
    }
  }
}
