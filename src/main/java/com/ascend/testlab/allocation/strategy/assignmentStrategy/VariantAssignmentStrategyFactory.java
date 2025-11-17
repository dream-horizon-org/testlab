package com.ascend.testlab.allocation.strategy.assignmentStrategy;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating variant assignment strategies based on distribution strategy type.
 * Implements the Factory Design Pattern to encapsulate strategy instantiation logic.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class VariantAssignmentStrategyFactory {

  private static final RoundRobinVariantAssignment ROUND_ROBIN_STRATEGY =
      new RoundRobinVariantAssignment();
  private static final RandomVariantAssignment RANDOM_STRATEGY = new RandomVariantAssignment();

  /**
   * Gets the appropriate variant assignment strategy based on the distribution strategy.
   *
   * @param distributionStrategy the distribution strategy type
   * @return appropriate VariantAssignmentStrategy implementation
   */
  public static VariantAssignmentStrategy getStrategy(DistributionStrategy distributionStrategy) {
    if (distributionStrategy == null) {
      log.warn("Null distribution strategy provided, defaulting to RANDOM");
      return RANDOM_STRATEGY;
    }

    switch (distributionStrategy) {
      case ROUND_ROBIN:
        log.debug("Using ROUND_ROBIN variant assignment strategy");
        return ROUND_ROBIN_STRATEGY;

      case RANDOM:
        log.debug("Using RANDOM variant assignment strategy");
        return RANDOM_STRATEGY;

      default:
        log.warn("Unknown distribution strategy: {}, defaulting to RANDOM", distributionStrategy);
        return RANDOM_STRATEGY;
    }
  }
}
