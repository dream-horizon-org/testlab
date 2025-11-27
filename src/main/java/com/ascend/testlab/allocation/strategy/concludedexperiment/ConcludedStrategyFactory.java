package com.ascend.testlab.allocation.strategy.concludedexperiment;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for creating concluded experiment override strategies. Provides easy access to
 * different strategy implementations.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class ConcludedStrategyFactory {

  public enum StrategyType {
    /** Only override winning variant for users who were already assigned */
    ASSIGNED_ONLY,
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

    return switch (strategyType) {
      case ASSIGNED_ONLY -> new AssignedOnlyStrategy();
      case UNASSIGNED_ONLY -> new UnassignedOnlyStrategy();
      case DEFAULT -> new DefaultStrategy();
    };
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
