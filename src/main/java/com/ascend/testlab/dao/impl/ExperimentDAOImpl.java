package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.factory.FilterExperimentsQueryFactory;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the ExperimentDAO interface.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentDAO
 */
@Slf4j
public class ExperimentDAOImpl implements ExperimentDAO {

  private final PgReaderClient pgReaderClient;
  private final PgWriterClient pgWriterClient;

  /**
   * Constructs a new ExperimentDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   * @param pgWriterClient thw PostgreSQL writer client
   */
  @Inject
  public ExperimentDAOImpl(PgReaderClient pgReaderClient, PgWriterClient pgWriterClient) {
    this.pgReaderClient = pgReaderClient;
    this.pgWriterClient = pgWriterClient;
  }

  /** {@inheritDoc} */
  @Override
  public Maybe<Experiment> getExperiment(String projectKey, String experimentId) {
    return pgReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectKey, experimentId),
        ExperimentMapper::mapRowToExperiment);
  }

  /** {@inheritDoc} */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest req) {
    ParameterizedQuery parameterizedQuery =
        FilterExperimentsQueryFactory.buildQuery(projectKey, req);

    return pgReaderClient
        .fetchAll(parameterizedQuery.query(), parameterizedQuery.tuple(), row -> row)
        .map(rows -> mapRowsToFilteredExperiment(rows, req));
  }

  /**
   * Maps database rows to a FilterExperimentsResponse object. Extracts experiment data from rows
   * and builds pagination metadata.
   */
  private FilterExperimentsResponse mapRowsToFilteredExperiment(
      List<Row> rows, FilterExperimentsRequest req) {
    FilterExperimentsResponse response = new FilterExperimentsResponse();

    // Extract total count from the first row (all rows have the same total_count value)
    int totalCount = (rows.isEmpty()) ? 0 : rows.get(0).getInteger(Columns.TOTAL_COUNT);

    // Map all rows to experiments
    List<Experiment> experiments =
        (rows.isEmpty())
            ? List.of()
            : rows.stream().map(ExperimentMapper::mapRowToExperiment).toList();

    response.setExperiments(experiments);

    PaginationMeta paginationMeta;
    paginationMeta =
        PaginationMeta.builder()
            .pageSize(experiments.size())
            .currentPage(req.getPage())
            .totalCount(totalCount)
            .build();

    response.setPagination(paginationMeta);

    return response;
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> deleteExperiment(String projectKey, Experiment experiment) {
    Tuple tuple = Tuple.of(projectKey, experiment.getExperimentId().toString());

    JsonObject previous_data_json = JsonObject.mapFrom(experiment);

    return pgWriterClient
        .executeWithTransaction(
            (sqlConnection) ->
                pgWriterClient
                    .execute(sqlConnection, WriteQuery.DELETE_EXPERIMENT_BY_ID, tuple)
                    .flatMap(
                        delExp ->
                            pgWriterClient.execute(
                                sqlConnection, WriteQuery.DELETE_TAG_FOR_EXPERIMENT, tuple))
                    .flatMap(
                        delTag ->
                            pgWriterClient.execute(
                                sqlConnection, WriteQuery.DELETE_OWNER_FOR_EXPERIMENT, tuple))
                    .flatMap(
                        delOwner ->
                            pgWriterClient.execute(
                                sqlConnection,
                                WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG,
                                tuple.addJsonObject(previous_data_json).addJsonObject(null)))
                    .toMaybe())
        .switchIfEmpty(Single.error(new RestException(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED)));
  }
}
