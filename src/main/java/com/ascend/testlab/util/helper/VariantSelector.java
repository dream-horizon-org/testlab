package com.ascend.testlab.util.helper;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.util.strategy.RandomVariantAssignment;
import com.ascend.testlab.util.strategy.RoundRobinVariantAssignment;
import com.ascend.testlab.util.strategy.VariantAssignmentStrategy;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VariantSelector {

  private static final VariantAssignmentStrategy RANDOM_STRATEGY = new RandomVariantAssignment();
  private static final VariantAssignmentStrategy ROUND_ROBIN_STRATEGY =
      new RoundRobinVariantAssignment();

  /**
   * Selects a variant for the given experiment and user
   *
   * @param experiment the experiment to assign
   * @param userId user identifier
   * @return selected variant or null if none available
   */
  public static Variant selectVariant(Experiment experiment, String userId) {
    if (experiment == null || experiment.getVariantWeights() == null) {
      log.warn(
          "Invalid experiment or variant weights for experiment: {}",
          experiment != null ? experiment.getExperimentId() : "null");
      return null;
    }

    List<Variant> variants = getVariantsList(experiment);

    if (variants.isEmpty()) {
      log.warn("No variants available for experiment: {}", experiment.getExperimentId());
      return null;
    }

    VariantAssignmentStrategy strategy = getStrategy(experiment.getDistributionStrategy());

    return strategy.selectVariant(variants, userId);
  }

  private static VariantAssignmentStrategy getStrategy(DistributionStrategy strategyType) {
    if (strategyType == null) {
      log.debug("No distribution strategy specified, using RANDOM");
      return RANDOM_STRATEGY;
    }

    switch (strategyType) {
      case ROUND_ROBIN:
        return ROUND_ROBIN_STRATEGY;
      case RANDOM:
      default:
        return RANDOM_STRATEGY;
    }
  }

  private static List<Variant> getVariantsList(Experiment experiment) {
    if (experiment.getVariantWeights() == null
        || experiment.getVariantWeights().getVariants() == null) {
      return new ArrayList<>();
    }

    return new ArrayList<>(experiment.getVariantWeights().getVariants().values());
  }
}
