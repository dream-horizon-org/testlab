package com.ascend.testlab.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the distribution weights for different variants in an experiment Maps variant names to
 * their respective Variant objects with percentage and variables
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantWeights {
  private Map<String, Variant> variants; // Map of variant name to Variant object
}
