package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AdminService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AdminService interface for managing tags and name availability operations.
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
  public Single<NameAvailabilityResponse> isExperimentNameAvailable(
      String projectKey, String experimentName) {
    return adminDAO
        .isExperimentNameAvailable(projectKey, experimentName)
        .map(
            isAvailable -> {
              if (isAvailable) {
                return new NameAvailabilityResponse(
                    true,
                    String.format(
                        "Experiment name '%s' is available in project '%s'",
                        experimentName, projectKey));
              } else {
                return new NameAvailabilityResponse(
                    false,
                    String.format(
                        "Experiment name '%s' already exists in project '%s'",
                        experimentName, projectKey));
              }
            })
        .doOnSuccess(
            response ->
                log.info(
                    "Checked experiment name availability for projectKey={} and experimentName={}",
                    projectKey,
                    experimentName))
        .doOnError(
            error ->
                log.error(
                    "Error checking experiment name availability for projectKey={} and experimentName={}: {}",
                    projectKey,
                    experimentName,
                    error.getMessage(),
                    error))
        .onErrorResumeNext(
            error ->
                Single.error(
                    new RestException(
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getErrorCode(),
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getErrorMessage(),
                        ErrorEnum.REST_EXPERIMENT_NAME_CHECK_FAILED.getHttpStatusCode(),
                        error)));
  }

  /** {@inheritDoc} */
  @Override
  public Single<GetExperimentHistoryResponse> getExperimentHistory(
      String projectKey, String experimentId, int limit, int page) {
    int offset = (page - 1) * limit;
    return adminDAO
        .fetchExperimentHistory(projectKey, experimentId, limit, offset)
        .map(
            result ->
                GetExperimentHistoryResponse.builder()
                    .experimentId(experimentId)
                    .history(result.historyEntries())
                    .pagination(
                        GetExperimentHistoryResponse.PaginationMeta.builder()
                            .currentPage(page)
                            .pageSize(limit)
                            .totalCount(result.totalCount())
                            .build())
                    .build())
        .doOnSuccess(
            res ->
                log.info(
                    "Received experiment history for projectKey={} and experimentId={}, page={}, limit={}",
                    projectKey,
                    experimentId,
                    page,
                    limit))
        .doOnError(
            err ->
                log.error(
                    "Error getting experiment history for projectKey={} and experimentId={}",
                    projectKey,
                    experimentId,
                    err))
        .onErrorResumeNext(
            error ->
                Single.error(
                    new RestException(
                        ErrorEnum.REST_FETCH_EXPERIMENT_HISTORY_FAILED.getErrorCode(),
                        ErrorEnum.REST_FETCH_EXPERIMENT_HISTORY_FAILED.getErrorMessage(),
                        ErrorEnum.REST_FETCH_EXPERIMENT_HISTORY_FAILED.getHttpStatusCode(),
                        error)));
  }
}
