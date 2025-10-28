package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentServiceImpl implements ExperimentService {

  private ExperimentDAO experimentDAO;

  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return experimentDAO
        .getExperiment(projectId, experimentId)
        .doOnError(error -> log.error("Error fetching experiment for projectId: {}, experimentId: {}", 
            projectId, experimentId, error))
            .onErrorResumeNext(throwable -> {
                if (throwable instanceof NoSuchElementException) {
                    log.warn("Experiment not found for projectId: {}, experimentId: {}", projectId, experimentId);
                    return Single.error(new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND));
                } else if (throwable instanceof RestException) {
                    return Single.error(throwable);
                } else {
                    log.error("Unexpected error while fetching experiment", throwable);
                    return Single.error(new RestException(ErrorEnum.DATABASE_ERROR));
                }
            });
  }

  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectId, FilterExperimentsRequest request) {
    
    // Check if we need to filter by tag or owner
    boolean hasTagFilter = request.hasTagFilter();
    boolean hasOwnerFilter = request.hasOwnerFilter();

    // Fetch tag IDs, owner IDs, and all experiments in parallel
    Single<Set<String>> tagIdsSingle = hasTagFilter
        ? experimentDAO.getExperimentIdsByTags(projectId, request.getTag()) 
        : Single.just(new HashSet<>());

    Single<Set<String>> ownerIdsSingle = hasOwnerFilter
        ? experimentDAO.getExperimentIdsByOwners(projectId, request.getOwner()) 
        : Single.just(new HashSet<>());

    // Fetch all experiments (filtered by status, type, name only) in parallel
    Single<List<Experiment>> allExperimentsSingle = experimentDAO.fetchExperiments(projectId, request);

    // Fetch all three in parallel
    return Single.zip(
        tagIdsSingle,
        ownerIdsSingle,
        allExperimentsSingle,
        (tagIds, ownerIds, allExperiments) -> {
          // Filter experiments by tag and owner IDs
          List<Experiment> filteredExperiments = filterExperimentsByTagAndOwner(
              allExperiments, tagIds, ownerIds, hasTagFilter, hasOwnerFilter);
          
          if (filteredExperiments.isEmpty()) {
            log.info("No experiments match tag/owner filters for projectId: {}", projectId);
            return List.<Experiment>of();
          }
          
          return filteredExperiments;
        }
    ).map(filteredExperiments -> {
      List<Experiment> paginatedResults = applyPagination(filteredExperiments, request);
      return buildPaginatedResponse(filteredExperiments, paginatedResults, request);
    }).onErrorResumeNext(error -> {
      log.error("Error filtering experiments for projectId: {}", projectId, error);
      return Single.just(buildEmptyPaginatedResponse(request));
    });
  }

  /**
   * Filter experiments based on tag and owner IDs.
   * Uses intersection for AND logic (both filters must match).
   */
  private List<Experiment> filterExperimentsByTagAndOwner(
      List<Experiment> experiments, Set<String> tagIds, Set<String> ownerIds,
      boolean hasTagFilter, boolean hasOwnerFilter) {
      if(!hasTagFilter && !hasOwnerFilter) return experiments;
    
    Set<String> validExperimentIds;
    
    if (hasTagFilter && hasOwnerFilter) {
      // Experiments must be in BOTH sets (AND logic)
      Set<String> intersection = new HashSet<>(tagIds);
      intersection.retainAll(ownerIds);
      validExperimentIds = intersection;
    } else if (hasTagFilter) {
      validExperimentIds = tagIds;
    } else {
      validExperimentIds = ownerIds;
    }
    
    // Filter experiments to only include those with valid IDs
    return experiments.stream()
        .filter(exp -> validExperimentIds.contains(exp.getExperimentId()))
        .collect(Collectors.toList());
  }

  private List<Experiment> applyPagination(List<Experiment> experiments, FilterExperimentsRequest request) {
    if (request.getLimit() != null && request.getLimit() > 0) {
      int offset = request.getPage() != null && request.getPage() > 0 
          ? (request.getPage() - 1) * request.getLimit() : 0;
      int limit = request.getLimit();
      return experiments.stream()
          .skip(offset)
          .limit(limit)
          .collect(Collectors.toList());
    }
    return experiments;
  }

  /**
   * Build paginated response with metadata from all experiments and paginated results.
   */
  private FilterExperimentsResponse buildPaginatedResponse(
      List<Experiment> allExperiments, 
      List<Experiment> paginatedResults, 
      FilterExperimentsRequest request) {
    
    int totalItems = allExperiments.size();
    int pageSize = request.getLimit() != null ? request.getLimit() : totalItems;
    int currentPage = request.getPage() != null ? request.getPage() : 1;

    FilterExperimentsResponse.PaginationMeta paginationMeta = FilterExperimentsResponse.PaginationMeta.builder()
        .currentPage(currentPage)
        .pageSize(pageSize)
        .totalCount(totalItems)
        .build();

    return FilterExperimentsResponse.builder()
        .experimentList(paginatedResults)
        .pagination(paginationMeta)
        .build();
  }

  /**
   * Build empty paginated response when no results match filters.
   */
  private FilterExperimentsResponse buildEmptyPaginatedResponse(FilterExperimentsRequest request) {
    int pageSize = request.getLimit() != null ? request.getLimit() : 20;
    int currentPage = request.getPage() != null ? request.getPage() : 1;

    FilterExperimentsResponse.PaginationMeta paginationMeta = FilterExperimentsResponse.PaginationMeta.builder()
        .currentPage(currentPage)
        .pageSize(pageSize)
        .totalCount(0)
        .build();

    return FilterExperimentsResponse.builder()
        .experimentList(List.of())
        .pagination(paginationMeta)
        .build();
  }
}
