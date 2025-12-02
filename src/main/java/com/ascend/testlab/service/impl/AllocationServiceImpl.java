package com.ascend.testlab.service.impl;

import com.ascend.testlab.allocation.builder.AssignmentBuilder;
import com.ascend.testlab.allocation.builder.ExperimentFilterChainBuilder;
import com.ascend.testlab.allocation.filter.ExperimentFilter;
import com.ascend.testlab.allocation.selector.VariantSelector;
import com.ascend.testlab.allocation.strategy.concludedexperiment.ConcludedStrategy;
import com.ascend.testlab.allocation.strategy.concludedexperiment.ConcludedStrategyFactory;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.AllocationStatus;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.experiment.WinningVariant;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.dto.response.AllocationsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AllocationService;
import com.ascend.testlab.service.CohortService;
import com.dream11.rest.util.ExceptionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  /**
   * Constructor for AllocationServiceImpl.
   *
   * @param allocationDAO the allocation DAO
   * @param cohortService the cohort service
   * @param objectMapper the object mapper
   */
  @Inject
  public AllocationServiceImpl(
      AllocationDAO allocationDAO, CohortService cohortService, ObjectMapper objectMapper) {
    this.allocationDAO = allocationDAO;
    this.cohortService = cohortService;
    this.objectMapper = objectMapper;
  }

  /** {@inheritDoc} */
  @Override
  public Single<AllocationsResponse> allotExperiments(
      String projectKey, AllocationRequest allocationRequest) {

    String guestId = allocationRequest != null ? allocationRequest.getStableId() : null;
    String userId = allocationRequest != null ? allocationRequest.getUserId() : null;
    String effectiveId = getEffectiveIdentifier(userId, guestId);

    log.info(
        "Allocation request for user: {}, guestId: {}, effectiveId: {}, project: {}",
        userId,
        guestId,
        effectiveId,
        projectKey);
    return Single.zip(
            fetchCohortsForUser(effectiveId, projectKey),
            fetchExperimentsAndAllocation(projectKey, userId, guestId, allocationRequest),
            (cohorts, data) -> {
              List<Experiment> allExperiments =
                  objectMapper.convertValue(data.get("experiments"), new TypeReference<>() {});
              List<UserExperimentMap> userAllocations =
                  objectMapper.convertValue(data.get("userAllocations"), new TypeReference<>() {});
              List<UserExperimentMap> guestAllocations =
                  objectMapper.convertValue(data.get("guestAllocations"), new TypeReference<>() {});

              List<Experiment> liveExperiments =
                  allExperiments.stream()
                      .filter(e -> e.getStatus() == ExperimentStatus.LIVE)
                      .toList();
              List<Experiment> concludedExperiments =
                  allExperiments.stream()
                      .filter(e -> e.getStatus() == ExperimentStatus.CONCLUDED)
                      .toList();

              // Use user allocations if logged in, otherwise use guest allocations
              List<UserExperimentMap> currentAllocations =
                  shouldApplyGuestCarryover(userId) ? userAllocations : guestAllocations;

              List<UserExperimentMap> activeAllocations =
                  filterActiveAllocations(currentAllocations, liveExperiments);

              List<UserExperimentMap> concludedAllocations =
                  applyConcludedExperiments(concludedExperiments, currentAllocations);

              List<Experiment> filteredExperiments =
                  applyFilters(liveExperiments, activeAllocations, allocationRequest, cohorts);

              if (filteredExperiments.isEmpty()) {
                log.debug("No experiments passed filters for identifier {}", effectiveId);
                List<UserExperimentMap> allAllocations = new ArrayList<>(activeAllocations);
                allAllocations.addAll(concludedAllocations);
                return buildResponse(allAllocations);
              }

              return assignWithLock(
                      userId,
                      guestId,
                      projectKey,
                      cohorts,
                      liveExperiments,
                      filteredExperiments,
                      activeAllocations,
                      guestAllocations)
                  .map(
                      response -> {
                        List<UserExperimentMap> allAllocations =
                            new ArrayList<>(response.experimentMap());
                        allAllocations.addAll(concludedAllocations);
                        return new AllocationsResponse(allAllocations);
                      });
            })
        .flatMap(res -> res)
        .doOnSuccess(
            response ->
                log.info(
                    "Allocation completed for user: {}, total experiments: {}",
                    userId,
                    response.experimentMap().size()))
        .doOnError(error -> log.error("Error in allocation flow for user: {}", userId, error));
  }

  /** {@inheritDoc} */
  @Override
  public Single<AllocationsResponse> getAllocations(String userId, String projectKey) {
    return allocationDAO
        .getAllocations(userId, projectKey)
        .map(AllocationsResponse::new)
        .doOnSuccess(
            res -> {
              log.info("Successfully fetched allocations for user: {}", userId);
            })
        .doOnError(error -> log.error("Error in allocation flow for user: {}", userId, error));
  }

  @Override
  public Single<UserExperimentMap> reallocateExperiment(
      String projectKey, ReallocateRequest reallocateRequest) {
    String userId = reallocateRequest.getUserId();
    return allocationDAO
        .acquireUserLock(userId, projectKey)
        .flatMap(
            lockAcquired -> {
              if (!lockAcquired) {
                log.warn("Failed to acquire lock for user {}, cannot reallocate", userId);
                throw ExceptionUtil.getException(ErrorEnum.REST_REALLOCATION_FAILED);
              }
              log.debug("Lock acquired for user {} reallocation", userId);

              return performReallocation(projectKey, reallocateRequest)
                  .doFinally(() -> releaseUserLock(userId, projectKey))
                  .onErrorResumeNext(
                      error -> {
                        log.error("Error during reallocation", error);
                        return Single.error(error);
                      });
            });
  }

  private Single<UserExperimentMap> performReallocation(
      String projectKey, ReallocateRequest reallocateRequest) {
    UUID experimentId = UUID.fromString(reallocateRequest.getExperimentId());
    return Single.zip(
            allocationDAO.fetchActiveExperiment(projectKey, reallocateRequest.getExperimentId()),
            allocationDAO.getAllocations(reallocateRequest.getUserId(), projectKey),
            (experiment, userAssignments) -> {
              if (Objects.isNull(experiment) || Objects.isNull(experiment.getExperimentId())) {
                throw ExceptionUtil.getException(ErrorEnum.ACTIVE_EXPERIMENT_NOT_FOUND);
              }
              log.debug("Fetched experiment {} for reallocation", experiment);
              UserExperimentMap currentAssignment =
                  userAssignments.stream()
                      .filter(assignment -> assignment.getExperimentId().equals(experimentId))
                      .findFirst()
                      .orElse(null);

              if (Objects.isNull(currentAssignment)) {
                throw ExceptionUtil.getException(ErrorEnum.NO_ALLOTMENT_FOUND);
              }
              log.debug("Fetched assignment {} for reallocation", currentAssignment);

              return Map.entry(experiment, currentAssignment);
            })
        .flatMap(
            data -> {
              Experiment experiment = data.getKey();
              UserExperimentMap currentAssignment = data.getValue();

              Variant newVariant = experiment.getVariants().get(reallocateRequest.getVariantName());
              if (Objects.isNull(newVariant)) {
                throw ExceptionUtil.getException(ErrorEnum.INVALID_VARIANT_FOUND);
              }
              if (currentAssignment.getVariantName().equals(reallocateRequest.getVariantName())) {
                throw ExceptionUtil.getException(ErrorEnum.VARIANT_ALREADY_ASSIGNED);
              }

              UserExperimentMap newAssignment =
                  AssignmentBuilder.buildAssignment(experiment, reallocateRequest.getVariantName());
              log.debug("OldAssignment {}", currentAssignment);
              log.debug("NewAssignment {}", newAssignment);
              return allocationDAO
                  .reallocateUserVariant(
                      projectKey,
                      currentAssignment.getVariantName(),
                      newAssignment,
                      reallocateRequest)
                  .doOnError(
                      error ->
                          log.error(
                              "Failed to reallocate user {} for experiment {}",
                              reallocateRequest.getUserId(),
                              experimentId,
                              error));
            });
  }

  /**
   * Applies concluded experiments using the default strategy. For each concluded experiment, builds
   * an allocation using the winning variant.
   *
   * @param concludedExperiments list of concluded experiments with winning variants
   * @param existingAllocations user's existing allocations
   * @return list of allocations for concluded experiments
   */
  private List<UserExperimentMap> applyConcludedExperiments(
      List<Experiment> concludedExperiments, List<UserExperimentMap> existingAllocations) {

    if (concludedExperiments == null || concludedExperiments.isEmpty()) {
      return Collections.emptyList();
    }

    ConcludedStrategy strategy = ConcludedStrategyFactory.getDefaultStrategy();
    List<UserExperimentMap> concludedAllocations = new ArrayList<>();

    for (Experiment experiment : concludedExperiments) {
      if (!strategy.shouldOverride(experiment, existingAllocations)) {
        log.debug(
            "Strategy {} skipping concluded experiment {}",
            strategy.getStrategyName(),
            experiment.getExperimentId());
        continue;
      }

      WinningVariant winningVariant = experiment.getWinningVariant();
      if (Objects.isNull(winningVariant) || Objects.isNull(winningVariant.getVariantName())) {
        log.warn(
            "Concluded experiment {} has no winning variant defined", experiment.getExperimentId());
        continue;
      }

      String variantName = winningVariant.getVariantName();
      if (Objects.isNull(experiment.getVariants())
          || !experiment.getVariants().containsKey(variantName)) {
        log.warn(
            "Winning variant {} not found in experiment {}",
            variantName,
            experiment.getExperimentId());
        continue;
      }

      try {
        UserExperimentMap allocation = AssignmentBuilder.buildAssignment(experiment, variantName);
        concludedAllocations.add(allocation);
        log.debug(
            "Built concluded allocation for experiment {} with winning variant {}",
            experiment.getExperimentId(),
            variantName);
      } catch (IllegalArgumentException e) {
        log.error(
            "Failed to build concluded allocation for experiment {}: {}",
            experiment.getExperimentId(),
            e.getMessage());
      }
    }

    log.info("Applied {} concluded experiment allocations", concludedAllocations.size());
    return concludedAllocations;
  }

  private Single<Map<String, List<?>>> fetchExperimentsAndAllocation(
      String projectKey, String userId, String guestId, AllocationRequest allocationRequest) {

    List<String> userIds = Stream.of(userId, guestId).filter(Objects::nonNull).toList();

    Single<List<Experiment>> experimentsSingle =
        allocationDAO.fetchActiveExperiments(projectKey, allocationRequest.getExperimentKeys());

    Single<Map<String, List<UserExperimentMap>>> userAllocationsSingle =
        allocationDAO.getAllocations(userIds, projectKey);

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
              result.put(
                  "userAllocations", userAllocations.getOrDefault(userId, new ArrayList<>()));
              result.put(
                  "guestAllocations", userAllocations.getOrDefault(guestId, new ArrayList<>()));
              return result;
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

  private Single<AllocationsResponse> assignWithLock(
      String userId,
      String stableId,
      String projectKey,
      List<String> cohorts,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> currentAllocations,
      List<UserExperimentMap> guestAllocations) {

    String effectiveId = getEffectiveIdentifier(userId, stableId);
    log.debug(
        "Attempting to assign {} experiments to identifier {}",
        filteredExperiments.size(),
        effectiveId);

    return allocationDAO
        .acquireUserLock(effectiveId, projectKey)
        .flatMap(
            lockAcquired -> {
              if (!lockAcquired) {
                log.warn(
                    "Failed to acquire lock for identifier {}, returning current allocations",
                    effectiveId);
                return buildResponse(currentAllocations);
              }

              log.debug("Lock acquired for identifier {}", effectiveId);

              return performAssignments(
                      userId,
                      stableId,
                      projectKey,
                      cohorts,
                      allExperiments,
                      filteredExperiments,
                      currentAllocations,
                      guestAllocations)
                  .flatMap(
                      newAssignments -> {
                        List<UserExperimentMap> allAssignments =
                            new ArrayList<>(currentAllocations);

                        allAssignments.addAll(newAssignments);

                        return releaseUserLock(effectiveId, projectKey)
                            .flatMap(released -> buildResponse(allAssignments));
                      })
                  .onErrorResumeNext(
                      error -> {
                        log.error("Error during allocation, releasing lock", error);
                        return releaseUserLock(effectiveId, projectKey)
                            .flatMap(released -> Single.error(error));
                      });
            });
  }

  private Single<List<UserExperimentMap>> performAssignments(
      String userId,
      String stableId,
      String projectKey,
      List<String> cohorts,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> latestAssignments,
      List<UserExperimentMap> guestAssignments) {

    String effectiveId = getEffectiveIdentifier(userId, stableId);
    Set<UUID> assignedIds = new HashSet<>();
    latestAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

    // Apply guest carryover only for logged-in users
    Single<List<UserExperimentMap>> carryoverSingle;
    if (shouldApplyGuestCarryover(userId)) {
      carryoverSingle =
          applyGuestCarryover(
              effectiveId, projectKey, allExperiments, guestAssignments, assignedIds);
    } else {
      log.debug("Skipping guest carryover for identifier {}", effectiveId);
      carryoverSingle = Single.just(Collections.emptyList());
    }

    return carryoverSingle
        .flatMap(
            carryoverAssignments -> {
              carryoverAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

              List<Experiment> stillUnassigned =
                  filteredExperiments.stream()
                      .filter(exp -> !assignedIds.contains(exp.getExperimentId()))
                      .toList();

              if (stillUnassigned.isEmpty()) {
                log.debug(
                    "No unassigned experiments after processing for identifier {}", effectiveId);
                return Single.just(carryoverAssignments);
              }

              return Observable.fromIterable(stillUnassigned)
                  .flatMap(
                      experiment ->
                          assignSingleExperiment(experiment, userId, stableId, projectKey, cohorts)
                              .toObservable())
                  .toList()
                  .flatMap(
                      newAssignments -> {
                        if (newAssignments.isEmpty()) {
                          log.debug("No new allocations created for identifier {}", effectiveId);
                          return Single.just(carryoverAssignments);
                        }

                        Map<String, String> variantCountMap = new HashMap<>();
                        for (UserExperimentMap allocation : newAssignments) {
                          String key =
                              allocation.getExperimentId().toString()
                                  + Constants.COLON
                                  + allocation.getVariantName();
                          variantCountMap.put(key, allocation.getVariantName());
                        }

                        return allocationDAO
                            .insertAllocationsAndIncrementCounts(
                                effectiveId, projectKey, newAssignments, variantCountMap)
                            .map(
                                saved -> {
                                  if (saved) {
                                    log.info(
                                        "Transactionally saved {} new allocations for identifier {}",
                                        newAssignments.size(),
                                        effectiveId);
                                  } else {
                                    log.warn(
                                        "Failed to save some allocations for identifier {}",
                                        effectiveId);
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
      Experiment experiment,
      String userId,
      String stableId,
      String projectKey,
      List<String> cohorts) {

    String identifierForVariantSelection = getEffectiveIdentifier(userId, stableId);

    return allocationDAO
        .checkThreshold(projectKey, experiment)
        .flatMapMaybe(
            underThreshold -> {
              if (!underThreshold) {
                log.debug(
                    "Experiment {} reached threshold, skipping", experiment.getExperimentId());
                return Maybe.empty();
              }

              String selectedVariant =
                  VariantSelector.selectVariant(experiment, identifierForVariantSelection, cohorts);

              if (Objects.isNull(selectedVariant)) {
                log.warn("No variant selected for experiment {}", experiment.getExperimentId());
                return Maybe.empty();
              }

              log.debug(
                  "Selected variant {} for identifier {} for experiment {}",
                  selectedVariant,
                  identifierForVariantSelection,
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
          allocation.getExperimentId().toString() + Constants.COLON + allocation.getVariantName();
      variantCountMap.put(key, allocation.getVariantName());
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

  private Single<AllocationsResponse> buildResponse(List<UserExperimentMap> allocations) {
    return Single.just(new AllocationsResponse(allocations));
  }

  /**
   * Returns the effective identifier to use for allocation operations. Uses userId if present,
   * otherwise falls back to stableId.
   *
   * @param userId the user ID (may be null)
   * @param stableId the stable/guest ID (may be null)
   * @return the effective identifier for operations
   */
  private String getEffectiveIdentifier(String userId, String stableId) {
    return Objects.nonNull(userId) ? userId : stableId;
  }

  /**
   * Determines if guest carryover should be applied. Guest carryover only applies when userId is
   * NOT null (user has logged in).
   *
   * @param userId the user ID
   * @return true if guest carryover should be applied
   */
  private boolean shouldApplyGuestCarryover(String userId) {
    return Objects.nonNull(userId);
  }
}
