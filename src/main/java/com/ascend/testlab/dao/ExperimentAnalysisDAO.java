package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.UUID;

/**
 * Data Access Object interface for ExperimentAnalysis operations.
 *
 * <p>Provides methods for creating and managing experiment analysis entries in the database.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentAnalysisDAO {

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertAnalysis(SqlConnection connection, UUID projectKey, UUID experimentId);

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
  Single<Boolean> insertAnalysis(
      SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      String config,
      String primaryMetrics,
      String secondaryMetrics,
      String metricTokens);
}
