package com.ascend.testlab.dao.impl;

import static org.apache.commons.lang3.StringUtils.join;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.mapper.ExperimentMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentDAOImpl implements ExperimentDAO {

  private final MySQLReaderClient mySQLReaderClient;

  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return mySQLReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectId, experimentId),
        ExperimentMapper::mapRowToExperiment);
  }

  @Override
  public Single<List<Experiment>> fetchExperiments(String projectId, FilterExperimentsRequest req) {

    // Build dynamic query for status, type, and name filters
    StringBuilder query = new StringBuilder(ReadQuery.FILTER_EXPERIMENT);
    List<Object> parameters = new java.util.ArrayList<>();
    parameters.add(projectId);

    if (req.hasNameFilter()) {
      query.append(ReadQuery.NAME_FILTER);
      parameters.add("%" + req.getName() + "%");
    }

    if (req.hasStatusFilter()) {
      String statusStr = req.getStatus().stream()
              .map(status -> "'" + status.name() + "'")
              .collect(Collectors.joining(","));
      query.append(ReadQuery.STATUS_FILTER.replace("<<STATUS>>", statusStr));
    }

    if (req.hasTypeFilter()) {
      String typeStr = req.getType().stream()
              .map(type -> "'" + type.name() + "'")
              .collect(Collectors.joining(","));
      query.append(ReadQuery.TYPE_FILTER.replace("<<TYPE>>", typeStr));
    }

    query.append(ReadQuery.GROUP_BY);
    query.append(ReadQuery.ORDER_BY_CREATED_AT);

    log.debug("Executing query: {} with parameters: {}", query, parameters);
    return mySQLReaderClient.fetchAll(
        query.toString(), Tuple.tuple(parameters), ExperimentMapper::mapRowToExperiment);
  }

  @Override
  public Single<Set<String>> getExperimentIdsByTags(String projectId, List<String> tags) {
    String query = ReadQuery.GET_EXPERIMENT_BY_TAGS_FILTER.replace("<<TAG>>", join(tags, ','));

    return mySQLReaderClient
        .fetchAll(query, Tuple.of(projectId), row -> row.getString("experiment_id"))
        .map(HashSet::new)
        .map(set -> (Set<String>) set)
        .doOnError(error -> log.error("Error fetching tag IDs for projectId: {}", projectId, error))
        .onErrorReturnItem(new HashSet<>());
  }

  @Override
  public Single<Set<String>> getExperimentIdsByOwners(String projectId, List<String> owners) {
    String query = ReadQuery.GET_EXPERIMENT_BY_OWNER_FILTER.replace("<<OWNER>>", join(owners, ','));

    return mySQLReaderClient
        .fetchAll(query, Tuple.of(projectId), row -> row.getString("experiment_id"))
        .map(HashSet::new)
        .map(set -> (Set<String>) set)
        .doOnError(
            error -> log.error("Error fetching owner IDs for projectId: {}", projectId, error))
        .onErrorReturnItem(new HashSet<>());
  }

  @Override
  public Single<List<Experiment>> fetchExperimentsByIds(
      String projectId, Set<String> experimentIds, FilterExperimentsRequest req) {

    // MySQL has a limit on IN clause size (typically 1000), handle large sets by chunking
    if (experimentIds.size() > 1000) {
      log.warn(
          "Large experiment ID set (size: {}) for projectId: {}, using first 1000 IDs",
          experimentIds.size(),
          projectId);
      experimentIds = experimentIds.stream().limit(1000).collect(Collectors.toSet());
    }

    // Build dynamic query for experiments filtered by IDs, status, type, and name
    StringBuilder query = new StringBuilder(ReadQuery.FILTER_EXPERIMENT);
    List<Object> parameters = new java.util.ArrayList<>();
    parameters.add(projectId);

    // Add experiment ID filter
    String idPlaceholders = experimentIds.stream().map(id -> "?").collect(Collectors.joining(","));
    query.append(" AND e.experiment_id IN (").append(idPlaceholders).append(")");
    parameters.addAll(experimentIds);

    // Add name filter
    if (req.hasNameFilter()) {
      query.append(ReadQuery.NAME_FILTER);
      parameters.add("%" + req.getName() + "%");
    }

    // Add status filter
    if (req.hasStatusFilter()) {
      String statusStr = req.getStatus().stream()
              .map(status -> "'" + status.name() + "'")
              .collect(Collectors.joining(","));
      query.append(ReadQuery.STATUS_FILTER.replace("<<STATUS>>", statusStr));
    }

    // Add type filter
    if (req.hasTypeFilter()) {
        String typeStr = req.getType().stream()
                .map(type -> "'" + type.name() + "'")
                .collect(Collectors.joining(","));
      query.append(ReadQuery.TYPE_FILTER.replace("<<TYPE>>", typeStr));
    }

    query.append(ReadQuery.GROUP_BY);
    query.append(ReadQuery.ORDER_BY_CREATED_AT);

    return mySQLReaderClient
        .fetchAll(query.toString(), Tuple.tuple(parameters), ExperimentMapper::mapRowToExperiment)
        .doOnSuccess(
            experiments ->
                log.debug(
                    "Retrieved {} experiments for projectId: {}", experiments.size(), projectId))
        .doOnError(
            error ->
                log.error(
                    "Error fetching filtered experiments for projectId: {}", projectId, error));
  }
}
