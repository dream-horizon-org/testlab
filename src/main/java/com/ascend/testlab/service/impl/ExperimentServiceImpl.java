package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentService;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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

  /**
   * Constructor for ExperimentServiceImpl.
   *
   * @param experimentDAO the experiment DAO to use for data access
   */
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
   *   <li>EXPERIMENT_NOT_FOUND: If no experiment is found with the given project Key and experiment
   *       ID.
   *   <li>REST_GET_EXPERIMENT_BY_ID_FAILED: For any other errors encountered during retrieval.
   * </ul>
   */
  @Override
  public Single<Experiment> getExperiment(String projectKey, String experimentId) {
    return experimentDAO
        .getExperiment(projectKey, experimentId)
        .switchIfEmpty(Single.error(new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND)))
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error in get Experiments for project {} and experimentID {} : {}",
                  projectKey,
                  experimentId,
                  err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED, err)));
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
      String projectKey, FilterExperimentsRequest request) {
    return experimentDAO
        .filterExperiments(projectKey, request)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error in filter Experiments for project {}: {}", projectKey, err.getMessage());
              return Single.error(
                  ErrorEnum.handleException(
                      err, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, err)));
            });
  }
}
