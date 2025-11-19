package com.ascend.testlab.allocation.strategy.variantStrategy;

import com.ascend.testlab.allocation.strategy.assignmentStrategy.VariantAssignmentStrategy;
import com.ascend.testlab.allocation.strategy.assignmentStrategy.VariantAssignmentStrategyFactory;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.entity.*;
import com.ascend.testlab.entity.variantWeights.CohortVariantWeights;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Cohort-based variant selection strategy. Selects variants using random or round-robin
 * distribution from ALL variants in the experiment's variant map.
 *
 * <p>This strategy ignores variantWeights and simply selects from all available variants in the
 * experiment based on the distribution strategy (ROUND_ROBIN or RANDOM).
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see VariantSelectionStrategy
 * @see VariantAssignmentStrategy
 * @see VariantAssignmentStrategyFactory
 */
@Slf4j
public class CohortVariantSelectionStrategy implements VariantSelectionStrategy {

  @Override
  public String selectVariant(Experiment experiment, String userId, List<String> userCohorts) {
    if (!(experiment.getVariantWeights() instanceof CohortVariantWeights)) {
      log.warn(
          "Cohort assignment domain but variant weights is not CohortVariantWeights for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    List<Variant> variants = convertVariantMapToList(experiment.getVariant());
    if (variants == null || variants.isEmpty()) {
      log.warn("No variants available for experiment {}", experiment.getExperimentId());
      return null;
    }

    DistributionStrategy distributionStrategy = experiment.getDistributionStrategy();

    VariantAssignmentStrategy assignmentStrategy =
        VariantAssignmentStrategyFactory.getStrategy(distributionStrategy);

    Variant selectedVariant = assignmentStrategy.selectVariant(variants, userId);

    if (selectedVariant == null) {
      log.warn(
          "No variant selected for user {} in experiment {}", userId, experiment.getExperimentId());
      return null;
    }

    String selectedVariantName = findVariantNameInMap(experiment.getVariant(), selectedVariant);

    if (selectedVariantName == null) {
      log.warn(
          "Selected variant not found in variant map for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    log.debug(
        "Selected variant {} for user {} in experiment {} using {} strategy from {} total variants",
        selectedVariant.getDisplayName(),
        userId,
        experiment.getExperimentId(),
        distributionStrategy,
        variants.size());

    return selectedVariantName;
  }

  /**
   * Converts the experiment's variant map to a list of Variant objects for selection.
   *
   * @param variantMap map of variant name to Variant object from experiment
   * @return list of all Variant objects, or null if map is null or empty
   */
  private List<Variant> convertVariantMapToList(Map<String, Variant> variantMap) {
    if (variantMap == null || variantMap.isEmpty()) {
      log.warn("Empty or null variant map provided");
      return null;
    }

    List<Variant> variants = new ArrayList<>(variantMap.values());

    log.debug("Converted {} variants from variant map", variants.size());
    return variants;
  }

  /**
   * Finds the key (variant name) for a given Variant object in the variant map.
   *
   * @param variantMap map of variant name to Variant object
   * @param targetVariant the Variant object to find
   * @return the variant name (key) or null if not found
   */
  private String findVariantNameInMap(Map<String, Variant> variantMap, Variant targetVariant) {
    if (variantMap == null || targetVariant == null) {
      return null;
    }

    for (Map.Entry<String, Variant> entry : variantMap.entrySet()) {
      if (entry.getValue().equals(targetVariant)) {
        return entry.getKey();
      }
    }

    log.warn("Variant object not found in map: {}", targetVariant.getDisplayName());
    return null;
  }

  @Override
  public boolean canHandle(Experiment experiment) {
    return experiment != null
        && experiment.getAssignmentDomain() == AssignmentDomain.COHORT
        && experiment.getVariantWeights() instanceof CohortVariantWeights;
  }
}
