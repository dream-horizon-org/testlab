package com.ascend.testlab.util.strategy.variantStrategy;

import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.variantWeights.ManualVariantWeights;
import lombok.extern.slf4j.Slf4j;

/**
 * Manual variant selection strategy. Selects variants based on explicit user-to-variant mappings
 * defined in ManualVariantWeights.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ManualVariantSelectionStrategy implements VariantSelectionStrategy {

  @Override
  public String selectVariant(Experiment experiment, String userId) {
    if (!(experiment.getVariantWeights() instanceof ManualVariantWeights)) {
      log.warn(
          "Manual assignment domain but variant weights is not ManualVariantWeights for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    ManualVariantWeights manualWeights = (ManualVariantWeights) experiment.getVariantWeights();

    return manualWeights.getVariantForUser(userId);
  }

  @Override
  public boolean canHandle(Experiment experiment) {
    return experiment != null
        && experiment.getAssignmentDomain() == AssignmentDomain.MANUAL
        && experiment.getVariantWeights() instanceof ManualVariantWeights;
  }
}
