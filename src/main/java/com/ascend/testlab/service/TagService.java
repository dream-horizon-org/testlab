package com.ascend.testlab.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Tag business logic.
 *
 * <p>Provides methods for managing experiment tags with proper validation and error handling.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface TagService {

  /**
   * Batch inserts tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags);

  /**
   * Updates tags for an experiment by merging with existing tags.
   *
   * <p>Gets existing active tags, marks removed tags as inactive (status=0), and inserts new tags
   * with status=1.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param newTags list of new tags to set
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> updateTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> newTags);

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
