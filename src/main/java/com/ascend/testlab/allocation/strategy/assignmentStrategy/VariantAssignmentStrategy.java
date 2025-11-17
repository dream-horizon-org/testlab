package com.ascend.testlab.allocation.strategy.assignmentStrategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;

/**
 * Strategy interface for variant assignment.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
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
