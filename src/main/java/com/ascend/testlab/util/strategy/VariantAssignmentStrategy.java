package com.ascend.testlab.util.strategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;

/**
 * Strategy interface for variant assignment
 */
public interface VariantAssignmentStrategy {

  /**
   * Selects a variant from the given list based on the strategy implementation
   *
   * @param variants list of available variants
   * @param userId user identifier for deterministic strategies
   * @return selected variant
   */
  Variant selectVariant(List<Variant> variants, String userId);
}

