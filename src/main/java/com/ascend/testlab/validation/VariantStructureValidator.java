package com.ascend.testlab.validation;

import com.ascend.testlab.entity.Variables;
import com.ascend.testlab.entity.Variant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator to ensure variant structure consistency during updates.
 *
 * <p>Validates that when updating variants, only variable values change - not keys or data types.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class VariantStructureValidator {

  private final ObjectMapper objectMapper;

  @Inject
  public VariantStructureValidator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Validates that variant updates only modify variable values.
   *
   * @param existingVariantsJson existing variants from database (JSONB)
   * @param newVariants new variants from update request
   * @param experimentId experiment identifier for logging
   * @throws IllegalArgumentException if structure validation fails
   */
  public void validate(
      Object existingVariantsJson, Map<String, Variant> newVariants, UUID experimentId) {

    if (existingVariantsJson == null || newVariants == null) {
      log.debug(
          "Skipping variant structure validation - null data for experimentId: {}", experimentId);
      return;
    }

    try {
      // Deserialize existing variants from JSONB
      Map<String, Object> existingVariantsMap;

      if (existingVariantsJson instanceof String) {
        // If stored as JSON string, parse it
        existingVariantsMap = objectMapper.readValue((String) existingVariantsJson, Map.class);
      } else if (existingVariantsJson instanceof Map) {
        // If already a Map, use it directly
        @SuppressWarnings("unchecked")
        Map<String, Object> tempMap = (Map<String, Object>) existingVariantsJson;
        existingVariantsMap = tempMap;
      } else {
        // Try to convert using ObjectMapper
        existingVariantsMap = objectMapper.convertValue(existingVariantsJson, Map.class);
      }

      // Validate variant keys match
      Set<String> existingVariantKeys = existingVariantsMap.keySet();
      Set<String> newVariantKeys = newVariants.keySet();

      if (!existingVariantKeys.equals(newVariantKeys)) {
        String errorMsg =
            String.format(
                "Variant keys cannot be changed. Expected: %s, but got: %s",
                existingVariantKeys, newVariantKeys);
        log.error(
            "Variant structure validation failed for experimentId: {}, error: {}",
            experimentId,
            errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }

      // Validate each variant's variable structure
      for (String variantKey : existingVariantKeys) {
        validateVariantVariables(
            existingVariantsMap.get(variantKey),
            newVariants.get(variantKey),
            variantKey,
            experimentId);
      }

      log.info("Variant structure validation passed for experimentId: {}", experimentId);

    } catch (IllegalArgumentException e) {
      throw e; // Re-throw validation errors
    } catch (Exception e) {
      log.error(
          "Error during variant structure validation for experimentId: {}, error: {}",
          experimentId,
          e.getMessage());
      throw new IllegalArgumentException(
          "Failed to validate variant structure: " + e.getMessage(), e);
    }
  }

  /**
   * Validates that variable keys and data types match between existing and new variants.
   *
   * @param existingVariantObj existing variant data
   * @param newVariant new variant from update request
   * @param variantKey variant key for error messages
   * @param experimentId experiment identifier for logging
   */
  private void validateVariantVariables(
      Object existingVariantObj, Variant newVariant, String variantKey, UUID experimentId) {

    if (existingVariantObj == null || newVariant == null) {
      return;
    }

    try {
      // Convert existing variant to map
      @SuppressWarnings("unchecked")
      Map<String, Object> existingVariantMap =
          objectMapper.convertValue(existingVariantObj, Map.class);

      // Get existing variables
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> existingVariables =
          (List<Map<String, Object>>) existingVariantMap.get("variables");

      List<Variables> newVariables = newVariant.getVariables();

      if (existingVariables == null || newVariables == null) {
        return;
      }

      // Check variable count matches
      if (existingVariables.size() != newVariables.size()) {
        String errorMsg =
            String.format(
                "Variable count mismatch for variant '%s'. Expected %d variables, but got %d",
                variantKey, existingVariables.size(), newVariables.size());
        log.error(
            "Variant structure validation failed for experimentId: {}, error: {}",
            experimentId,
            errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }

      // Build maps for comparison (key -> data_type)
      Map<String, String> existingVarStructure = new HashMap<>();
      for (Map<String, Object> existingVar : existingVariables) {
        String key = (String) existingVar.get("key");
        String dataType = (String) existingVar.get("data_type");
        if (key != null && dataType != null) {
          existingVarStructure.put(key, dataType);
        }
      }

      Map<String, String> newVarStructure = new HashMap<>();
      for (Variables newVar : newVariables) {
        if (newVar.getKey() != null && newVar.getDataType() != null) {
          newVarStructure.put(newVar.getKey(), newVar.getDataType());
        }
      }

      // Validate keys match
      if (!existingVarStructure.keySet().equals(newVarStructure.keySet())) {
        String errorMsg =
            String.format(
                "Variable keys cannot be changed for variant '%s'. Expected keys: %s, but got: %s. Only variable values can be updated.",
                variantKey, existingVarStructure.keySet(), newVarStructure.keySet());
        log.error(
            "Variant structure validation failed for experimentId: {}, error: {}",
            experimentId,
            errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }

      // Validate data types match for each key
      for (String key : existingVarStructure.keySet()) {
        String existingDataType = existingVarStructure.get(key);
        String newDataType = newVarStructure.get(key);

        if (!existingDataType.equals(newDataType)) {
          String errorMsg =
              String.format(
                  "Variable data_type cannot be changed for variant '%s', variable '%s'. Expected data_type: '%s', but got: '%s'. Only variable values can be updated.",
                  variantKey, key, existingDataType, newDataType);
          log.error(
              "Variant structure validation failed for experimentId: {}, error: {}",
              experimentId,
              errorMsg);
          throw new IllegalArgumentException(errorMsg);
        }
      }

      log.debug(
          "Variable structure validated successfully for variant '{}' in experimentId: {}",
          variantKey,
          experimentId);

    } catch (IllegalArgumentException e) {
      throw e; // Re-throw validation errors
    } catch (Exception e) {
      log.error(
          "Error validating variant '{}' for experimentId: {}, error: {}",
          variantKey,
          experimentId,
          e.getMessage());
      throw new IllegalArgumentException(
          "Failed to validate variant '" + variantKey + "': " + e.getMessage(), e);
    }
  }
}
