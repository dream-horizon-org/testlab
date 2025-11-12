package com.ascend.testlab.util.strategy.ConcludedExperimentStrategy;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for creating concluded experiment override strategies. Provides easy access to
 * different strategy implementations.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class ConcludedExperimentStrategyFactory {

  /** Strategy type enum for easy selection */
  public enum StrategyType {
    /** Only override users who were already assigned to the experiment */
    ASSIGNED_ONLY,
    /** Only add winning variant for users who were not assigned */
    UNASSIGNED_ONLY,
    /** Apply winning variant to all users */
    BOTH
  }

  /**
   * Gets a strategy implementation based on the strategy type.
   *
   * @param strategyType the type of strategy to get
   * @return the corresponding strategy implementation
   */
  public static ConcludedExperimentOverrideStrategy getStrategy(StrategyType strategyType) {
    log.debug("Creating concluded experiment strategy: {}", strategyType);

    switch (strategyType) {
      case ASSIGNED_ONLY:
        return new AssignedOnlyStrategy();
      case UNASSIGNED_ONLY:
        return new UnassignedOnlyStrategy();
      case BOTH:
        return new BothStrategy();
      default:
        log.warn("Unknown strategy type {}, defaulting to BOTH", strategyType);
        return new BothStrategy();
    }
  }

  /**
   * Gets a strategy implementation based on a string name. Useful for configuration-based strategy
   * selection.
   *
   * @param strategyName the name of the strategy
   * @return the corresponding strategy implementation
   */
  public static ConcludedExperimentOverrideStrategy getStrategyByName(String strategyName) {
    try {
      StrategyType type = StrategyType.valueOf(strategyName.toUpperCase());
      return getStrategy(type);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid strategy name '{}', defaulting to BOTH", strategyName);
      return new BothStrategy();
    }
  }

  /**
   * Gets the default strategy (BOTH).
   *
   * @return the default strategy implementation
   */
  public static ConcludedExperimentOverrideStrategy getDefaultStrategy() {
    return new BothStrategy();
  }
}
