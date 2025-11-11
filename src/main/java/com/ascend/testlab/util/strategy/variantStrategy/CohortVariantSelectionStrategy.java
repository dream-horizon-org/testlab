package com.ascend.testlab.util.strategy.variantStrategy;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.entity.*;
import com.ascend.testlab.entity.variantWeights.CohortVariantWeights;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Cohort-based variant selection strategy. Selects variants based on user's cohorts and their
 * weighted distribution.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
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

    return selectVariantNameByCohortWeight((CohortVariantWeights) experiment.getVariantWeights());
  }

  private String selectVariantNameByCohortWeight(CohortVariantWeights weightedVariants) {
    String variant = Constants.EMPTY_STRING;
    Map<String, Double> percentageDistributionMap = weightedVariants.getWeights();
    double random = Math.random();
    double weight = 0.0;
    for (Map.Entry<String, Double> percentageDistribution : percentageDistributionMap.entrySet()) {
      weight = weight + percentageDistribution.getValue();
      if (weight > random) {
        variant = percentageDistribution.getKey();
        break;
      }
    }
    return variant;
  }

  @Override
  public boolean canHandle(Experiment experiment) {
    return experiment != null
        && experiment.getAssignmentDomain() == AssignmentDomain.COHORT
        && experiment.getVariantWeights() instanceof CohortVariantWeights;
  }
}
