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
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private final ObjectMapper objectMapper;

  /**
   * Constructs a new ExperimentDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   * @param pgWriterClient the PostgreSQL writer client
   * @param objectMapper the ObjectMapper for JSON serialization/deserialization
   */
  @Inject
  public ExperimentDAOImpl(
      PgReaderClient pgReaderClient, PgWriterClient pgWriterClient, ObjectMapper objectMapper) {
    this.pgReaderClient = pgReaderClient;
    this.pgWriterClient = pgWriterClient;
    this.objectMapper = objectMapper;
  }

  /** {@inheritDoc} */
  @Override
  public Maybe<Experiment> getExperiment(String projectKey, String experimentId) {
    return pgReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectKey, experimentId),
        row -> ExperimentMapper.mapRowToExperiment(row, objectMapper));
  }

  /** {@inheritDoc} */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest req) {
    ParameterizedQuery parameterizedQuery =
        FilterExperimentsQueryFactory.buildQuery(projectKey, req);

    return pgReaderClient
        .fetchAll(parameterizedQuery.query(), parameterizedQuery.tuple(), row -> row)
        .flatMap(
            rows -> {
              if (rows.isEmpty()) {
                return getTotalFilteredExperimentsCount(projectKey, req)
                    .map(count -> mapRowsToFilteredExperiment(rows, req, count));
              }
              return Single.just(
                  mapRowsToFilteredExperiment(
                      rows, req, rows.get(0).getInteger(Columns.TOTAL_COUNT)));
            });
  }

  /**
   * Maps database rows to a FilterExperimentsResponse object. Extracts experiment data from rows
   * and builds pagination metadata.
   */
  private FilterExperimentsResponse mapRowsToFilteredExperiment(
      List<Row> rows, FilterExperimentsRequest req, Integer totalCount) {
    FilterExperimentsResponse response = new FilterExperimentsResponse();

    // Map all rows to experiments
    List<Experiment> experiments =
        (rows.isEmpty())
            ? List.of()
            : rows.stream()
                .map(row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
                .toList();

    response.setExperiments(experiments);

    PaginationMeta paginationMeta = setPaginationInResponse(req, totalCount, rows.size());

    response.setPagination(paginationMeta);

    return response;
  }

  private Single<Integer> getTotalFilteredExperimentsCount(
      String projectKey, FilterExperimentsRequest request) {
    ParameterizedQuery parameterizedQuery =
        FilterExperimentsQueryFactory.buildCountQuery(projectKey, request);

    return pgReaderClient
        .fetchOne(
            parameterizedQuery.query(),
            parameterizedQuery.tuple(),
            row -> row.getInteger(Columns.TOTAL_COUNT))
        .toSingle()
        .onErrorReturnItem(0);
  }

  private PaginationMeta setPaginationInResponse(
      FilterExperimentsRequest req, Integer totalCount, Integer rowCount) {
    int currentPage = req.getPage();
    int pageSize = rowCount;
    int offset = (currentPage - 1) * req.getLimit();
    boolean hasNextPage = pageSize == req.getLimit() && (offset + pageSize) <= totalCount;

    return PaginationMeta.builder()
        .pageSize(pageSize)
        .currentPage(req.getPage())
        .totalCount(totalCount)
        .hasNext(hasNextPage)
        .build();
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
