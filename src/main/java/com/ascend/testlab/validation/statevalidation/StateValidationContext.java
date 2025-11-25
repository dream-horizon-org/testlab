package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Context for managing and executing state-based validation strategies.
 *
 * <p>Uses Chain of Responsibility pattern to apply appropriate validation based on experiment
 * state.
 */
@Slf4j
public class StateValidationContext {

  private final List<StateValidationStrategy> strategies;

  /** Default constructor that initializes with all available strategies. */
  public StateValidationContext() {
    this.strategies = new ArrayList<>();
    registerDefaultStrategies();
  }

  /**
   * Constructor that accepts custom strategies.
   *
   * @param strategies list of validation strategies
   */
  public StateValidationContext(List<StateValidationStrategy> strategies) {
    this.strategies = new ArrayList<>(strategies);
    sortStrategiesByPriority();
  }

  /** Registers default validation strategies for all experiment states. */
  private void registerDefaultStrategies() {
    strategies.add(new LiveStateValidationStrategy());
    strategies.add(new DraftStateValidationStrategy());
    // Add more strategies here as needed (e.g., PausedStateValidationStrategy)
    sortStrategiesByPriority();
  }

  /**
   * Registers a new validation strategy.
   *
   * @param strategy the strategy to register
   */
  public void registerStrategy(StateValidationStrategy strategy) {
    strategies.add(strategy);
    sortStrategiesByPriority();
  }

  /**
   * Validates the update request based on the experiment's current state.
   *
   * <p>Iterates through registered strategies and applies the first matching strategy.
   *
   * @param request the update request containing fields to update
   * @param currentStatus the current experiment status
   * @param experimentId the experiment identifier for logging
   * @throws IllegalArgumentException if validation fails
   */
  public void validate(UpdateExperimentRequest request, String currentStatus, UUID experimentId) {
    log.debug(
        "Starting state-based validation for experimentId: {}, status: {}",
        experimentId,
        currentStatus);

    boolean strategyApplied = false;

    for (StateValidationStrategy strategy : strategies) {
      if (strategy.appliesTo(currentStatus)) {
        log.debug(
            "Applying validation strategy: {} for experimentId: {}",
            strategy.getClass().getSimpleName(),
            experimentId);

        strategy.validate(request, currentStatus, experimentId);
        strategyApplied = true;
        break; // Apply only the first matching strategy
      }
    }

    if (!strategyApplied) {
      log.warn(
          "No validation strategy found for status: {} on experimentId: {}",
          currentStatus,
          experimentId);
    }
  }

  /** Sorts strategies by priority (lower number = higher priority). */
  private void sortStrategiesByPriority() {
    strategies.sort(Comparator.comparingInt(StateValidationStrategy::getPriority));
  }

  /**
   * Returns the number of registered strategies.
   *
   * @return strategy count
   */
  public int getStrategyCount() {
    return strategies.size();
  }
}
