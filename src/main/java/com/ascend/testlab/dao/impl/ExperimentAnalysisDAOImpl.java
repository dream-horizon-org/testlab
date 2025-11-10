package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentAnalysisDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentAnalysisDAO for PostgreSQL database operations.
 *
 * <p>This class handles all experiment analysis database operations with proper type conversions
 * for PostgreSQL-specific types.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentAnalysisDAOImpl implements ExperimentAnalysisDAO {

  private final PgWriterClient pgWriterClient;

  /**
   * Constructs ExperimentAnalysisDAOImpl with PostgreSQL writer client.
   *
   * @param pgWriterClient PostgreSQL writer client for database operations
   */
  @Inject
  public ExperimentAnalysisDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
  }

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertAnalysis(
      SqlConnection connection, UUID projectKey, UUID experimentId) {
    return insertAnalysis(connection, projectKey, experimentId, null, null, null, null);
  }

  /**
   * Inserts an experiment analysis entry with specified values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param config analysis configuration
   * @param primaryMetrics primary metrics for analysis
   * @param secondaryMetrics secondary metrics for analysis
   * @param metricTokens metric tokens
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertAnalysis(
      SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      String config,
      String primaryMetrics,
      String secondaryMetrics,
      String metricTokens) {
    log.debug(
        "DAO: Inserting analysis for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple()
            .addString(projectKey.toString())
            .addString(experimentId.toString())
            .addString(config)
            .addString(primaryMetrics)
            .addString(secondaryMetrics)
            .addString(metricTokens);

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_ANALYSIS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted analysis for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to insert analysis for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for analysis insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }
}
