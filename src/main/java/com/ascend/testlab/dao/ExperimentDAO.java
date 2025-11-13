package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object interface for Experiment operations.
 *
 * <p>Provides methods for creating and updating experiments in the database, including related
 * entities like tags, owners, update logs, and analysis.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentDAO {

  /**
   * Creates a new experiment in the database.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param request experiment creation request with all experiment details
   * @return Single emitting 1L on success, 0L on failure
   */
  Single<Long> create(UUID tenantId, UUID projectKey, CreateExperimentRequest request);

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request experiment creation request with all experiment details
   * @param tags list of tags to associate with experiment
   * @param owner owner of the experiment
   * @return Maybe emitting experiment ID on success
   */
  Maybe<Long> createWithRelatedData(
      UUID tenantId,
      UUID projectKey,
      UUID experimentId,
      CreateExperimentRequest request,
      List<String> tags,
      String owner);

  /**
   * Gets current experiment data as a map.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting map of experiment data
   */
  Single<Map<String, Object>> getExperimentData(UUID projectKey, UUID experimentId);

  /**
   * Updates experiment fields partially based on provided request map.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> updatePartial(UUID projectKey, UUID experimentId, Map<String, Object> request);

  /**
   * Updates experiment with tags and logs in a transaction.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param experimentFields map of experiment fields to update
   * @param tags list of tags to update (null if no tag update)
   * @param previousData experiment data before update
   * @param updatedBy user who updated the experiment
   * @return Maybe emitting true on success
   */
  Maybe<Boolean> updateWithTransaction(
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> experimentFields,
      List<String> tags,
      Map<String, Object> previousData,
      String updatedBy);

  // ==================== Tag Operations ====================

  /**
   * Batch inserts tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> batchInsertTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags);

  /**
   * Gets active tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting list of active tag names
   */
  Single<List<String>> getActiveTags(SqlConnection connection, UUID projectKey, UUID experimentId);

  /**
   * Marks tags as inactive (status = 0).
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to mark as inactive
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> markTagsInactive(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags);

  /**
   * Deletes all tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteTags(SqlConnection connection, UUID projectKey, UUID experimentId);

  // ==================== Owner Operations ====================

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param owner owner name/email
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertOwner(
      SqlConnection connection, UUID projectKey, UUID experimentId, String owner);

  /**
   * Deletes all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteOwners(SqlConnection connection, UUID projectKey, UUID experimentId);

  // ==================== Update Log Operations ====================

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
  Single<Boolean> insertUpdateLog(
      SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      Map<String, Object> currentData,
      String updatedBy);

  // ==================== Analysis Operations ====================

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param metrics list of metrics (primary and secondary)
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertAnalysis(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> metrics);

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
