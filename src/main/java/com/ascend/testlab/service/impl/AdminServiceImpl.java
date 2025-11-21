package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.ExperimentKeyAvailabilityResponse;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AdminService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
    return adminDAO
        .isExperimentKeyAvailable(projectKey, experimentKey)
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
              if (response.pagination().totalCount() == 0 && request.getPage() == 1) {
                return Single.error(new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND));
              }
              return Single.just(response);
            })
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error getting experiment history for projectKey={} and experimentId={}: {}",
                  request.getProjectKey(),
                  request.getExperimentId(),
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FETCH_EXPERIMENT_HISTORY_FAILED, err)));
            });
  }
}
