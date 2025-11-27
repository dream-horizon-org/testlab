package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Interface for experiment-related database operations. */
public interface ExperimentDAO {
  /**
   * Retrieves a single experiment by project Key and experiment ID.
   *
   * @param projectKey the project key to fetch experiment
   * @param experimentId the unique identifier of the experiment
   * @return a Maybe that emits the Experiment if found, or else empty
   */
  Maybe<Experiment> getExperiment(String projectKey, String experimentId);

  /**
   * Filters experiments based on the provided criteria and returns paginated results. Supports
   * filtering by various attributes such as name, tags, type, status, and owner.
   *
   * @param projectKey the project Key to filter experiments within
   * @param req the filter request containing filter criteria, pagination, and sorting options
   * @return a Single that emits a FilterExperimentsResponse containing the filtered experiments and
   *     pagination metadata
   */
  Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest req);

  /**
   * Delete experiment and related data for the provided projectKey and experimentId
   *
   * @param projectKey the project Key
   * @param experiment the experiment data
   * @return a Single
   */
  Single<Boolean> deleteExperiment(String projectKey, Experiment experiment);

  /**
   * Creates a new experiment in the database without transaction management.
   *
   * <p><b>Deprecated:</b> This method is only for testing purposes. Use createWithRelatedData for
   * production code.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param request experiment creation request with all experiment details
   * @return Single emitting experiment ID as String on success
   * @deprecated Use {@link #createWithRelatedData(UUID, CreateExperimentRequest)} instead
   */
  @Deprecated
  Single<String> create(UUID tenantId, String projectKey, CreateExperimentRequest request);

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * <p>All necessary data (projectKey, experimentId, tags, owner) is extracted from the request
   * object.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param request experiment creation request with all experiment details including projectKey,
   *     experimentId, tags, and owner
   * @return Single emitting experiment ID as String on success
   */
  Single<String> createWithRelatedData(UUID tenantId, CreateExperimentRequest request);

  /**
   * Gets current experiment data as a map.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting map of experiment data
   */
  Single<Map<String, Object>> getExperimentData(String projectKey, UUID experimentId);

  /**
   * Updates experiment fields partially based on provided request map.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> updatePartial(String projectKey, UUID experimentId, Map<String, Object> request);

  /**
   * Updates experiment with tags and logs in a transaction.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to update (null if no tag update)
   * @param previousData experiment data before update
   * @param updatedBy user who updated the experiment
   * @return Single emitting true on success
   */
  Single<Boolean> updateWithTransaction(
      String projectKey,
      UUID experimentId,
      UpdateExperimentRequest request,
      List<String> tags,
      List<String> owners,
      Map<String, List<String>> metrics,
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
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags);

  /**
   * Gets all tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting list of tag names
   */
  Single<List<String>> getTags(SqlConnection connection, String projectKey, UUID experimentId);

  /**
   * Deletes tags (hard delete).
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to delete
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteTags(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags);

  /**
   * Deletes all tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteTags(SqlConnection connection, String projectKey, UUID experimentId);

  // ==================== Owner Operations ====================

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> batchInsertOwners(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> owners);

  /**
   * Deletes all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteOwners(SqlConnection connection, String projectKey, UUID experimentId);

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
      String projectKey,
      UUID experimentId,
      Object previousData,
      Object currentData,
      String updatedBy);

  // ==================== Analysis Operations ====================

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param metrics map of metrics (primary and secondary)
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertAnalysis(
      SqlConnection connection,
      String projectKey,
      UUID experimentId,
      Map<String, List<String>> metrics);

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
      String projectKey,
      UUID experimentId,
      String config,
      String primaryMetrics,
      String secondaryMetrics,
      String metricTokens);
}
