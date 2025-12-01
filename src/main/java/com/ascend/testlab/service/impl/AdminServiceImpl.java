package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.ExperimentKeyAvailabilityResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AdminService;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AdminService interface for managing tags and key availability operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminService
 * @see AdminDAO
 */
@Slf4j
public class AdminServiceImpl implements AdminService {

  /** The admin DAO. */
  private final AdminDAO adminDAO;

  /**
   * Constructor for the AdminServiceImpl.
   *
   * @param adminDAO the admin DAO
   */
  @Inject
  public AdminServiceImpl(AdminDAO adminDAO) {
    this.adminDAO = adminDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<TagsResponse> getTags(String projectKey) {
    return adminDAO
        .fetchTags(projectKey)
        .map(TagsResponse::new)
        .onErrorResumeNext(
            err -> {
              log.error("Error in list tags for project {}: {}", projectKey, err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FETCH_TAGS_FAILED, err)));
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<ExperimentKeyAvailabilityResponse> isExperimentKeyAvailable(
      String projectKey, String experimentKey) {
    String normalizedExperimentKey = CommonUtil.normalizeExperimentKey(experimentKey);
    return adminDAO
        .isExperimentKeyAvailable(projectKey, normalizedExperimentKey)
        .map(ExperimentKeyAvailabilityResponse::new)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error checking experiment key availability for projectKey={} and experimentKey={}: {}",
                  projectKey,
                  experimentKey,
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_EXPERIMENT_KEY_CHECK_FAILED, err)));
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<ExperimentHistoryResponse> getExperimentHistory(ExperimentHistoryRequest request) {
    return adminDAO
        .fetchExperimentHistory(request)
        .flatMap(
            response -> {
              // if history is empty, it means one of these two things:
              // 1. The experiment does not exist
              // 2. The experiment has no history with the given offset and limit
              if (response.history().isEmpty()) {
                return adminDAO
                    .getExperimentHistoryCount(request.getProjectKey(), request.getExperimentId())
                    .map(
                        historyCount -> {
                          if (historyCount <= 0)
                            throw new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND);
                          return ExperimentHistoryResponse.builder()
                              .experiment_id(request.getExperimentId())
                              .history(List.of())
                              .pagination(
                                  PaginationMeta.builder()
                                      .current_page(request.getPage())
                                      .page_size(0)
                                      .total_count(historyCount)
                                      .has_next(false)
                                      .build())
                              .build();
                        });
              } else return Single.just(response);
            })
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error getting experiment history for projectKey={} and experiment_id={}: {}",
                  request.getProjectKey(),
                  request.getExperimentId(),
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FETCH_EXPERIMENT_HISTORY_FAILED, err)));
            });
  }
}
