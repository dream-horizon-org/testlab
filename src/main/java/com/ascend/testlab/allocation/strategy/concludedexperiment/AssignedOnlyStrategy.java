package com.ascend.testlab.allocation.strategy.concludedexperiment;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy that only overrides variants for users who were already assigned to the experiment. Does
 * not add winning variant for users who were not part of the experiment.
 *
 * <p>Use Case: Update experiment participants with winning variant without affecting users who
 * never participated.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class AssignedOnlyStrategy implements ConcludedStrategy {

  @Override
  public boolean shouldOverride(
      Experiment concludedExperiment, List<UserExperimentMap> existingAssignments) {

    boolean wasAssigned =
        existingAssignments.stream()
            .anyMatch(
                assignment ->
                    assignment.getExperimentId().equals(concludedExperiment.getExperimentId()));

    if (wasAssigned) {
      log.debug(
          "User was assigned to experiment {}, will override with winning variant",
          concludedExperiment.getExperimentId());
    } else {
      log.debug(
          "User was not assigned to experiment {}, skipping override",
          concludedExperiment.getExperimentId());
    }

    return wasAssigned;
  }

  @Override
  public String getStrategyName() {
    return "ASSIGNED_ONLY";
  }
}
