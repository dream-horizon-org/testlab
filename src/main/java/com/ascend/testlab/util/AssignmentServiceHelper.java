package com.ascend.testlab.util;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.util.strategy.RandomVariantAssignment;
import com.ascend.testlab.util.strategy.RoundRobinVariantAssignment;
import com.ascend.testlab.util.strategy.VariantAssignmentStrategy;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentServiceHelper {

  private static final String STRATEGY_ROUND_ROBIN = "ROUND_ROBIN";
  private static final String STATUS_ASSIGNED = "ASSIGNED";

  /** Filters experiments that are not yet assigned to the user */
  public static List<Experiment> getUnassignedExperiments(
      List<Experiment> activeExperiments, List<UserExperimentMap> userAssignments) {

    Set<UUID> assignedExperimentIds =
        userAssignments.stream()
            .map(UserExperimentMap::getExperimentId)
            .collect(Collectors.toSet());

    return activeExperiments.stream()
        .filter(exp -> !assignedExperimentIds.contains(exp.getExperimentId()))
        .collect(Collectors.toList());
  }

  /** Filters experiments based on entities */
  public static List<Experiment> applyEntityFilter(
      List<Experiment> experiments, List<String> requestEntities) {

    if (requestEntities == null || requestEntities.isEmpty()) {
      return experiments;
    }

    Set<String> entitySet = new HashSet<>(requestEntities);

    return experiments.stream()
        .filter(
            exp ->
                exp.getEntities() == null
                    || exp.getEntities().isEmpty()
                    || entitySet.containsAll(exp.getEntities()))
        .collect(Collectors.toList());
  }

  /** Filters experiments based on cohort assignment domain */
  public static List<Experiment> applyCohortFilter(
      List<Experiment> experiments, List<String> userCohorts) {

    if (userCohorts == null || userCohorts.isEmpty()) {
      // Return only non-cohort experiments
      return experiments.stream()
          .filter(
              exp ->
                  exp.getAssignmentDomain() == null || !"COHORT".equals(exp.getAssignmentDomain()))
          .collect(Collectors.toList());
    }

    Set<String> cohortSet = new HashSet<>(userCohorts);

    return experiments.stream()
        .filter(
            exp -> {
              if (exp.getAssignmentDomain() == null) {
                return true;
              }
              if ("COHORT".equals(exp.getAssignmentDomain())) {
                List<String> expCohorts = Arrays.asList(exp.getCohorts().split(","));
                return expCohorts != null && expCohorts.stream().anyMatch(cohortSet::contains);
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  /** Selects variant based on distribution strategy */
  public static Variant selectVariant(Experiment experiment, String userId) {
    if (experiment.getDistributionStrategy() == null) {
      log.warn("No distribution strategy defined for experiment {}", experiment.getExperimentId());
      return null;
    }

    DistributionStrategy strategyType = experiment.getDistributionStrategy();
    // todo
    List<Variant> variants = new ArrayList<>();

    VariantAssignmentStrategy strategy = getStrategy(strategyType);
    return strategy.selectVariant(variants, userId);
  }

  /** Creates UserExperimentMap from experiment and variant */
  public static UserExperimentMap createUserExperimentMap(
      Experiment experiment, Variant variant, String apiPath) {

    return UserExperimentMap.builder()
        .experimentId(experiment.getExperimentId())
        .experimentName(experiment.getName())
        .variant(variant.getVariantName())
        .status(STATUS_ASSIGNED)
        .variables(variant.getVariables())
        .entities(experiment.getEntities())
        .assignedAt(System.currentTimeMillis())
        .build();
  }

  /** Applies guest user carryover - transfers guest assignments to logged-in user */
  public static List<UserExperimentMap> applyGuestCarryover(
      List<UserExperimentMap> guestAssignments,
      List<Experiment> activeExperiments,
      List<UserExperimentMap> currentUserAssignments) {

    if (guestAssignments == null || guestAssignments.isEmpty()) {
      return Collections.emptyList();
    }

    Set<UUID> activeExpIds =
        activeExperiments.stream().map(Experiment::getExperimentId).collect(Collectors.toSet());

    Set<UUID> userAssignedIds =
        currentUserAssignments.stream()
            .map(UserExperimentMap::getExperimentId)
            .collect(Collectors.toSet());

    return guestAssignments.stream()
        .filter(ga -> activeExpIds.contains(ga.getExperimentId()))
        .filter(ga -> !userAssignedIds.contains(ga.getExperimentId()))
        .collect(Collectors.toList());
  }

  private static VariantAssignmentStrategy getStrategy(DistributionStrategy strategyType) {
    if (STRATEGY_ROUND_ROBIN.equals(strategyType)) {
      return new RoundRobinVariantAssignment();
    }
    return new RandomVariantAssignment();
  }
}
