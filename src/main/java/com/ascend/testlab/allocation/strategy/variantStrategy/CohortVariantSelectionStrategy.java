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
 * Cohort-based variant selection strategy. Selects variants based on user's cohorts and their
 * distribution strategy (ROUND_ROBIN, RANDOM) using the Strategy and Factory design patterns.
 *
 * <p>This class acts as an adapter between the VariantSelectionStrategy interface (used at the
 * assignment domain level) and the VariantAssignmentStrategy interface (used for distribution
 * strategy selection). It converts CohortVariantWeights to a list of Variants and delegates the
 * actual selection to the appropriate VariantAssignmentStrategy obtained from the factory.
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
  public String selectVariant(Experiment experiment, String userId) {
    if (!(experiment.getVariantWeights() instanceof CohortVariantWeights)) {
      log.warn(
          "Cohort assignment domain but variant weights is not CohortVariantWeights for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    CohortVariantWeights cohortWeights = (CohortVariantWeights) experiment.getVariantWeights();

    List<Variant> variants = convertToVariantList(cohortWeights, experiment.getVariant());
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

    log.debug(
        "Selected variant {} for user {} in experiment {} using {} strategy",
        selectedVariant.getDisplayName(),
        userId,
        experiment.getExperimentId(),
        distributionStrategy);

    return selectedVariant.getVariantName();
  }

  /**
   * Converts CohortVariantWeights to a list of Variant objects for strategy pattern usage.
   *
   * @param cohortWeights cohort variant weights containing variant names
   * @param variantMap map of variant name to Variant object from experiment
   * @return list of Variant objects, or null if no valid variants
   */
  private List<Variant> convertToVariantList(
      CohortVariantWeights cohortWeights, Map<String, Variant> variantMap) {

    if (cohortWeights == null
        || cohortWeights.getWeights() == null
        || cohortWeights.getWeights().isEmpty()) {
      log.warn("Empty cohort weights provided");
      return null;
    }

    if (variantMap == null || variantMap.isEmpty()) {
      log.warn("Empty variant map provided");
      return null;
    }

    List<Variant> variants = new ArrayList<>();

    for (String variantName : cohortWeights.getWeights().keySet()) {
      Variant variant = variantMap.get(variantName);

      if (variant != null) {
        variants.add(variant);
      } else {
        log.warn("Variant {} not found in variant map", variantName);
      }
    }

    if (variants.isEmpty()) {
      log.warn("No valid variants found after conversion");
      return null;
    }

    log.debug("Converted {} variants from cohort weights", variants.size());
    return variants;
  }

  @Override
  public boolean canHandle(Experiment experiment) {
    return experiment != null
        && experiment.getAssignmentDomain() == AssignmentDomain.COHORT
        && experiment.getVariantWeights() instanceof CohortVariantWeights;
  }
}
