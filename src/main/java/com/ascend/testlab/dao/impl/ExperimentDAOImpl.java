package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dao.queryBuilder.FilterExperimentsQueryFactory;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.google.inject.Inject;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return pgReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectId, experimentId),
        ExperimentMapper::mapRowToExperiment);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectId, FilterExperimentsRequest req) {
    String query = FilterExperimentsQueryFactory.buildQuery(req);

    return pgReaderClient
        .fetchAll(query, Tuple.of(projectId), row -> row)
        .map(rows -> mapRowsToFilteredExperiment(rows, req));
  }

  /**
   * Maps database rows to a FilterExperimentsResponse object.
   * Extracts experiment data from rows and builds pagination metadata.
   */
  private FilterExperimentsResponse mapRowsToFilteredExperiment(
      List<Row> rows, FilterExperimentsRequest req) {
    FilterExperimentsResponse response = new FilterExperimentsResponse();

    if (rows.isEmpty()) {
      response.setExperimentList(List.of());
      FilterExperimentsResponse.PaginationMeta paginationMeta =
          new FilterExperimentsResponse.PaginationMeta();
      paginationMeta.setTotalCount(0);
      paginationMeta.setPageSize(req.getLimit());
      paginationMeta.setCurrentPage(req.getPage());
      response.setPagination(paginationMeta);
      return response;
    }

    // Extract total count from the first row (all rows have the same total_count value)
    int totalCount = rows.get(0).getInteger("total_count");

    // Map all rows to experiments
    List<Experiment> experiments = rows.stream().map(ExperimentMapper::mapRowToExperiment).toList();

    response.setExperimentList(experiments);

    // Build pagination metadata
    FilterExperimentsResponse.PaginationMeta paginationMeta =
        new FilterExperimentsResponse.PaginationMeta();
    paginationMeta.setTotalCount(totalCount);
    paginationMeta.setPageSize(req.getLimit());
    paginationMeta.setCurrentPage(req.getPage());
    response.setPagination(paginationMeta);

    return response;
  }
}
