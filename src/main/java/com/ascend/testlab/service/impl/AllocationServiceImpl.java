package com.ascend.testlab.service.impl;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.AllocationStatus;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.response.AllocationResponse;
import com.ascend.testlab.dto.response.GetAllocationsResponse;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.service.AllocationService;
import com.ascend.testlab.service.CohortService;
import com.ascend.testlab.util.builder.AssignmentBuilder;
import com.ascend.testlab.util.filter.ExperimentFilterChainBuilder;
import com.ascend.testlab.util.filter.experimentFilter.ExperimentFilter;
import com.ascend.testlab.util.helper.VariantSelector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AllocationService interface. Handles experiment allocation logic including
 * filtering, variant selection, and user allocation persistence.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AllocationService
 */
@Slf4j
public class AllocationServiceImpl implements AllocationService {

  private final AllocationDAO allocationDAO;
  private final CohortService cohortService;
  private final ObjectMapper objectMapper;

  @Inject
  public AllocationServiceImpl(
      AllocationDAO allocationDAO, CohortService cohortService, ObjectMapper objectMapper) {
    this.allocationDAO = allocationDAO;
    this.cohortService = cohortService;
    this.objectMapper = objectMapper;
  }

  /** {@inheritDoc} */
  @Override
  public Single<AllocationResponse> allotExperiments(
      String projectKey, AllocationRequest allocationRequest) {

    String guestId = allocationRequest != null ? allocationRequest.getGuestId() : null;
    String userId = allocationRequest != null ? allocationRequest.getUserId() : null;

    log.info(
        "Allocation request for user: {}, guestId: {}, project: {}", userId, guestId, projectKey);
    return Single.zip(
            fetchCohortsForUser(userId, projectKey),
            fetchExperimentsAndAllocation(projectKey, userId, guestId, allocationRequest),
            (cohorts, data) -> {
              List<Experiment> activeExperiments =
                  objectMapper.convertValue(data.get("experiments"), new TypeReference<>() {});
              List<UserExperimentMap> userAllocations =
                  objectMapper.convertValue(data.get("userAllocations"), new TypeReference<>() {});
              List<UserExperimentMap> guestAllocations =
                  objectMapper.convertValue(data.get("guestAllocations"), new TypeReference<>() {});

              List<UserExperimentMap> activeAllocations =
                  filterActiveAllocations(userAllocations, activeExperiments);

              List<Experiment> filteredExperiments =
                  applyFilters(activeExperiments, activeAllocations, allocationRequest, cohorts);

              if (filteredExperiments.isEmpty()) {
                log.debug("No experiments passed filters for user {}", userId);
                return buildResponse(activeAllocations);
              }

              return assignWithLock(
                  userId,
                  projectKey,
                  activeExperiments,
                  filteredExperiments,
                  activeAllocations,
                  guestAllocations);
            })
        .flatMap(res -> res)
        .doOnSuccess(
            response ->
                log.info(
                    "Allocation completed for user: {}, total experiments: {}",
                    userId,
                    response.getExperimentMap().size()))
        .doOnError(error -> log.error("Error in allocation flow for user: {}", userId, error));
  }

  // For concluded, uncomment and test

  //    @Override
  //    public Single<List<UserExperimentMap>> applyConcludedExperiments(
  //            String userId, UUID projectKey, ConcludedStrategy strategy) {
  //
  //        log.debug(
  //                "Applying concluded experiments for user {} with strategy {}",
  //                userId,
  //                strategy.getStrategyName());
  //
  //        return Single.zip(
  //                        fetchConcludedExperiments(projectKey),
  //                        getUserAssignments(userId, projectKey),
  //                        (concludedExperiments, existingAssignments) -> {
  //                            List<UserExperimentMap> assignmentsToApply = new ArrayList<>();
  //
  //                            for (Experiment concludedExperiment : concludedExperiments) {
  //                                if (strategy.shouldOverride(concludedExperiment,
  // existingAssignments)) {
  ////                  UserExperimentMap concludedAssignment =
  ////                      createConcludedAssignment(concludedExperiment, existingAssignments);
  ////                  assignmentsToApply.add(concludedAssignment);
  //                                }
  //                            }
  //
  //                            log.debug(
  //                                    "Applying {} concluded experiment assignments for user {}",
  //                                    assignmentsToApply.size(),
  //                                    userId);
  //                            return assignmentsToApply;
  //                        })
  //                .flatMap(
  //                        assignmentsToApply -> {
  //                            if (assignmentsToApply.isEmpty()) {
  //                                log.debug("No concluded experiments to apply for user {}",
  // userId);
  //                                return Single.just(new ArrayList<UserExperimentMap>());
  //                            }
  //                            return insertUserAssignments(userId, projectKey, assignmentsToApply)
  //                                    .map(
  //                                            success ->
  //                                                    success ? assignmentsToApply : new
  // ArrayList<UserExperimentMap>());
  //                        })
  //                .onErrorReturn(
  //                        error -> {
  //                            log.error("Error applying concluded experiments for user {}",
  // userId, error);
  //                            return new ArrayList<>();
  //                        });
  //    }

  private Single<Map<String, List<?>>> fetchExperimentsAndAllocation(
      String projectKey, String userId, String guestId, AllocationRequest allocationRequest) {

    Single<List<Experiment>> experimentsSingle =
        allocationDAO.fetchActiveExperiments(projectKey, allocationRequest.getExperiments());

    Single<Map<String, List<UserExperimentMap>>> userAllocationsSingle =
        allocationDAO.getAllocations(List.of(userId, guestId), projectKey);

    return Single.zip(
            experimentsSingle,
            userAllocationsSingle,
            (experiments, userAllocations) -> {
              log.debug(
                  "Fetched {} active experiments, {} user allocations",
                  experiments.size(),
                  userAllocations.size());

              Map<String, List<?>> result = new HashMap<>();
              result.put("experiments", experiments);
              result.put("userAllocations", userAllocations.get(userId));
              result.put(
                  "guestAllocations", userAllocations.getOrDefault(guestId, new ArrayList<>()));
              return result;
            })
        .doOnError(error -> log.error("Error in allocation flow for user: {}", userId, error));
  }

  /** {@inheritDoc} */
  @Override
  public Single<GetAllocationsResponse> getAllocations(String userId, String projectKey) {
    return allocationDAO
        .getAllocations(userId, projectKey)
        .map(GetAllocationsResponse::new)
        .doOnSuccess(
            res -> {
              log.info("Successfully fetched allocations for user: {}", userId);
            })
        .doOnError(error -> log.error("Error in allocation flow for user: {}", userId, error));
  }

  private List<Experiment> applyFilters(
      List<Experiment> experiments,
      List<UserExperimentMap> userAllocations,
      AllocationRequest request,
      List<String> cohorts) {

    log.debug(
        "Applying filters to {} experiments with {} cohorts", experiments.size(), cohorts.size());

    ExperimentFilter experimentFilter =
        ExperimentFilterChainBuilder.buildFromRequest(request, userAllocations, cohorts);

    List<Experiment> filtered = experimentFilter.filter(experiments);

    log.debug("After all filters: {} experiments remain", filtered.size());
    return filtered;
  }

  private Single<List<String>> fetchCohortsForUser(String userId, String projectKey) {
    return cohortService
        .getUserCohorts(userId, projectKey)
        .switchIfEmpty(Single.just(Collections.emptyList()))
        .onErrorReturnItem(Collections.emptyList());
  }

  /**
   * Filters user allocations to only include those for active experiments.
   *
   * @param userAllocations all user allocations
   * @param activeExperiments list of active experiments
   * @return filtered list of allocations for active experiments only
   */
  private List<UserExperimentMap> filterActiveAllocations(
      List<UserExperimentMap> userAllocations, List<Experiment> activeExperiments) {

    Set<UUID> activeExperimentIds =
        activeExperiments.stream().map(Experiment::getExperimentId).collect(Collectors.toSet());

    List<UserExperimentMap> activeAllocations =
        userAllocations.stream()
            .filter(allocation -> activeExperimentIds.contains(allocation.getExperimentId()))
            .collect(Collectors.toList());

    log.debug(
        "Filtered {} active allocations out of {} total allocations",
        activeAllocations.size(),
        userAllocations.size());

    return activeAllocations;
  }

  private Single<AllocationResponse> assignWithLock(
      String userId,
      String projectKey,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> currentAllocations,
      List<UserExperimentMap> guestAllocations) {

    log.debug("Attempting to assign {} experiments to user {}", filteredExperiments.size(), userId);

    return allocationDAO
        .acquireUserLock(userId, projectKey)
        .flatMap(
            lockAcquired -> {
              if (!lockAcquired) {
                log.warn(
                    "Failed to acquire lock for user {}, returning current allocations", userId);
                return buildResponse(currentAllocations);
              }

              log.debug("Lock acquired for user {}", userId);

              return performAssignments(
                      userId,
                      projectKey,
                      allExperiments,
                      filteredExperiments,
                      currentAllocations,
                      guestAllocations)
                  .flatMap(
                      newAssignments -> {
                        List<UserExperimentMap> allAssignments =
                            new ArrayList<>(currentAllocations);

                        allAssignments.addAll(newAssignments);

                        return releaseUserLock(userId, projectKey)
                            .flatMap(released -> buildResponse(allAssignments));
                      })
                  .onErrorResumeNext(
                      error -> {
                        log.error("Error during allocation, releasing lock", error);
                        return releaseUserLock(userId, projectKey)
                            .flatMap(released -> Single.error(error));
                      });
            });
  }

  private Single<List<UserExperimentMap>> performAssignments(
      String userId,
      String projectKey,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> latestAssignments,
      List<UserExperimentMap> guestAssignments) {

    Set<UUID> assignedIds = new HashSet<>();
    latestAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

    return applyGuestCarryover(userId, projectKey, allExperiments, guestAssignments, assignedIds)
        .flatMap(
            carryoverAssignments -> {
              carryoverAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

              List<Experiment> stillUnassigned =
                  filteredExperiments.stream()
                      .filter(exp -> !assignedIds.contains(exp.getExperimentId()))
                      .toList();

              if (stillUnassigned.isEmpty()) {
                log.debug("No unassigned experiments after guest carryover for user {}", userId);
                return Single.just(carryoverAssignments);
              }

              return Observable.fromIterable(stillUnassigned)
                  .flatMap(
                      experiment ->
                          assignSingleExperiment(experiment, userId, projectKey).toObservable())
                  .toList()
                  .flatMap(
                      newAssignments -> {
                        if (newAssignments.isEmpty()) {
                          log.debug("No new allocations created for user {}", userId);
                          return Single.just(carryoverAssignments);
                        }

                        Map<String, String> variantCountMap = new HashMap<>();
                        for (UserExperimentMap allocation : newAssignments) {
                          String key =
                              allocation.getExperimentId().toString()
                                  + Constants.COLON
                                  + allocation.getVariant().getDisplayName();
                          variantCountMap.put(key, allocation.getVariant().getDisplayName());
                        }

                        return allocationDAO
                            .insertAllocationsAndIncrementCounts(
                                userId, projectKey, newAssignments, variantCountMap)
                            .map(
                                saved -> {
                                  if (saved) {
                                    log.info(
                                        "Transactionally saved {} new allocations for user {}",
                                        newAssignments.size(),
                                        userId);
                                  } else {
                                    log.warn("Failed to save some allocations for user {}", userId);
                                  }

                                  List<UserExperimentMap> allNewAssignments =
                                      new ArrayList<>(carryoverAssignments);
                                  allNewAssignments.addAll(newAssignments);
                                  return allNewAssignments;
                                });
                      });
            })
        .doOnError(
            error -> {
              log.error("Error during allocation, releasing lock", error);
            });
  }

  private Maybe<UserExperimentMap> assignSingleExperiment(
      Experiment experiment, String userId, String projectKey) {

    return allocationDAO
        .checkThreshold(projectKey, experiment)
        .flatMapMaybe(
            underThreshold -> {
              if (!underThreshold) {
                log.debug(
                    "Experiment {} reached threshold, skipping", experiment.getExperimentId());
                return Maybe.empty();
              }

              String selectedVariant = VariantSelector.selectVariant(experiment, userId);

              if (Objects.isNull(selectedVariant)) {
                log.warn("No variant selected for experiment {}", experiment.getExperimentId());
                return Maybe.empty();
              }

              log.debug(
                  "Selected variant {} for user {} for experiment {}",
                  selectedVariant,
                  userId,
                  experiment.getExperimentId());

              return Maybe.just(
                  Objects.requireNonNull(createUserExperimentMap(experiment, selectedVariant)));
            })
        .doOnError(
            err -> {
              log.error(err.getMessage(), err);
            });
  }

  private UserExperimentMap createUserExperimentMap(Experiment experiment, String variant) {
    try {
      return AssignmentBuilder.buildAssignment(experiment, variant);
    } catch (IllegalArgumentException e) {
      log.error("Cannot create UserExperimentMap: {}", e.getMessage());
      return null;
    }
  }

  private Single<List<UserExperimentMap>> applyGuestCarryover(
      String userId,
      String projectKey,
      List<Experiment> allExperiments,
      List<UserExperimentMap> guestAssignments,
      Set<UUID> userAssignedIds) {

    if (Objects.isNull(guestAssignments) || guestAssignments.isEmpty()) {
      log.debug("No guest allocations to carry over for user {}", userId);
      return Single.just(Collections.emptyList());
    }

    Map<UUID, Experiment> experimentMap =
        allExperiments.stream().collect(Collectors.toMap(Experiment::getExperimentId, exp -> exp));

    List<UserExperimentMap> carryoverAssignments =
        guestAssignments.stream()
            .filter(
                ga -> {
                  if (!experimentMap.containsKey(ga.getExperimentId())) {
                    log.trace(
                        "Skipping guest allocation for inactive experiment: {}",
                        ga.getExperimentId());
                    return false;
                  }

                  if (userAssignedIds.contains(ga.getExperimentId())) {
                    log.trace(
                        "User already assigned to experiment: {}, skipping carryover",
                        ga.getExperimentId());
                    return false;
                  }
                  return true;
                })
            .map(
                ga ->
                    UserExperimentMap.builder()
                        .experimentId(ga.getExperimentId())
                        .experimentName(ga.getExperimentName())
                        .variant(ga.getVariant())
                        .status(AllocationStatus.ASSIGNED.name())
                        .variant(ga.getVariant())
                        .assignedAt(System.currentTimeMillis())
                        .build())
            .collect(Collectors.toList());

    if (carryoverAssignments.isEmpty()) {
      log.debug("No guest allocations eligible for carryover for user {}", userId);
      return Single.just(Collections.emptyList());
    }

    log.info("Carrying over {} guest allocations to user {}", carryoverAssignments.size(), userId);

    Map<String, String> variantCountMap = new HashMap<>();
    for (UserExperimentMap allocation : carryoverAssignments) {
      String key =
          allocation.getExperimentId().toString()
              + Constants.COLON
              + allocation.getVariant().getDisplayName();
      variantCountMap.put(key, allocation.getVariant().getDisplayName());
    }

    return allocationDAO
        .insertAllocationsAndIncrementCounts(
            userId, projectKey, carryoverAssignments, variantCountMap)
        .map(
            saved -> {
              if (saved) {
                log.info(
                    "Transactionally carried over {} allocations from guest to user {}",
                    carryoverAssignments.size(),
                    userId);
                return carryoverAssignments;
              } else {
                log.warn("Failed to save some carryover allocations for user {}", userId);
                return Collections.<UserExperimentMap>emptyList();
              }
            })
        .onErrorReturnItem(Collections.emptyList());
  }

  private Single<Boolean> releaseUserLock(String userId, String projectKey) {
    return allocationDAO
        .releaseUserLock(userId, projectKey)
        .doOnSuccess(released -> log.debug("Released lock for user {}: {}", userId, released))
        .onErrorReturnItem(true);
  }

  private Single<AllocationResponse> buildResponse(List<UserExperimentMap> allocations) {
    return Single.just(AllocationResponse.builder().experimentMap(allocations).build());
  }
}
