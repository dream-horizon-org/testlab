package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dao.querybuilder.ExperimentUpdateQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.factory.FilterExperimentsQueryFactory;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Metrics;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.DbExceptionUtil;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

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
   * @param pgReaderClient the Postgres reader client
   * @param pgWriterClient the Postgres writer client
   * @param objectMapper the Jackson ObjectMapper for JSON serialization/deserialization
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
        ReadQuery.FETCH_EXPERIMENT,
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
        .map(rows -> mapRowsToFilteredExperiment(rows, req));
  }

  /**
   * Maps database rows to a FilterExperimentsResponse object. Extracts experiment data from rows
   * and builds pagination metadata.
   */
  private FilterExperimentsResponse mapRowsToFilteredExperiment(
      List<Row> rows, FilterExperimentsRequest req) {
    FilterExperimentsResponse response = new FilterExperimentsResponse();

    int totalCount = (rows.isEmpty()) ? 0 : rows.get(0).getInteger(Columns.TOTAL_COUNT);

    List<Experiment> experiments =
        (rows.isEmpty())
            ? List.of()
            : rows.stream()
                .map(row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
                .toList();

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

    // make this parallel
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
                    .map(result -> true)
                    .toMaybe(),
            false)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to delete experiment for projectKey: {}, experimentId: {}, error: {}",
                  projectKey,
                  experiment.getExperimentId(),
                  err.getMessage());
              return Single.error(new RestException(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED, err));
            });
  }

  /**
   * Creates a new experiment using an existing SQL connection (for transaction support).
   *
   * @param connection the SQL connection to use
   * @param projectKey project identifier for partitioning
   * @param experiment experiment creation experiment
   * @return Single emitting experiment ID as String on success
   */
  private Single<Boolean> insertExperiment(
      SqlConnection connection, String projectKey, Experiment experiment) {
    log.info(
        "DAO: Creating experiment with connection, projectKey: {}, experimentId: {}, name: {}",
        projectKey,
        experiment.getExperimentId(),
        experiment.getName());

    Tuple tuple =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experiment.getExperimentId().toString())
            .addString(experiment.getName())
            .addString(experiment.getExperimentKey())
            .addString(experiment.getDescription())
            .addString(experiment.getHypothesis())
            .addString(experiment.getStatus().name())
            .addString(experiment.getType().getValue())
            .addValue(
                experiment.getCohorts() == null || experiment.getCohorts().isEmpty()
                    ? new String[0]
                    : experiment.getCohorts().toArray(new String[0]))
            .addJsonObject(JsonObject.mapFrom(experiment.getVariantWeights()))
            .addJsonObject(JsonObject.mapFrom(experiment.getVariants()))
            .addString(experiment.getDistributionStrategy().name())
            .addString(experiment.getAssignmentDomain().name())
            .addValue(
                experiment.getOverrides() == null || experiment.getOverrides().isEmpty()
                    ? new String[0]
                    : experiment.getOverrides().toArray(new String[0]))
            .addJsonArray(new JsonArray(experiment.getRuleAttributes()))
            .addValue(experiment.getExposure())
            .addValue(experiment.getThreshold())
            .addValue(experiment.getStartTime())
            .addValue(experiment.getEndTime())
            .addValue(experiment.getCreatedBy());

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT, tuple)
        .onErrorResumeNext(
            err -> {
              log.error("Error while inserting experiment", err);
              return Single.error(
                  DbExceptionUtil.handleDbError(err, ErrorEnum.EXPERIMENT_CREATION_FAILED));
            });
  }

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * <p>Extracts projectKey, experimentId, tags, and owner from the experiment object and executes
   * all insert operations in parallel within a transaction.
   *
   * @param experiment experiment creation experiment with all experiment details including
   *     projectKey, experimentId, tags, and owner
   * @return Single emitting experiment ID as String on success
   */
  @Override
  public Single<Boolean> createExperiment(String projectKey, Experiment experiment) {

    log.info(
        "DAO: Creating experiment, projectKey: {}, experimentId: {}",
        projectKey,
        experiment.getExperimentId());

    return pgWriterClient.executeWithTransaction(
        connection -> createExperimentWithConnection(connection, projectKey, experiment), false);
  }

  private Maybe<Boolean> createExperimentWithConnection(
      SqlConnection connection, String projectKey, Experiment experiment) {

    return insertExperiment(connection, projectKey, experiment)
        .flatMap(
            success -> {
              log.info(
                  "DAO: Experiment created successfully, inserting related data for experimentId: {}",
                  experiment.getExperimentId());

              return Single.zip(
                      insertTags(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getTags()),
                      insertOwners(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getOwners()),
                      insertUpdateLog(connection, projectKey, null, experiment),
                      insertAnalysis(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getMetrics()),
                      (tagsSuccess, ownerSuccess, updateLogSuccess, analysisSuccess) ->
                          tagsSuccess && ownerSuccess && updateLogSuccess && analysisSuccess)
                  .map(res -> res);
            })
        .toMaybe();
  }

  /**
   * Batch inserts tags for an experiment using a single query.
   *
   * <p>This method uses batch insert for optimal performance, inserting all tags in a single
   * database round trip.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertTags(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags) {
    if (Objects.isNull(tags) || tags.isEmpty()) {
      log.debug("No tags to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Batch inserting {} tags for experimentId: {}, projectKey: {}",
        tags.size(),
        experimentId,
        projectKey);

    List<Tuple> tuples = new ArrayList<>();
    for (String tag : tags) {
      Tuple params =
          Tuple.tuple().addString(experimentId.toString()).addString(projectKey).addString(tag);
      tuples.add(params);
    }

    return pgWriterClient
        .executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_TAG, tuples)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted {} tags for experimentId: {}, projectKey: {}",
                    tags.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for tag batch insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertOwners(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> owners) {
    if (Objects.isNull(owners) || owners.isEmpty()) {
      log.debug("No owners to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Batch inserting {} owners for experimentId: {}, projectKey: {}",
        owners.size(),
        experimentId,
        projectKey);

    List<Tuple> tuples =
        owners.stream()
            .map(
                owner ->
                    Tuple.tuple()
                        .addString(experimentId.toString())
                        .addString(projectKey)
                        .addString(owner))
            .toList();

    return pgWriterClient
        .executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_OWNER, tuples)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted {} owners for experimentId: {}, projectKey: {}",
                    owners.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for owners batch insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts an experiment update log entry.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param previousData previous experiment data (null for create operation)
   * @param currentData current experiment data
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertUpdateLog(
      SqlConnection connection,
      String projectKey,
      Experiment previousData,
      Experiment currentData) {
    log.debug(
        "DAO: Inserting update log for experimentId: {}, projectKey: {}",
        currentData.getExperimentId(),
        projectKey);

    Tuple tuple =
        Tuple.tuple()
            .addString(projectKey)
            .addString(currentData.getExperimentId().toString())
            .addJsonObject(JsonObject.mapFrom(previousData))
            .addJsonObject(JsonObject.mapFrom(currentData))
            .addString(currentData.getUpdatedBy());

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG, tuple)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted update log for experimentId: {}, projectKey: {}",
                    currentData.getExperimentId(),
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for update log insert, experimentId: {}, error: {}",
                  currentData.getExperimentId(),
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param metrics map of metrics (primary and secondary)
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertAnalysis(
      SqlConnection connection, String projectKey, UUID experimentId, Metrics metrics) {

    if (Objects.isNull(metrics)) {
      return Single.just(true);
    }

    log.debug(
        "DAO: Inserting analysis for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experimentId.toString())
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getPrimary()).toArray(new String[0]))
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getSecondary()).toArray(new String[0]));
    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_ANALYSIS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted analysis for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for analysis insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> updateExperiment(
      String projectKey, Experiment previousExperiment, Experiment updatedExperiment) {

    log.info(
        "DAO: Updating experiment, projectKey: {}, experimentId: {}",
        projectKey,
        updatedExperiment.getExperimentId());

    return pgWriterClient.executeWithTransaction(
        connection ->
            updateExperimentInTransaction(
                connection, projectKey, previousExperiment, updatedExperiment),
        false);
  }

  private Maybe<Boolean> updateExperimentInTransaction(
      SqlConnection connection,
      String projectKey,
      Experiment previousExperiment,
      Experiment updatedExperiment) {

    return executeExperimentUpdate(connection, projectKey, previousExperiment, updatedExperiment)
        .flatMap(
            success -> {
              log.info(
                  "DAO: Experiment updated successfully, updating related data for experimentId: {}",
                  updatedExperiment.getExperimentId());

              return Single.zip(
                  syncTags(
                      connection,
                      projectKey,
                      updatedExperiment.getExperimentId(),
                      previousExperiment.getTags(),
                      updatedExperiment.getTags()),
                  syncOwners(
                      connection,
                      projectKey,
                      updatedExperiment.getExperimentId(),
                      previousExperiment.getOwners(),
                      updatedExperiment.getOwners()),
                  updateAnalysis(
                      connection,
                      projectKey,
                      updatedExperiment.getExperimentId(),
                      updatedExperiment.getMetrics()),
                  insertUpdateLog(connection, projectKey, previousExperiment, updatedExperiment),
                  (tagsSuccess, ownersSuccess, analysisSuccess, logSuccess) ->
                      tagsSuccess && ownersSuccess && analysisSuccess && logSuccess);
            })
        .toMaybe();
  }

  private Single<Boolean> executeExperimentUpdate(
      SqlConnection connection,
      String projectKey,
      Experiment previousExperiment,
      Experiment updatedExperiment) {

    ExperimentUpdateQueryBuilder queryBuilder =
        ExperimentUpdateQueryBuilder.build(previousExperiment, updatedExperiment, projectKey);

    if (!queryBuilder.hasChanges()) {
      log.debug(
          "DAO: No changes detected for experimentId: {}", updatedExperiment.getExperimentId());
      return Single.just(true);
    }

    String query = queryBuilder.buildQuery();
    log.debug("DAO: Executing dynamic update query: {}", query);

    return pgWriterClient.execute(connection, query, queryBuilder.getTuple());
  }

  private Single<Boolean> syncTags(
      SqlConnection connection,
      String projectKey,
      UUID experimentId,
      List<String> existingTags,
      List<String> newTags) {

    Set<String> existing = new HashSet<>(ListUtils.emptyIfNull(existingTags));
    Set<String> updated = new HashSet<>(ListUtils.emptyIfNull(newTags));

    List<String> toDelete = existing.stream().filter(t -> !updated.contains(t)).toList();
    List<String> toAdd = updated.stream().filter(t -> !existing.contains(t)).toList();

    if (toDelete.isEmpty() && toAdd.isEmpty()) {
      return Single.just(true);
    }

    Single<Boolean> deleteResult =
        toDelete.isEmpty()
            ? Single.just(true)
            : deleteTags(connection, projectKey, experimentId, toDelete);

    Single<Boolean> addResult =
        toAdd.isEmpty()
            ? Single.just(true)
            : insertTags(connection, projectKey, experimentId, toAdd);

    return deleteResult.flatMap(deleted -> addResult);
  }

  private Single<Boolean> syncOwners(
      SqlConnection connection,
      String projectKey,
      UUID experimentId,
      List<String> existingOwners,
      List<String> newOwners) {

    Set<String> existing = new HashSet<>(ListUtils.emptyIfNull(existingOwners));
    Set<String> updated = new HashSet<>(ListUtils.emptyIfNull(newOwners));

    List<String> toDelete = existing.stream().filter(o -> !updated.contains(o)).toList();
    List<String> toAdd = updated.stream().filter(o -> !existing.contains(o)).toList();

    if (toDelete.isEmpty() && toAdd.isEmpty()) {
      return Single.just(true);
    }

    Single<Boolean> deleteResult =
        toDelete.isEmpty()
            ? Single.just(true)
            : deleteOwners(connection, projectKey, experimentId, toDelete);

    Single<Boolean> addResult =
        toAdd.isEmpty()
            ? Single.just(true)
            : insertOwners(connection, projectKey, experimentId, toAdd);

    return deleteResult.flatMap(deleted -> addResult);
  }

  private Single<Boolean> deleteTags(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags) {

    log.debug("DAO: Deleting {} tags for experimentId: {}", tags.size(), experimentId);

    Tuple tuple = Tuple.of(projectKey, experimentId.toString(), tags.toArray(new String[0]));
    return pgWriterClient.execute(connection, WriteQuery.DELETE_TAGS, tuple);
  }

  private Single<Boolean> deleteOwners(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> owners) {

    log.debug("DAO: Deleting {} owners for experimentId: {}", owners.size(), experimentId);

    Tuple tuple = Tuple.of(projectKey, experimentId.toString(), owners.toArray(new String[0]));
    return pgWriterClient.execute(connection, WriteQuery.DELETE_OWNERS, tuple);
  }

  private Single<Boolean> updateAnalysis(
      SqlConnection connection, String projectKey, UUID experimentId, Metrics metrics) {

    if (Objects.isNull(metrics)) {
      return Single.just(true);
    }

    log.debug(
        "DAO: Updating analysis for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experimentId.toString())
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getPrimary()).toArray(new String[0]))
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getSecondary()).toArray(new String[0]));

    return pgWriterClient
        .execute(connection, WriteQuery.UPDATE_EXPERIMENT_ANALYSIS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully updated analysis for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for analysis update, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }
}
