package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation strategy for experiments in PAUSED state.
 *
 * <p>Blocks all updates when experiment is paused. Only status transitions (to resume or terminate)
 * are allowed, which are handled separately in status transition validation.
 */
@Slf4j
public class PausedStateValidationStrategy implements StateValidationStrategy {

  @Override
  public boolean appliesTo(String currentStatus) {
    if (currentStatus == null) {
      return false;
    }
    try {
      ExperimentStatus status = ExperimentStatus.valueOf(currentStatus);
      return status == ExperimentStatus.PAUSED;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid status '{}', skipping PAUSED state validation", currentStatus);
      return false;
    }
  }

  @Override
  public void validate(UpdateExperimentRequest request, String currentStatus, UUID experimentId) {
    log.debug("Validating PAUSED state restrictions for experimentId: {}", experimentId);

    // Extract non-null fields from request using reflection
    Set<String> attemptedFields = extractAttemptedFields(request);

    // Remove system fields that are always allowed (updated_by is metadata, not experiment data)
    attemptedFields.remove("updated_by");

    // In PAUSED state, only status changes are allowed (handled by status transition validation)
    // All other field updates are blocked
    if (!attemptedFields.isEmpty()) {
      String errorMessage =
          String.format(
              "Cannot update experiment in PAUSED state. "
                  + "Only status transitions are allowed. "
                  + "Attempted to update: %s",
              String.join(", ", attemptedFields));
      log.error(
          "PAUSED state validation failed for experimentId: {}, attempted fields: {}",
          experimentId,
          attemptedFields);
      throw new IllegalArgumentException(errorMessage);
    }

    log.info("PAUSED state validation passed for experimentId: {}", experimentId);
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
}
