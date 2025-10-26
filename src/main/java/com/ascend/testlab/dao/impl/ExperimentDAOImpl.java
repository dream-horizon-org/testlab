package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.mapper.ExperimentMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.join;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentDAOImpl implements ExperimentDAO {

  private final MySQLReaderClient mySQLReaderClient;

  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return mySQLReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectId, Long.parseLong(experimentId)),
        ExperimentMapper::mapRowToExperiment);
  }

  @Override
  public Single<List<Experiment>> filterExperiments(String projectId, FilterExperimentsRequest req) {

    // Build dynamic query based on filters
    StringBuilder query = new StringBuilder(ReadQuery.FILTER_EXPERIMENT);
    List<Object> parameters = new java.util.ArrayList<>();

    parameters.add(projectId);

    if (req.getName() != null && !req.getName().trim().isEmpty()) {
      query.append(ReadQuery.NAME_FILTER);
      parameters.add("%" + req.getName() + "%");
    }

    // Handle status filtering
    if (req.getStatus() != null && !req.getStatus().isEmpty()) {
      appendStatusQuery(query, parameters, req);
    }

    if (req.getType() != null && !req.getType().isEmpty()) {
      appendTypeQuery(query, parameters, req);
    }

    // Handle tag filtering with separate tags table
    if (req.getTag() != null && !req.getTag().isEmpty()) {
      appendTagQuery(query, parameters, req, projectId);
    }

    // Handle owner filtering with separate owners table
    if (req.getOwner() != null && !req.getOwner().isEmpty()) {
      appendOwnerQuery(query, parameters, req, projectId);
    }

    query.append(ReadQuery.ORDER_BY_CREATED_AT);

    // Handle pagination with validation
    if (req.getLimit() != null && req.getLimit() > 0) {
      query.append(" LIMIT ? ");
      parameters.add(req.getLimit());

      if (req.getPage() != null && req.getPage() > 0) {
        query.append(" OFFSET ? ");
        parameters.add((req.getPage() - 1) * req.getLimit());
      }
    }

    log.debug("Executing query: {} with parameters: {}", query, parameters);
    return mySQLReaderClient.fetchAll(
        query.toString(), Tuple.tuple(parameters), ExperimentMapper::mapRowToExperiment);
  }

  private void appendStatusQuery(
      StringBuilder query, List<Object> parameters, FilterExperimentsRequest req) {
        query.append(ReadQuery.STATUS_FILTER.replace("<<STATUS>>", join(req.getStatus(), ',')));
  }

  private void appendTypeQuery(
      StringBuilder query, List<Object> parameters, FilterExperimentsRequest req) {
        query.append(ReadQuery.TYPE_FILTER.replace("<<TYPE>>", join(req.getType(), ',')));
  }

  private void appendTagQuery(
      StringBuilder query, List<Object> parameters, FilterExperimentsRequest req, String projectId) {

  }

  private void appendOwnerQuery(
      StringBuilder query, List<Object> parameters, FilterExperimentsRequest req, String projectId) {

  }
}
