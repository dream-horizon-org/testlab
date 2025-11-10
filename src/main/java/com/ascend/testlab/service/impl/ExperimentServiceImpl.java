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
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentService. Handles business logic for experiment retrieval and
 * filtering operations, including error handling, pagination, and combining filters for tags and
 * owners.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentService
 * @see ExperimentDAO
 */
@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;

  @Inject
  public ExperimentServiceImpl(ExperimentDAO experimentDAO) {
    this.experimentDAO = experimentDAO;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to the DAO layer to fetch the experiment. Handles error translation:
   *
   * <ul>
   *   <li>NoSuchElementException -> EXPERIMENT_NOT_FOUND
   *   <li>RestException -> rethrown as-is
   *   <li>Other exceptions -> DATABASE_ERROR
   * </ul>
   */
  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return experimentDAO
        .getExperiment(projectId, experimentId)
        .doOnError(
            error ->
                log.error(
                    "Error fetching experiment for projectId: {}, experimentId: {}",
                    projectId,
                    experimentId,
                    error))
        .onErrorResumeNext(
            err -> {
              if (err instanceof NoSuchElementException) {
                log.warn(
                    "Experiment not found for projectId: {}, experimentId: {}",
                    projectId,
                    experimentId);
                return Single.error(
                          ErrorEnum.handleException(
                                  err, new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND)));
              } else {
                  log.error("Error in get Experiments for project {} and experimentID {} : {}", projectId, experimentId, err.getMessage());
                  return Single.error(ErrorEnum.handleException(
                        err, new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED, err)));
              }
            });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates filtering to the DAO layer. The DAO handles all filter combinations including
   * status, type, name, tags, and owners. Multiple filter values can be provided as comma-separated
   * strings for status, type, tag, and owner parameters.
   *
   * <p>Pagination is applied in the SQL query using LIMIT and OFFSET clauses, which is more
   * efficient than in-memory pagination. The total count is obtained using a window function in the
   * same query, eliminating the need for a separate COUNT query. Defaults to limit=20 and page=1 if
   * not specified.
   */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectId, FilterExperimentsRequest request) {
    return experimentDAO.filterExperiments(projectId, request)
            .onErrorResumeNext(err -> {
                log.error("Error in filter Experiments for project {}: {}", projectId, err.getMessage());
                return Single.error(ErrorEnum.handleException(err, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, err)));
            });
  }
}
