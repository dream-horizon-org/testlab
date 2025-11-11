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
 * @author anudeepreddy20
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

  //  /**
  //   * Selects a variant for the given experiment and user (without cohort information)
  //   *
  //   * @param experiment the experiment to assign
  //   * @param userId user identifier
  //   * @return selected variant or null if none available
  //   */
  //  public static Variant selectVariant(Experiment experiment, String userId) {
  //    return selectVariant(experiment, userId);
  //  }

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
        log.debug(
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

  //  /**
  //   * Gets a list of all available variants for an experiment
  //   *
  //   * @param experiment the experiment
  //   * @return list of variants
  //   */
  //  public static List<Variant> getVariantsList(Experiment experiment) {
  //    if (experiment == null || experiment.getVariant() == null) {
  //      return new ArrayList<>();
  //    }
  //    return new ArrayList<>(experiment.getVariant().values());
  //  }

  //  /**
  //   * Validates if an experiment has proper variant configuration
  //   *
  //   * @param experiment the experiment to validate
  //   * @return true if experiment has valid variant configuration
  //   */
  //  public static boolean hasValidVariantConfiguration(Experiment experiment) {
  //    if (experiment == null) {
  //      return false;
  //    }
  //
  //    if (experiment.getVariant() == null || experiment.getVariant().isEmpty()) {
  //      log.debug("Experiment {} has no variants defined", experiment.getExperimentId());
  //      return false;
  //    }
  //
  //    if (experiment.getVariantWeights() == null) {
  //      log.debug("Experiment {} has no variant weights defined", experiment.getExperimentId());
  //      return false;
  //    }
  //
  //    return true;
  //  }
}
