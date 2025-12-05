package com.ascend.testlab.allocation.builder;

import com.ascend.testlab.constants.enums.AllocationStatus;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import lombok.experimental.UtilityClass;

/**
 * Builder class for creating UserExperimentMap instances. Provides a consistent way to build
 * assignment objects across the application.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class AssignmentBuilder {

  /**
   * Creates a UserExperimentMap from experiment and variant
   *
   * @param experiment the experiment
   * @param variantName the variant name
   * @return UserExperimentMap instance
   */
  public static UserExperimentMap buildAssignment(Experiment experiment, String variantName) {
    if (Objects.isNull(experiment) || Objects.isNull(variantName)) {
      throw new IllegalArgumentException("Experiment and variant cannot be null");
    }

    if (Objects.isNull(experiment.getVariants()) || experiment.getVariants().isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Experiment variant map cannot be null or empty for experiment: %s",
              experiment.getExperimentId()));
    }

    Variant variant = experiment.getVariants().get(variantName);

    if (Objects.isNull(variant)) {
      throw new IllegalArgumentException(
          String.format(
              "Variant '%s' not found in experiment variant map for experiment: %s",
              variantName, experiment.getExperimentId()));
    }

    return UserExperimentMap.builder()
        .experimentId(experiment.getExperimentId())
        .experimentName(experiment.getName())
        .experimentKey(experiment.getExperimentKey())
        .variant(variant)
        .variantName(variantName)
        .status(AllocationStatus.ASSIGNED.name())
        .assignedAt(Instant.now(Clock.systemUTC()).toEpochMilli())
        .build();
  }
}
