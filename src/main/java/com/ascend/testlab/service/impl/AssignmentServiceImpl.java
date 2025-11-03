package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.AssignmentDAO;
import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.AssignmentResponse;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.service.AssignmentService;
import com.ascend.testlab.util.filter.ExperimentFilter;
import com.ascend.testlab.util.filter.ExperimentFilterChainBuilder;
import com.ascend.testlab.util.helper.VariantSelector;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AssignmentServiceImpl implements AssignmentService {

  private final AssignmentDAO assignmentDAO;

  @Override
  public Single<AssignmentResponse> assignExperiments(
      UUID tenantId, String userId, AssignmentRequest assignmentRequest) {

    log.info(
        "Assignment request for user: {}, tenant: {}, entities: {}",
        userId,
        tenantId,
        assignmentRequest.getEntities());

    return fetchExperimentsAndAssignments(tenantId, userId)
        .flatMap(
            data -> {
              List<Experiment> activeExperiments = data.getKey();
              List<UserExperimentMap> userAssignments = data.getValue();

              List<Experiment> filteredExperiments =
                  applyFilters(activeExperiments, userAssignments, assignmentRequest);

              if (filteredExperiments.isEmpty()) {
                log.debug("No experiments passed filters for user {}", userId);
                return buildResponse(userAssignments);
              }

              return assignWithLock(userId, tenantId, filteredExperiments, userAssignments);
            })
        .doOnSuccess(
            response ->
                log.info(
                    "Assignment completed for user: {}, total experiments: {}",
                    userId,
                    response.getApiAssignmentMap().size()))
        .doOnError(error -> log.error("Error in assignment flow for user: {}", userId, error));
  }

  private Single<Map.Entry<List<Experiment>, List<UserExperimentMap>>>
      fetchExperimentsAndAssignments(UUID tenantId, String userId) {

    return Single.zip(
        assignmentDAO.fetchActiveExperiments(tenantId),
        assignmentDAO.getUserAssignments(userId, tenantId),
        (experiments, assignments) -> {
          log.debug(
              "Fetched {} active experiments and {} user assignments",
              experiments.size(),
              assignments.size());
          return Map.entry(experiments, assignments);
        });
  }

  private List<Experiment> applyFilters(
      List<Experiment> experiments,
      List<UserExperimentMap> userAssignments,
      AssignmentRequest request) {

    log.debug("Applying filters to {} experiments", experiments.size());

    ExperimentFilter filterChain =
        ExperimentFilterChainBuilder.buildFromRequest(request, userAssignments);

    List<Experiment> filtered = filterChain.filter(experiments);

    log.debug("After all filters: {} experiments remain", filtered.size());
    return filtered;
  }

  private Single<AssignmentResponse> assignWithLock(
      String userId,
      UUID tenantId,
      List<Experiment> experiments,
      List<UserExperimentMap> currentAssignments) {

    log.debug("Attempting to assign {} experiments to user {}", experiments.size(), userId);

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

              return performAssignments(userId, tenantId, experiments, currentAssignments)
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
      List<Experiment> experiments,
      List<UserExperimentMap> latestAssignments) {

    Set<UUID> assignedIds = new HashSet<>();
    latestAssignments.forEach(a -> assignedIds.add(a.getExperimentId()));

    List<Experiment> stillUnassigned =
        experiments.stream().filter(exp -> !assignedIds.contains(exp.getExperimentId())).toList();

    if (stillUnassigned.isEmpty()) {
      log.debug("No unassigned experiments after lock re-check for user {}", userId);
      return Single.just(Collections.emptyList());
    }

    return Observable.fromIterable(stillUnassigned)
        .flatMapSingle(experiment -> assignSingleExperiment(experiment, userId, tenantId))
        .filter(Objects::nonNull)
        .toList()
        .flatMap(
            newAssignments -> {
              if (newAssignments.isEmpty()) {
                log.debug("No new assignments created for user {}", userId);
                return Single.just(newAssignments);
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
                        return newAssignments;
                      });
            });
  }

  private Single<UserExperimentMap> assignSingleExperiment(
      Experiment experiment, String userId, UUID tenantId) {

    return assignmentDAO
        .checkThreshold(experiment)
        .flatMap(
            underThreshold -> {
              if (!underThreshold) {
                log.debug(
                    "Experiment {} reached threshold, skipping", experiment.getExperimentId());
                return Single.just(null);
              }

              Variant selectedVariant = VariantSelector.selectVariant(experiment, userId);

              if (selectedVariant == null) {
                log.warn("No variant selected for experiment {}", experiment.getExperimentId());
                return Single.just(null);
              }

              return assignmentDAO
                  .incrementVariantCount(
                      experiment.getExperimentId(), selectedVariant.getVariantName())
                  .map(
                      newCount -> {
                        log.debug(
                            "Assigned variant {} to user {} for experiment {}, count: {}",
                            selectedVariant.getVariantName(),
                            userId,
                            experiment.getExperimentId(),
                            newCount);

                        return createUserExperimentMap(experiment, selectedVariant);
                      });
            })
        .onErrorReturnItem(null);
  }

  private UserExperimentMap createUserExperimentMap(Experiment experiment, Variant variant) {

    if (experiment == null || variant == null) {
      log.error("Cannot create UserExperimentMap with null experiment or variant");
      return null;
    }

    return UserExperimentMap.builder()
        .experimentId(experiment.getExperimentId())
        .experimentName(experiment.getName())
        .variant(variant.getVariantName())
        .status("ASSIGNED")
        .variables(variant.getVariables())
        .entities(experiment.getEntities())
        .assignedAt(System.currentTimeMillis())
        .build();
  }

  private Single<Boolean> releaseUserLock(String userId, UUID tenantId) {
    return assignmentDAO
        .releaseUserLock(userId, tenantId)
        .doOnSuccess(released -> log.debug("Released lock for user {}: {}", userId, released))
        .onErrorReturnItem(true);
  }

  private Single<AssignmentResponse> buildResponse(List<UserExperimentMap> assignments) {
    return Single.just(AssignmentResponse.builder().apiAssignmentMap(assignments).build());
  }
}
