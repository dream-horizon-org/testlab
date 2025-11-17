package com.ascend.testlab.util.helper;

import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.util.strategy.variantStrategy.CohortVariantSelectionStrategy;
import com.ascend.testlab.util.strategy.variantStrategy.ManualVariantSelectionStrategy;
import com.ascend.testlab.util.strategy.variantStrategy.VariantSelectionStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for selecting variants based on distribution strategy and assignment domain. Uses
 * Strategy Pattern with a chain of responsibility to delegate to appropriate selection strategy.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class VariantSelector {

  private static final List<VariantSelectionStrategy> STRATEGIES = new ArrayList<>();

  static {
    STRATEGIES.add(new ManualVariantSelectionStrategy());
    STRATEGIES.add(new CohortVariantSelectionStrategy());
  }

  /**
   * Selects a variant for the given experiment and user with cohort information. Uses the
   * appropriate strategy based on assignment domain.
   *
   * @param experiment the experiment to assign
   * @param userId user identifier
   * @return selected variant or null if none available
   */
  public static String selectVariant(Experiment experiment, String userId) {

    if (Objects.isNull(experiment)) {
      log.warn("Cannot select variant: experiment is null");
      return null;
    }

    if (StringUtils.isBlank(userId)) {
      log.warn("Cannot select variant: userId is null or empty");
      return null;
    }

    if (Objects.isNull(experiment.getVariantWeights())) {
      log.warn(
          "Cannot select variant: no variant weights configured for experiment {}",
          experiment.getExperimentId());
      return null;
    }

    for (VariantSelectionStrategy strategy : STRATEGIES) {
      if (strategy.canHandle(experiment)) {
        log.info(
            "Using {} strategy for experiment {}",
            strategy.getClass().getSimpleName(),
            experiment.getExperimentId());

        String selectedVariant = strategy.selectVariant(experiment, userId);

        if (selectedVariant != null) {
          log.info(
              "Successfully selected variant {} for user {} in experiment {} using {} domain",
              selectedVariant,
              userId,
              experiment.getExperimentId(),
              experiment.getAssignmentDomain());
          return selectedVariant;
        }
      }
    }

    log.warn(
        "No suitable strategy found or no variant selected for experiment {} with domain {}",
        experiment.getExperimentId(),
        experiment.getAssignmentDomain());
    return null;
  }
}
