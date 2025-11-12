package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object interface for Tag operations.
 *
 * <p>Provides methods for creating and managing experiment tags in the database.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface TagDAO {

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
}
