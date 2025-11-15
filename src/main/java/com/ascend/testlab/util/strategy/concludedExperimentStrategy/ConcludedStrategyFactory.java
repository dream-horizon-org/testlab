package com.ascend.testlab.util.strategy.concludedExperimentStrategy;

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
public class ConcludedStrategyFactory {

  public enum StrategyType {
    /** Only add winning variant for users who were not assigned */
    UNASSIGNED_ONLY,
    /** Apply winning variant to all users */
    DEFAULT
  }

  /**
   * Gets a strategy implementation based on the strategy type.
   *
   * @param strategyType the type of strategy to get
   * @return the corresponding strategy implementation
   */
  public static ConcludedStrategy getStrategy(StrategyType strategyType) {
    log.debug("Creating concluded experiment strategy: {}", strategyType);

    switch (strategyType) {
      case UNASSIGNED_ONLY:
        return new UnassignedOnlyStrategy();
      case DEFAULT:
        return new DefaultStrategy();
      default:
        log.warn("Unknown strategy type {}, defaulting to BOTH", strategyType);
        return new DefaultStrategy();
    }
  }

  /**
   * Gets a strategy implementation based on a string name. Useful for configuration-based strategy
   * selection.
   *
   * @param strategyName the name of the strategy
   * @return the corresponding strategy implementation
   */
  public static ConcludedStrategy getStrategyByName(String strategyName) {
    try {
      StrategyType type = StrategyType.valueOf(strategyName.toUpperCase());
      return getStrategy(type);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid strategy name '{}', defaulting to BOTH", strategyName);
      return new DefaultStrategy();
    }
  }

  /**
   * Gets the default strategy (BOTH).
   *
   * @return the default strategy implementation
   */
  public static ConcludedStrategy getDefaultStrategy() {
    return new DefaultStrategy();
  }
}
