package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation strategy for experiments in LIVE state.
 *
 * <p>Restricts updates to only non-critical fields that don't affect experiment behavior.
 */
@Slf4j
public class LiveStateValidationStrategy implements StateValidationStrategy {

  @Override
  public boolean appliesTo(String currentStatus) {
    if (currentStatus == null) {
      return false;
    }
    try {
      ExperimentStatus status = ExperimentStatus.valueOf(currentStatus);
      return status == ExperimentStatus.LIVE;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid status '{}', skipping LIVE state validation", currentStatus);
      return false;
    }
  }

  @Override
  public void validate(UpdateExperimentRequest request, String currentStatus, UUID experimentId) {
    log.debug("Validating LIVE state field restrictions for experimentId: {}", experimentId);

    // Extract non-null fields from request using reflection
    Set<String> attemptedFields = extractAttemptedFields(request);

    // Check if any restricted fields are being updated
    Set<String> restrictedFields = findRestrictedFields(attemptedFields);

    if (!restrictedFields.isEmpty()) {
      String errorMessage = buildErrorMessage(restrictedFields);
      log.error(
          "LIVE state validation failed for experimentId: {}, restricted fields: {}",
          experimentId,
          restrictedFields);
      throw new IllegalArgumentException(errorMessage);
    }

    log.info(
        "LIVE state field validation passed for experimentId: {}, fields: {}",
        experimentId,
        attemptedFields);
  }

  @Override
  public int getPriority() {
    return 10; // High priority for LIVE state validation
  }

  /**
   * Extracts non-null field names from the update request.
   *
   * @param request the update request
   * @return set of field names that are being updated
   */
  private Set<String> extractAttemptedFields(UpdateExperimentRequest request) {
    Set<String> attemptedFields = new HashSet<>();

    for (Field field : UpdateExperimentRequest.class.getDeclaredFields()) {
      try {
        field.setAccessible(true);
        Object value = field.get(request);

        if (value != null) {
          // Get JSON property name (snake_case)
          JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
          String fieldName = jsonProperty != null ? jsonProperty.value() : field.getName();
          attemptedFields.add(fieldName);
        }
      } catch (IllegalAccessException e) {
        log.warn("Could not access field {} for validation", field.getName());
      }
    }

    return attemptedFields;
  }

  /**
   * Identifies which attempted fields are restricted in LIVE state.
   *
   * @param attemptedFields fields being updated
   * @return set of restricted field names
   */
  private Set<String> findRestrictedFields(Set<String> attemptedFields) {
    Set<String> restrictedFields = new HashSet<>();

    for (String fieldName : attemptedFields) {
      if (Constants.LIVE_STATE_NON_UPDATABLE_FIELDS.contains(fieldName)) {
        restrictedFields.add(fieldName);
      }
    }

    return restrictedFields;
  }

  /**
   * Builds a detailed error message listing restricted fields and allowed fields.
   *
   * @param restrictedFields fields that cannot be updated
   * @return formatted error message
   */
  private String buildErrorMessage(Set<String> restrictedFields) {
    return String.format(
        "Cannot update the following fields in LIVE state: %s. "
            + "Only these fields can be updated in LIVE state: %s",
        String.join(", ", restrictedFields),
        String.join(", ", Constants.LIVE_STATE_UPDATABLE_FIELDS));
  }
}
