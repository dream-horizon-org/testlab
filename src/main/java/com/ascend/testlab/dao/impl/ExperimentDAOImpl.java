package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.factory.FilterExperimentsQueryFactory;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
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

  /**
   * Constructs a new ExperimentDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public ExperimentDAOImpl(PgReaderClient pgReaderClient) {
    this.pgReaderClient = pgReaderClient;
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
        FilterExperimentsQueryFactory.buildQuery(projectKey, req, true);

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
            : rows.stream().map(ExperimentMapper::mapRowToExperiment).toList();

    response.setExperiments(experiments);

    PaginationMeta paginationMeta = setPaginationInResponse(req, totalCount, rows.size());

    response.setPagination(paginationMeta);

    return response;
  }

  private Single<Integer> getTotalFilteredExperimentsCount(
      String projectKey, FilterExperimentsRequest request) {
    ParameterizedQuery parameterizedQuery =
        FilterExperimentsQueryFactory.buildQuery(projectKey, request, false);

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
        .hasNextPage(hasNextPage)
        .build();
  }
}
