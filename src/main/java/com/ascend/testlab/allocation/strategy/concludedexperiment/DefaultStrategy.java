package com.ascend.testlab.allocation.strategy.concludedexperiment;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy that applies winning variant for all users, regardless of previous assignment status.
 * Overrides existing assignments and adds new ones for unassigned users.
 *
 * <p>Use Case: Immediate full adoption of winning variant for all users.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class DefaultStrategy implements ConcludedStrategy {

  @Override
  public boolean shouldOverride(
      Experiment concludedExperiment, List<UserExperimentMap> existingAssignments) {

    log.debug(
        "Applying winning variant for experiment {} to all users",
        concludedExperiment.getExperimentId());

    return true;
  }

  @Override
  public String getStrategyName() {
    return "BOTH";
  }
}
