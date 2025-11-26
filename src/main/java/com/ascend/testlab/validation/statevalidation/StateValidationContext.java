package com.ascend.testlab.validation.statevalidation;

import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.google.inject.Inject;
import java.util.ArrayList;
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

  /**
   * Constructor that accepts strategies via dependency injection.
   *
   * @param liveStrategy validation strategy for LIVE state
   * @param pausedStrategy validation strategy for PAUSED state
   * @param draftStrategy validation strategy for DRAFT state
   */
  @Inject
  public StateValidationContext(
      LiveStateValidationStrategy liveStrategy,
      PausedStateValidationStrategy pausedStrategy,
      DraftStateValidationStrategy draftStrategy) {
    this.strategies = new ArrayList<>();
    strategies.add(liveStrategy);
    strategies.add(pausedStrategy);
    strategies.add(draftStrategy);
    // CONCLUDED and TERMINATED are handled as terminal states in the service layer
  }

  /**
   * Registers a new validation strategy.
   *
   * @param strategy the strategy to register
   */
  public void registerStrategy(StateValidationStrategy strategy) {
    strategies.add(strategy);
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

  /**
   * Returns the number of registered strategies.
   *
   * @return strategy count
   */
  public int getStrategyCount() {
    return strategies.size();
  }
}
