package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variables;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Validates variant-related constraints.
 *
 * <ul>
 *   <li>Variants cannot be removed except in DRAFT mode
 *   <li>Variant naming must be contiguous (control, variant1, variant2, ...)
 *   <li>Variable keys must be consistent across all variants
 *   <li>Existing variable dataType cannot be changed
 *   <li>Variable keys can only be removed in DRAFT mode
 *   <li>Variable keys cannot be added in LIVE or PAUSED mode
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class VariantValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return request.getVariants() != null && !request.getVariants().isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus status = existing.getStatus();
    Map<String, Variant> existingVariants = existing.getVariants();
    Map<String, Variant> requestVariants = request.getVariants();

    // Validate no variant removal except in DRAFT
    validateNoVariantRemoval(status, existingVariants, requestVariants);

    validateContiguousVariantNaming(requestVariants.keySet());

    validateVariableKeys(status, existingVariants, requestVariants);
  }

  /**
   * Validates that no existing variants are removed except in DRAFT mode.
   *
   * @param status current experiment status
   * @param existingVariants existing variants map
   * @param requestVariants request variants map
   */
  private void validateNoVariantRemoval(
      ExperimentStatus status,
      Map<String, Variant> existingVariants,
      Map<String, Variant> requestVariants) {

    if (status == ExperimentStatus.DRAFT) {
      return; // Removal allowed in DRAFT
    }

    if (existingVariants == null || existingVariants.isEmpty()) {
      return;
    }

    for (String existingKey : existingVariants.keySet()) {
      if (!requestVariants.containsKey(existingKey)) {
        throw new RestException(ErrorEnum.VARIANT_REMOVAL_NOT_ALLOWED);
      }
    }
  }

  /** Validates contiguous variant naming: control, variant1, variant2, ... */
  private void validateContiguousVariantNaming(Set<String> variantKeys) {
    if (variantKeys == null || variantKeys.isEmpty()) {
      return;
    }

    if (!variantKeys.contains("control")) {
      throw new RestException(ErrorEnum.VARIANT_NAMING_INVALID);
    }

    int expectedNumberedVariants = variantKeys.size() - 1;
    for (int i = 1; i <= expectedNumberedVariants; i++) {
      if (!variantKeys.contains("variant" + i)) {
        throw new RestException(ErrorEnum.VARIANT_NAMING_NOT_CONTIGUOUS);
      }
    }
  }

  /**
   * Validates variable keys across all variants:
   *
   * <ul>
   *   <li>All variants must have the SAME variable keys
   *   <li>New key added → only allowed in DRAFT mode
   *   <li>Key removed → only allowed in DRAFT mode
   *   <li>dataType cannot change for existing keys
   * </ul>
   */
  private void validateVariableKeys(
      ExperimentStatus status,
      Map<String, Variant> existingVariants,
      Map<String, Variant> requestVariants) {

    Set<String> existingKeys = new HashSet<>();
    Map<String, String> existingKeyDataTypes = new HashMap<>();
    if (existingVariants != null) {
      for (Variant variant : existingVariants.values()) {
        if (variant.getVariables() != null) {
          for (Variables var : variant.getVariables()) {
            existingKeys.add(var.getKey());
            existingKeyDataTypes.put(var.getKey(), var.getDataType());
          }
        }
      }
    }

    Set<String> allRequestKeys = new HashSet<>();
    Map<String, String> requestKeyDataTypes = new HashMap<>();

    for (Variant variant : requestVariants.values()) {
      if (variant.getVariables() != null) {
        for (Variables var : variant.getVariables()) {
          String key = var.getKey();
          String dataType = var.getDataType();
          allRequestKeys.add(key);

          if (requestKeyDataTypes.containsKey(key)) {
            if (!Objects.equals(requestKeyDataTypes.get(key), dataType)) {
              throw new RestException(ErrorEnum.VARIABLE_DATA_TYPE_INCONSISTENT_ACROSS_VARIANTS);
            }
          } else {
            requestKeyDataTypes.put(key, dataType);
          }

          if (existingKeyDataTypes.containsKey(key)
              && !Objects.equals(existingKeyDataTypes.get(key), dataType)) {
            throw new RestException(ErrorEnum.VARIABLE_DATA_TYPE_CHANGED);
          }
        }
      }
    }

    // Check for removed keys - only allowed in DRAFT
    Set<String> removedKeys = new HashSet<>(existingKeys);
    removedKeys.removeAll(allRequestKeys);

    if (!removedKeys.isEmpty() && status != ExperimentStatus.DRAFT) {
      throw new RestException(ErrorEnum.VARIABLE_KEY_REMOVAL_NOT_ALLOWED);
    }

    // Check for added keys - only allowed in DRAFT
    Set<String> addedKeys = new HashSet<>(allRequestKeys);
    addedKeys.removeAll(existingKeys);

    if (!addedKeys.isEmpty() && status != ExperimentStatus.DRAFT) {
      throw new RestException(ErrorEnum.VARIABLE_KEY_ADDITION_NOT_ALLOWED);
    }

    // Ensure all variants have consistent keys
    for (Map.Entry<String, Variant> entry : requestVariants.entrySet()) {
      Set<String> variantKeys = new HashSet<>();
      if (entry.getValue().getVariables() != null) {
        for (Variables var : entry.getValue().getVariables()) {
          variantKeys.add(var.getKey());
        }
      }

      if (!variantKeys.equals(allRequestKeys)) {
        throw new RestException(ErrorEnum.VARIABLE_KEYS_INCONSISTENT_ACROSS_VARIANTS);
      }
    }
  }
}
