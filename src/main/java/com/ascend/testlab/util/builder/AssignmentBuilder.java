package com.ascend.testlab.util.builder;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.Variant;
import java.util.Objects;

/**
 * Builder class for creating UserExperimentMap instances. Provides a consistent way to build
 * assignment objects across the application.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public class AssignmentBuilder {

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

    Variant variant = experiment.getVariant().get(variantName);
    return UserExperimentMap.builder()
        .experimentId(experiment.getExperimentId())
        .experimentName(experiment.getName())
        .variant(variant)
        .variantName(variantName)
        .status(Constants.STATUS_ASSIGNED)
        .assignedAt(System.currentTimeMillis())
        .build();
  }
}
