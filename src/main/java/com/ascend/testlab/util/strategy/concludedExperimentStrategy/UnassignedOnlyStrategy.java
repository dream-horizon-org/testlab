package com.ascend.testlab.util.strategy.concludedExperimentStrategy;

import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy that only adds winning variant for users who were NOT assigned to the experiment. Does
 * not override existing assignments.
 *
 * <p>Use Case: Gradual rollout of winning variant to users who didn't participate in the original
 * experiment.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class UnassignedOnlyStrategy implements ConcludedStrategy {

  @Override
  public boolean shouldOverride(
      Experiment concludedExperiment, List<UserExperimentMap> existingAssignments) {

    boolean wasNotAssigned =
        existingAssignments.stream()
            .noneMatch(
                assignment ->
                    assignment.getExperimentId().equals(concludedExperiment.getExperimentId()));

    if (wasNotAssigned) {
      log.debug(
          "User was not assigned to experiment {}, will add winning variant",
          concludedExperiment.getExperimentId());
    } else {
      log.debug(
          "User was already assigned to experiment {}, skipping",
          concludedExperiment.getExperimentId());
    }

    return wasNotAssigned;
  }

  @Override
  public String getStrategyName() {
    return "UNASSIGNED_ONLY";
  }
}
