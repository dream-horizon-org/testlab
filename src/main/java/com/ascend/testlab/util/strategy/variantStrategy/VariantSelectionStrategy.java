package com.ascend.testlab.util.strategy.variantStrategy;

import com.ascend.testlab.entity.Experiment;

/**
 * Strategy interface for selecting variants based on assignment domain type. Implements the
 * Strategy Design Pattern for different variant selection approaches.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public interface VariantSelectionStrategy {

  /**
   * Selects a variant for the given experiment and user
   *
   * @param experiment the experiment containing variant configuration
   * @param userId user identifier
   * @return selected variant or null if no suitable variant found
   */
  String selectVariant(Experiment experiment, String userId);

  /**
   * Checks if this strategy can handle the given experiment's assignment domain
   *
   * @param experiment the experiment to check
   * @return true if this strategy can handle the experiment
   */
  boolean canHandle(Experiment experiment);
}
