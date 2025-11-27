package com.ascend.testlab.allocation.strategy.concludedexperiment;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.List;

/**
 * Strategy interface for handling concluded experiment variant overrides. Provides flexible control
 * over which user assignments should be overridden with winning variants.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public interface ConcludedStrategy {

  /**
   * Determines whether a concluded experiment's winning variant should override existing user
   * assignments.
   *
   * @param concludedExperiment the concluded experiment with winning variant
   * @param existingAssignments user's current experiment assignments
   * @return true if the winning variant should be applied, false otherwise
   */
  boolean shouldOverride(
      Experiment concludedExperiment, List<UserExperimentMap> existingAssignments);

  /**
   * Gets the strategy name for identification and logging.
   *
   * @return strategy name
   */
  String getStrategyName();
}
