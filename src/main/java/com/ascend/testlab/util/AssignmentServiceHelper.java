package com.ascend.testlab.util;

import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.util.strategy.RandomVariantAssignment;
import com.ascend.testlab.util.strategy.RoundRobinVariantAssignment;
import com.ascend.testlab.util.strategy.VariantAssignmentStrategy;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for assignment service with utility methods for filtering and variant assignment
 */
@Slf4j
public class AssignmentServiceHelper {

  private static final String STRATEGY_ROUND_ROBIN = "ROUND_ROBIN";
  private static final String STATUS_ASSIGNED = "ASSIGNED";

  /**
   * Filters experiments that are not yet assigned to the user
   */
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

  /**
   * Filters experiments based on exclusive flag
   * If user has exclusive experiment, no new assignments
   * If unassigned has exclusive, user should have no other assignments
   */
  public static List<Experiment> applyExclusiveFilter(
      List<Experiment> experiments, List<UserExperimentMap> userAssignments) {

    boolean userHasExclusive =
        userAssignments.stream().anyMatch(UserExperimentMap::getIsExclusive);

    if (userHasExclusive) {
      log.debug("User has exclusive experiment, filtering all new assignments");
      return Collections.emptyList();
    }

    // If user has non-exclusive assignments, filter out exclusive experiments
    if (!userAssignments.isEmpty()) {
      return experiments.stream()
          .filter(exp -> !exp.getIsExclusive())
          .collect(Collectors.toList());
    }

    return experiments;
  }

  /**
   * Filters experiments based on API path
   */
  public static List<Experiment> applyApiPathFilter(
      List<Experiment> experiments, AssignmentRequest request) {

    String apiPath = request.getApiPath();
    if (apiPath == null) {
      return experiments;
    }

    return experiments.stream()
        .filter(exp -> exp.getApiPaths() != null && exp.getApiPaths().containsKey(apiPath))
        .filter(
            exp ->
                request.getIsStatic() == null
                    || exp.getIsStatic().equals(request.getIsStatic()))
        .collect(Collectors.toList());
  }

  /**
   * Filters experiments based on entities
   */
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

  /**
   * Filters experiments based on cohort assignment domain
   */
  public static List<Experiment> applyCohortFilter(
      List<Experiment> experiments, List<String> userCohorts) {

    if (userCohorts == null || userCohorts.isEmpty()) {
      // Return only non-cohort experiments
      return experiments.stream()
          .filter(
              exp ->
                  exp.getAssignmentDomain() == null
                      || !"COHORT".equals(exp.getAssignmentDomain().getDomainType()))
          .collect(Collectors.toList());
    }

    Set<String> cohortSet = new HashSet<>(userCohorts);

    return experiments.stream()
        .filter(
            exp -> {
              if (exp.getAssignmentDomain() == null) {
                return true;
              }
              if ("COHORT".equals(exp.getAssignmentDomain().getDomainType())) {
                List<String> expCohorts = exp.getAssignmentDomain().getCohortIds();
                return expCohorts != null
                    && expCohorts.stream().anyMatch(cohortSet::contains);
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  /**
   * Filters experiments based on trait assignment domain
   */
  public static List<Experiment> applyTraitFilter(
      List<Experiment> experiments, String userTrait) {

    if (userTrait == null) {
      // Return only non-trait experiments
      return experiments.stream()
          .filter(
              exp ->
                  exp.getAssignmentDomain() == null
                      || !"TRAIT".equals(exp.getAssignmentDomain().getDomainType()))
          .collect(Collectors.toList());
    }

    return experiments.stream()
        .filter(
            exp -> {
              if (exp.getAssignmentDomain() == null) {
                return true;
              }
              if ("TRAIT".equals(exp.getAssignmentDomain().getDomainType())) {
                return userTrait.equals(exp.getAssignmentDomain().getTraitName());
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  /**
   * Selects variant based on distribution strategy
   */
  public static Variant selectVariant(Experiment experiment, String userId) {
    if (experiment.getDistributionStrategy() == null) {
      log.warn("No distribution strategy defined for experiment {}", experiment.getExperimentId());
      return null;
    }

    String strategyType = experiment.getDistributionStrategy().getStrategyType();
    String apiPath = experiment.getApiPaths().keySet().iterator().next();
    List<Variant> variants = experiment.getApiPaths().get(apiPath).getVariants();

    VariantAssignmentStrategy strategy = getStrategy(strategyType);
    return strategy.selectVariant(variants, userId);
  }

  /**
   * Creates UserExperimentMap from experiment and variant
   */
  public static UserExperimentMap createUserExperimentMap(
      Experiment experiment, Variant variant, String apiPath) {

    return UserExperimentMap.builder()
        .experimentId(experiment.getExperimentId())
        .experimentName(experiment.getName())
        .variant(variant.getVariantName())
        .status(STATUS_ASSIGNED)
        .variables(variant.getVariables())
        .isStatic(experiment.getIsStatic())
        .isExclusive(experiment.getIsExclusive())
        .entities(experiment.getEntities())
        .apiPath(apiPath)
        .assignedAt(System.currentTimeMillis())
        .build();
  }

  /**
   * Applies guest user carryover - transfers guest assignments to logged-in user
   */
  public static List<UserExperimentMap> applyGuestCarryover(
      List<UserExperimentMap> guestAssignments,
      List<Experiment> activeExperiments,
      List<UserExperimentMap> currentUserAssignments) {

    if (guestAssignments == null || guestAssignments.isEmpty()) {
      return Collections.emptyList();
    }

    Set<UUID> activeExpIds =
        activeExperiments.stream()
            .map(Experiment::getExperimentId)
            .collect(Collectors.toSet());

    Set<UUID> userAssignedIds =
        currentUserAssignments.stream()
            .map(UserExperimentMap::getExperimentId)
            .collect(Collectors.toSet());

    return guestAssignments.stream()
        .filter(ga -> activeExpIds.contains(ga.getExperimentId()))
        .filter(ga -> !userAssignedIds.contains(ga.getExperimentId()))
        .collect(Collectors.toList());
  }

  /**
   * Filters experiments for specific API path from user assignments
   */
  public static List<UserExperimentMap> filterByApiPath(
      List<UserExperimentMap> assignments, String apiPath, List<String> entities) {

    return assignments.stream()
        .filter(a -> apiPath.equals(a.getApiPath()))
        .filter(
            a -> {
              if (entities == null || entities.isEmpty()) {
                return true;
              }
              Set<String> entitySet = new HashSet<>(entities);
              return a.getEntities() == null
                  || a.getEntities().isEmpty()
                  || entitySet.containsAll(a.getEntities());
            })
        .collect(Collectors.toList());
  }

  /**
   * Groups assignments by API path for complete assignment map
   */
  public static Map<String, List<UserExperimentMap>> groupByApiPath(
      List<UserExperimentMap> assignments) {

    return assignments.stream()
        .collect(Collectors.groupingBy(UserExperimentMap::getApiPath));
  }

  private static VariantAssignmentStrategy getStrategy(String strategyType) {
    if (STRATEGY_ROUND_ROBIN.equals(strategyType)) {
      return new RoundRobinVariantAssignment();
    }
    return new RandomVariantAssignment();
  }
}

