package com.ascend.testlab.util.filter;

import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UnassignedExperimentFilter extends AbstractExperimentFilter {

  private final List<UserExperimentMap> userAssignments;

  @Override
  protected List<Experiment> applyFilter(List<Experiment> experiments) {
    if (userAssignments == null || userAssignments.isEmpty()) {
      log.debug("No user assignments, all experiments are unassigned");
      return experiments;
    }

    Set<UUID> assignedExperimentIds =
        userAssignments.stream()
            .map(UserExperimentMap::getExperimentId)
            .collect(Collectors.toSet());

    return experiments.stream()
        .filter(
            exp -> {
              boolean isUnassigned = !assignedExperimentIds.contains(exp.getExperimentId());
              if (!isUnassigned) {
                log.trace("Experiment {} already assigned to user", exp.getExperimentId());
              }
              return isUnassigned;
            })
        .collect(Collectors.toList());
  }
}
