package com.ascend.testlab.service.impl;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.dao.AssignmentDAO;
import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.AssignmentResponse;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.service.AssignmentService;
import com.ascend.testlab.service.CohortService;
import com.ascend.testlab.util.builder.AssignmentBuilder;
import com.ascend.testlab.util.filter.ExperimentFilterChainBuilder;
import com.ascend.testlab.util.filter.experimentFilter.ExperimentFilter;
import com.ascend.testlab.util.helper.VariantSelector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of the AssignmentService interface. Handles experiment assignment logic including
 * filtering, variant selection, and user assignment persistence.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see AssignmentService
 */
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

  private final AssignmentDAO assignmentDAO;
  private final CohortService cohortService;
  private final ObjectMapper objectMapper;

  @Inject
  public AssignmentServiceImpl(
      AssignmentDAO assignmentDAO, CohortService cohortService, ObjectMapper objectMapper) {
    this.assignmentDAO = assignmentDAO;
    this.cohortService = cohortService;
    this.objectMapper = objectMapper;
  }

  @Override
  public Single<AssignmentResponse> assignExperiments(
      UUID tenantId, AssignmentRequest assignmentRequest) {

    String guestId = assignmentRequest != null ? assignmentRequest.getGuestId() : null;
    String userId = assignmentRequest != null ? assignmentRequest.getUserId() : null;

    log.info("Assignment request for user: {}, guestId: {}, tenant: {}", userId, guestId, tenantId);
    return Single.zip(
            fetchCohortsForUser(userId, tenantId),
            fetchExperimentsAndAssignments(tenantId, userId, guestId),
            (cohorts, data) -> {
              List<Experiment> activeExperiments =
                  objectMapper.convertValue(data.get("experiments"), new TypeReference<>() {});
              List<UserExperimentMap> userAssignments =
                  objectMapper.convertValue(data.get("userAssignments"), new TypeReference<>() {});
              List<UserExperimentMap> guestAssignments =
                  objectMapper.convertValue(data.get("guestAssignments"), new TypeReference<>() {});

              List<UserExperimentMap> activeUserAssignments =
                  filterActiveAssignments(userAssignments, activeExperiments);

              List<Experiment> filteredExperiments =
                  applyFilters(
                      activeExperiments, activeUserAssignments, assignmentRequest, cohorts);

              if (filteredExperiments.isEmpty()) {
                log.debug("No experiments passed filters for user {}", userId);
                return buildResponse(activeUserAssignments);
              }

              return assignWithLock(
                  userId,
                  tenantId,
                  activeExperiments,
                  filteredExperiments,
                  activeUserAssignments,
                  guestAssignments,
                  cohorts);
            })
        .flatMap(res -> res)
        .doOnSuccess(
            response ->
                log.info(
                    "Assignment completed for user: {}, total experiments: {}",
                    userId,
                    response.getExperimentMap().size()))
        .doOnError(error -> log.error("Error in assignment flow for user: {}", userId, error));
  }

  // For concluded, uncomment and test

  //    @Override
  //    public Single<List<UserExperimentMap>> applyConcludedExperiments(
  //            String userId, UUID tenantId, ConcludedStrategy strategy) {
  //
  //        log.debug(
  //                "Applying concluded experiments for user {} with strategy {}",
  //                userId,
  //                strategy.getStrategyName());
  //
  //        return Single.zip(
  //                        fetchConcludedExperiments(tenantId),
  //                        getUserAssignments(userId, tenantId),
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
  //                            return insertUserAssignments(userId, tenantId, assignmentsToApply)
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

  //    @Override
  //    public Single<UserExperimentMap> reassignExperiment(
  //            UUID tenantId, ReassignmentRequest reassignmentRequest) {
  //        String userId = reassignmentRequest != null ? reassignmentRequest.getUserId() : null;
  //        log.info(
  //                "Reassignment request for user: {}, experiment: {}, variant: {}, reason: {}",
  //                userId,
  //                reassignmentRequest.getExperimentId(),
  //                reassignmentRequest.getVariantName(),
  //                reassignmentRequest.getReason());
  //
  //        UUID experimentId;
  //        try {
  //            experimentId = UUID.fromString(reassignmentRequest.getExperimentId());
  //        } catch (IllegalArgumentException e) {
  //            log.error("Invalid experiment ID format: {}",
  // reassignmentRequest.getExperimentId());
  //            return Single.error(new IllegalArgumentException("Invalid experiment ID format"));
  //        }
  //
  //        return assignmentDAO
  //                .acquireUserLock(userId, tenantId)
  //                .flatMap(
  //                        lockAcquired -> {
  //                            if (!lockAcquired) {
  //                                log.warn("Failed to acquire lock for user {}, cannot reassign",
  // userId);
  //                                return Single.error(
  //                                        new RuntimeException("Failed to acquire user lock for
  // reassignment"));
  //                            }
  //
  //                            log.debug("Lock acquired for user {} reassignment", userId);
  //
  //                            return performReassignment(userId, tenantId, experimentId,
  // reassignmentRequest)
  //                                    .flatMap(
  //                                            updatedAssignment ->
  //                                                    releaseUserLock(userId, tenantId)
  //                                                            .flatMap(released ->
  // Single.just(updatedAssignment)))
  //                                    .onErrorResumeNext(
  //                                            error -> {
  //                                                log.error("Error during reassignment, releasing
  // lock", error);
  //                                                return releaseUserLock(userId, tenantId)
  //                                                        .flatMap(released ->
  // Single.error(error));
  //                                            });
  //                        });
  //    }

  private Single<Map<String, List<?>>> fetchExperimentsAndAssignments(
      UUID tenantId, String userId, String guestId) {

    Single<List<Experiment>> experimentsSingle = assignmentDAO.fetchActiveExperiments(tenantId);

    // todo : batch read
    Single<List<UserExperimentMap>> userAssignmentsSingle =
        assignmentDAO.getUserAssignments(userId, tenantId);

    Single<List<UserExperimentMap>> guestAssignmentsSingle;
    if (StringUtils.isNotBlank(guestId)) {
      log.debug("Fetching guest assignments for guestId: {}", guestId);
      guestAssignmentsSingle =
          assignmentDAO
              .getUserAssignments(guestId, tenantId)
              .onErrorReturnItem(Collections.emptyList());
    } else {
      guestAssignmentsSingle = Single.just(Collections.emptyList());
    }

    return Single.zip(
            experimentsSingle,
            userAssignmentsSingle,
            guestAssignmentsSingle,
            (experiments, userAssignments, guestAssignments) -> {
              log.debug(
                  "Fetched {} active experiments, {} user assignments, {} guest assignments",
                  experiments.size(),
                  userAssignments.size(),
                  guestAssignments.size());

              Map<String, List<?>> result = new HashMap<>();
              result.put("experiments", experiments);
              result.put("userAssignments", userAssignments);
              result.put("guestAssignments", guestAssignments);
              return result;
            })
        .doOnError(error -> log.error("Error in assignment flow for user: {}", userId, error));
  }

  private List<Experiment> applyFilters(
      List<Experiment> experiments,
      List<UserExperimentMap> userAssignments,
      AssignmentRequest request,
      List<String> cohorts) {

    log.debug(
        "Applying filters to {} experiments with {} cohorts", experiments.size(), cohorts.size());

    ExperimentFilter filterChain =
        ExperimentFilterChainBuilder.buildFromRequest(request, userAssignments, cohorts);

    List<Experiment> filtered = filterChain.filter(experiments);

    log.debug("After all filters: {} experiments remain", filtered.size());
    return filtered;
  }

  private Single<List<String>> fetchCohortsForUser(String userId, UUID tenantId) {
    return cohortService
        .getUserCohorts(userId, tenantId)
        .switchIfEmpty(Single.just(Collections.emptyList()))
        .onErrorReturnItem(Collections.emptyList());
  }

  /**
   * Filters user assignments to only include those for active experiments.
   *
   * @param userAssignments all user assignments
   * @param activeExperiments list of active experiments
   * @return filtered list of assignments for active experiments only
   */
  private List<UserExperimentMap> filterActiveAssignments(
      List<UserExperimentMap> userAssignments, List<Experiment> activeExperiments) {

    Set<UUID> activeExperimentIds =
        activeExperiments.stream().map(Experiment::getExperimentId).collect(Collectors.toSet());

    List<UserExperimentMap> activeAssignments =
        userAssignments.stream()
            .filter(assignment -> activeExperimentIds.contains(assignment.getExperimentId()))
            .collect(Collectors.toList());

    log.debug(
        "Filtered {} active assignments out of {} total assignments",
        activeAssignments.size(),
        userAssignments.size());

    return activeAssignments;
  }

  private Single<AssignmentResponse> assignWithLock(
      String userId,
      UUID tenantId,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> currentAssignments,
      List<UserExperimentMap> guestAssignments,
      List<String> cohorts) {

    log.debug("Attempting to assign {} experiments to user {}", filteredExperiments.size(), userId);

    return assignmentDAO
        .acquireUserLock(userId, tenantId)
        .flatMap(
            lockAcquired -> {
              if (!lockAcquired) {
                log.warn(
                    "Failed to acquire lock for user {}, returning current assignments", userId);
                return buildResponse(currentAssignments);
              }

              log.debug("Lock acquired for user {}", userId);

              return performAssignments(
                      userId,
                      tenantId,
                      allExperiments,
                      filteredExperiments,
                      currentAssignments,
                      guestAssignments,
                      cohorts)
                  .flatMap(
                      newAssignments -> {
                        List<UserExperimentMap> allAssignments =
                            new ArrayList<>(currentAssignments);

                        allAssignments.addAll(newAssignments);

                        return releaseUserLock(userId, tenantId)
                            .flatMap(released -> buildResponse(allAssignments));
                      })
                  .onErrorResumeNext(
                      error -> {
                        log.error("Error during assignment, releasing lock", error);
                        return releaseUserLock(userId, tenantId)
                            .flatMap(released -> Single.error(error));
                      });
            });
  }

  private Single<List<UserExperimentMap>> performAssignments(
      String userId,
      UUID tenantId,
      List<Experiment> allExperiments,
      List<Experiment> filteredExperiments,
      List<UserExperimentMap> latestAssignments,
      List<UserExperimentMap> guestAssignments,
      List<String> userCohorts) {

    Set<UUID> assignedIds = new HashSet<>();
    latestAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

    return applyGuestCarryover(userId, tenantId, allExperiments, guestAssignments, assignedIds)
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
                  .flatMapSingle(experiment -> assignSingleExperiment(experiment, userId, tenantId))
                  .filter(Objects::nonNull)
                  .toList()
                  .flatMap(
                      newAssignments -> {
                        if (newAssignments.isEmpty()) {
                          log.debug("No new assignments created for user {}", userId);
                          return Single.just(carryoverAssignments);
                        }

                        return assignmentDAO
                            .insertUserAssignments(userId, tenantId, newAssignments)
                            .map(
                                saved -> {
                                  if (saved) {
                                    log.info(
                                        "Saved {} new assignments for user {}",
                                        newAssignments.size(),
                                        userId);
                                  } else {
                                    log.warn("Failed to save some assignments for user {}", userId);
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
              log.error("Error during assignment, releasing lock", error);
            });
  }

  private Single<UserExperimentMap> assignSingleExperiment(
      Experiment experiment, String userId, UUID tenantId) {

    return assignmentDAO
        .checkThreshold(tenantId, experiment)
        .flatMap(
            underThreshold -> {
              if (!underThreshold) {
                log.debug(
                    "Experiment {} reached threshold, skipping", experiment.getExperimentId());
                return Single.just(null);
              }

              String selectedVariant = VariantSelector.selectVariant(experiment, userId);

              if (Objects.isNull(selectedVariant)) {
                log.warn("No variant selected for experiment {}", experiment.getExperimentId());
                // to do throw exception
                return Single.just(null);
              }

              return assignmentDAO
                  .incrementVariantCount(tenantId, experiment.getExperimentId(), selectedVariant)
                  .map(
                      newCount -> {
                        log.debug(
                            "Assigned variant {} to user {} for experiment {}, count: {}",
                            selectedVariant,
                            userId,
                            experiment.getExperimentId(),
                            newCount);

                        return createUserExperimentMap(experiment, selectedVariant);
                      });
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
      UUID tenantId,
      List<Experiment> allExperiments,
      List<UserExperimentMap> guestAssignments,
      Set<UUID> userAssignedIds) {

    if (Objects.isNull(guestAssignments) || guestAssignments.isEmpty()) {
      log.debug("No guest assignments to carry over for user {}", userId);
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
                        "Skipping guest assignment for inactive experiment: {}",
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
                        .status(Constants.STATUS_ASSIGNED)
                        .variant(ga.getVariant())
                        .assignedAt(System.currentTimeMillis())
                        .build())
            .collect(Collectors.toList());

    if (carryoverAssignments.isEmpty()) {
      log.debug("No guest assignments eligible for carryover for user {}", userId);
      return Single.just(Collections.emptyList());
    }

    log.info("Carrying over {} guest assignments to user {}", carryoverAssignments.size(), userId);

    return Observable.fromIterable(carryoverAssignments)
        .flatMapSingle(
            assignment ->
                assignmentDAO
                    .incrementVariantCount(
                        tenantId,
                        assignment.getExperimentId(),
                        assignment.getVariant().getDisplayName())
                    .map(count -> assignment))
        .toList()
        .flatMap(
            assignments ->
                assignmentDAO
                    .insertUserAssignments(userId, tenantId, assignments)
                    .map(
                        saved -> {
                          if (saved) {
                            log.info(
                                "Successfully carried over {} assignments from guest to user {}",
                                assignments.size(),
                                userId);
                          } else {
                            log.warn(
                                "Failed to save some carryover assignments for user {}", userId);
                          }
                          return assignments;
                        })
                    .onErrorReturnItem(Collections.emptyList()));
  }

  private Single<Boolean> releaseUserLock(String userId, UUID tenantId) {
    return assignmentDAO
        .releaseUserLock(userId, tenantId)
        .doOnSuccess(released -> log.debug("Released lock for user {}: {}", userId, released))
        .onErrorReturnItem(true);
  }

  private Single<AssignmentResponse> buildResponse(List<UserExperimentMap> assignments) {
    return Single.just(AssignmentResponse.builder().experimentMap(assignments).build());
  }

  //  private Single<UserExperimentMap> performReassignment(
  //      String userId, UUID tenantId, UUID experimentId, ReassignmentRequest reassignmentRequest)
  // {
  //
  //    return Single.zip(
  //            assignmentDAO.fetchActiveExperiments(tenantId),
  //            assignmentDAO.getUserAssignments(userId, tenantId),
  //            (experiments, userAssignments) -> {
  //              Experiment experiment =
  //                  experiments.stream()
  //                      .filter(exp -> exp.getExperimentId().equals(experimentId))
  //                      .findFirst()
  //                      .orElse(null);
  //
  //              if (Objects.isNull(experiment)) {
  //                throw new RuntimeException(
  //                    "Experiment " + experimentId + " not found or not active");
  //              }
  //
  //              UserExperimentMap currentAssignment =
  //                  userAssignments.stream()
  //                      .filter(assignment -> assignment.getExperimentId().equals(experimentId))
  //                      .findFirst()
  //                      .orElse(null);
  //
  //              return Map.entry(experiment, currentAssignment);
  //            })
  //        .flatMap(
  //            data -> {
  //              Experiment experiment = data.getKey();
  //              UserExperimentMap currentAssignment = data.getValue();
  //
  //              String newVariantName = reassignmentRequest.getVariantName();
  //              Variant newVariant;
  //
  //              if (newVariantName != null && !newVariantName.isEmpty()) {
  //
  //                newVariant = experiment.getVariant().get(newVariantName);
  //                if (Objects.isNull(newVariant)) {
  //                  return Single.error(
  //                      new IllegalArgumentException(
  //                          "Variant " + newVariantName + " not found in experiment"));
  //                }
  //              } else {
  //
  //                newVariant = VariantSelector.selectVariant(experiment, userId);
  //                if (Objects.isNull(newVariant)) {
  //                  return Single.error(
  //                      new RuntimeException("Failed to select variant for reassignment"));
  //                }
  //              }
  //
  //              if (currentAssignment != null
  //                  &&
  // currentAssignment.getVariant().getVariantName().equals(newVariant.getVariantName())
  //                  && !reassignmentRequest.isForceReassign()) {
  //                log.info(
  //                    "User {} already assigned to variant {} for experiment {}, skipping
  // reassignment",
  //                    userId,
  //                    newVariant.getVariantName(),
  //                    experimentId);
  //                return Single.just(currentAssignment);
  //              }
  //
  //              String oldVariantName =
  //                  Objects.nonNull(currentAssignment)
  //                      ? currentAssignment.getVariant().getVariantName()
  //                      : null;
  //
  //              return variantCountManager
  //                  .updateForReassignment(experimentId, oldVariantName,
  // newVariant.getVariantName())
  //                  .flatMap(
  //                      success -> {
  //                        if (!success) {
  //                          log.warn("Failed to update variant counts for reassignment");
  //                        }
  //
  //                        UserExperimentMap updatedAssignment =
  //                            AssignmentBuilder.buildAssignment(
  //                                experiment, newVariant);
  //
  //                        return assignmentDAO
  //                            .updateUserAssignment(userId, tenantId, updatedAssignment)
  //                            .map(
  //                                saved -> {
  //                                  if (saved) {
  //                                    log.info(
  //                                        "Successfully reassigned user {} from variant {} to {}
  // for experiment {}. Reason: {}",
  //                                        userId,
  //                                        oldVariantName,
  //                                        newVariant.getVariantName(),
  //                                        experimentId,
  //                                        reassignmentRequest.getReason());
  //                                  } else {
  //                                    log.error(
  //                                        "Failed to save reassignment for user {} experiment {}",
  //                                        userId,
  //                                        experimentId);
  //                                  }
  //                                  return updatedAssignment;
  //                                });
  //                      });
  //            });

}
