package com.ascend.testlab.allocation.strategy.variantselection;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Stratified variant selection strategy. Selects variants based on cohort-to-variant mappings
 * defined in StratifiedVariantWeights. If user belongs to any cohort mapped to a variant, they get
 * that variant.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class StratifiedVariantSelectionStrategy implements VariantSelectionStrategy {

  @Override
  public String selectVariant(Experiment experiment, String userId, List<String> userCohorts) {
    if (!(experiment.getVariantWeights() instanceof StratifiedVariantWeights)) {
      log.warn(
          "Stratified assignment domain but variant weights is not StratifiedVariantWeights for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    if (userCohorts == null || userCohorts.isEmpty()) {
      log.debug(
          "No cohorts provided for user {} in experiment {}", userId, experiment.getExperimentId());
      return null;
    }

    StratifiedVariantWeights manualWeights =
        (StratifiedVariantWeights) experiment.getVariantWeights();
    String selectedVariantName = manualWeights.getVariantForCohorts(userCohorts);

    // Validate that the selected variant exists in the experiment's variant map
    if (selectedVariantName == null) {
      log.debug(
          "No variant mapping found for user {} with cohorts {} in experiment {}",
          userId,
          userCohorts,
          experiment.getExperimentId());
      return null;
    }

    if (experiment.getVariants() == null
        || !experiment.getVariants().containsKey(selectedVariantName)) {
      log.warn(
          "Selected variant {} for user {} not found in variant map for experiment {}",
          selectedVariantName,
          userId,
          experiment.getExperimentId());
      return null;
    }

    log.debug(
        "Selected variant {} for user {} (cohorts: {}) in experiment {}",
        selectedVariantName,
        userId,
        userCohorts,
        experiment.getExperimentId());

    return selectedVariantName;
  }

  @Override
  public boolean canHandle(Experiment experiment) {
    return experiment != null
        && experiment.getAssignmentDomain() == AssignmentDomain.STRATIFIED
        && experiment.getVariantWeights() instanceof StratifiedVariantWeights;
  }
}
