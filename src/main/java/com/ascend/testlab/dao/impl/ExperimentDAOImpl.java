package com.ascend.testlab.dao.impl;


import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.mapper.ExperimentMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentDAOImpl implements ExperimentDAO {

  private final PgReaderClient pgReaderClient;

  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return pgReaderClient.fetchOne(
        ReadQuery.GET_EXPERIMENT,
        Tuple.of(projectId, experimentId),
        ExperimentMapper::mapRowToExperiment);
  }

  @Override
  public Single<List<Experiment>> fetchExperiments(String projectId, FilterExperimentsRequest req) {

    // Build dynamic query for status, type, and name filters
    StringBuilder query = new StringBuilder(ReadQuery.FILTER_EXPERIMENT);
    List<Object> parameters = new ArrayList<>();
    parameters.add(projectId);
    int paramIndex = 2; // $1 is projectId

    if (req.hasNameFilter()) {
      query.append(String.format(ReadQuery.NAME_FILTER, "$" + paramIndex));
      parameters.add(req.getName());
    }

    if (req.hasStatusFilter()) {
      String statusStr =
          req.getStatus().stream()
              .map(status -> "'" + status.name() + "'")
              .collect(Collectors.joining(","));
      query.append(ReadQuery.STATUS_FILTER.replace("<<STATUS>>", statusStr));
    }

    if (req.hasTypeFilter()) {
      String typeStr =
          req.getType().stream().map(type -> "'" + type + "'").collect(Collectors.joining(","));
      query.append(ReadQuery.TYPE_FILTER.replace("<<TYPE>>", typeStr));
    }

    query.append(ReadQuery.GROUP_BY);
    query.append(ReadQuery.ORDER_BY_CREATED_AT);

    log.debug("Executing query: {} with parameters: {}", query, parameters);
    return pgReaderClient.fetchAll(
        query.toString(), Tuple.tuple(parameters), ExperimentMapper::mapRowToExperiment);
  }

  @Override
  public Single<Set<UUID>> getExperimentIdsByTags(String projectId, List<String> tags) {
    String tagStr = tags.stream().map(tag -> "'" + tag + "'").collect(Collectors.joining(","));
    String query = ReadQuery.GET_EXPERIMENT_BY_TAGS_FILTER.replace("<<TAG>>", tagStr);

    return pgReaderClient
        .fetchAll(query, Tuple.of(projectId), row -> row.getUUID("experiment_id"))
        .map(HashSet::new)
        .map(set -> (Set<UUID>) set)
        .doOnError(error -> log.error("Error fetching tag IDs for projectId: {}", projectId, error))
        .onErrorReturnItem(new HashSet<>());
  }

  @Override
  public Single<Set<UUID>> getExperimentIdsByOwners(String projectId, List<String> owners) {
    String ownerStr =
        owners.stream().map(owner -> "'" + owner + "'").collect(Collectors.joining(","));
    String query = ReadQuery.GET_EXPERIMENT_BY_OWNER_FILTER.replace("<<OWNER>>", ownerStr);

    return pgReaderClient
        .fetchAll(query, Tuple.of(projectId), row -> row.getUUID("experiment_id"))
        .map(HashSet::new)
        .map(set -> (Set<UUID>) set)
        .doOnError(
            error -> log.error("Error fetching owner IDs for projectId: {}", projectId, error))
        .onErrorReturnItem(new HashSet<>());
  }

  @Override
  public Single<List<Experiment>> fetchExperimentsByIds(
      String projectId, Set<String> experimentIds, FilterExperimentsRequest req) {

    // Build dynamic query for experiments filtered by IDs, status, type, and name
    StringBuilder query = new StringBuilder(ReadQuery.FILTER_EXPERIMENT);
    List<Object> parameters = new java.util.ArrayList<>();
    parameters.add(projectId);
    int paramIndex = 2; // $1 is projectId

    // Add experiment ID filter
    StringBuilder idPlaceholderBuilder = new StringBuilder();
    boolean first = true;
    for (String id : experimentIds) {
      if (!first) {
        idPlaceholderBuilder.append(",");
      }
      String placeholder = "$" + paramIndex;
      idPlaceholderBuilder.append(placeholder);
      parameters.add(id);
      paramIndex++;
      first = false;
    }
    query.append(" AND e.experiment_id IN (").append(idPlaceholderBuilder).append(")");

    // Add name filter
    if (req.hasNameFilter()) {
      query.append(String.format(ReadQuery.NAME_FILTER, "$" + paramIndex));
      parameters.add("%" + req.getName() + "%");
      paramIndex++;
    }

    // Add status filter
    if (req.hasStatusFilter()) {
      String statusStr =
          req.getStatus().stream()
              .map(status -> "'" + status.name() + "'")
              .collect(Collectors.joining(","));
      query.append(ReadQuery.STATUS_FILTER.replace("<<STATUS>>", statusStr));
    }

    // Add type filter
    if (req.hasTypeFilter()) {
      String typeStr =
          req.getType().stream().map(type -> "'" + type + "'").collect(Collectors.joining(","));
      query.append(ReadQuery.TYPE_FILTER.replace("<<TYPE>>", typeStr));
    }

    query.append(ReadQuery.GROUP_BY);
    query.append(ReadQuery.ORDER_BY_CREATED_AT);

    return pgReaderClient
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
