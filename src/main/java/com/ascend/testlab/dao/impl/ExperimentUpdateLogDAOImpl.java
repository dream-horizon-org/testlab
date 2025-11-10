package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentUpdateLogDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentUpdateLogDAO for PostgreSQL database operations.
 *
 * <p>This class handles all experiment update log database operations with proper type conversions
 * for PostgreSQL-specific types.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentUpdateLogDAOImpl implements ExperimentUpdateLogDAO {

  private final PgWriterClient pgWriterClient;
  private final ObjectMapper objectMapper;

  /**
   * Constructs ExperimentUpdateLogDAOImpl with PostgreSQL writer client.
   *
   * @param pgWriterClient PostgreSQL writer client for database operations
   */
  @Inject
  public ExperimentUpdateLogDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Inserts an experiment update log entry.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param previousData previous experiment data (null for create operation)
   * @param currentData current experiment data
   * @param updatedBy user who performed the update
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertUpdateLog(
      SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      Map<String, Object> currentData,
      String updatedBy) {
    log.debug(
        "DAO: Inserting update log for experimentId: {}, projectKey: {}, updatedBy: {}",
        experimentId,
        projectKey,
        updatedBy);

    try {
      // Convert maps to JsonObject for JSONB columns
      JsonObject previousDataJson =
          previousData != null
              ? new JsonObject(objectMapper.writeValueAsString(previousData))
              : null;
      JsonObject currentDataJson =
          currentData != null ? new JsonObject(objectMapper.writeValueAsString(currentData)) : null;

      Tuple params =
          Tuple.tuple()
              .addString(projectKey.toString())
              .addString(experimentId.toString())
              .addValue(previousDataJson)
              .addValue(currentDataJson)
              .addString(updatedBy);

      return pgWriterClient
          .execute(connection, WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG, params)
          .doOnSuccess(
              success ->
                  log.info(
                      "DAO: Successfully inserted update log for experimentId: {}, projectKey: {}",
                      experimentId,
                      projectKey))
          .doOnError(
              error ->
                  log.error(
                      "DAO: Failed to insert update log for experimentId: {}, error: {}",
                      experimentId,
                      error.getMessage(),
                      error))
          .onErrorReturn(
              error -> {
                log.error(
                    "DAO: Returning false for update log insert, experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage());
                return false;
              });
    } catch (Exception e) {
      log.error(
          "DAO: Exception while preparing update log data for experimentId: {}, error: {}",
          experimentId,
          e.getMessage(),
          e);
      return Single.just(false);
    }
  }
}
