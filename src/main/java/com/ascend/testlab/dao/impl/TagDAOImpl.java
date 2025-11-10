package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.TagDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of TagDAO for PostgreSQL database operations.
 *
 * <p>This class handles all tag-related database operations including batch insert and delete
 * operations with proper type conversions for PostgreSQL-specific types.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class TagDAOImpl implements TagDAO {

  private final PgWriterClient pgWriterClient;

  /**
   * Constructs TagDAOImpl with PostgreSQL writer client.
   *
   * @param pgWriterClient PostgreSQL writer client for database operations
   */
  @Inject
  public TagDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
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
  @Override
  public Single<Boolean> batchInsertTags(
      SqlConnection connection, UUID projectKey, UUID experimentId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      log.debug("No tags to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Batch inserting {} tags for experimentId: {}, projectKey: {}",
        tags.size(),
        experimentId,
        projectKey);

    // Build list of tuples for batch insert
    List<Tuple> tuples = new ArrayList<>();
    for (String tag : tags) {
      Tuple params =
          Tuple.tuple()
              .addString(experimentId.toString())
              .addString(projectKey.toString())
              .addString(tag);
      tuples.add(params);
    }

    // Execute batch insert
    return pgWriterClient
        .executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_TAG, tuples)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully batch inserted {} tags for experimentId: {}, projectKey: {}",
                    tags.size(),
                    experimentId,
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to batch insert {} tags for experimentId: {}, error: {}",
                    tags.size(),
                    experimentId,
                    error.getMessage(),
                    error))
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
   * Deletes all tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> deleteTags(SqlConnection connection, UUID projectKey, UUID experimentId) {
    log.debug("DAO: Deleting tags for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple().addString(projectKey.toString()).addString(experimentId.toString());

    return pgWriterClient
        .execute(connection, WriteQuery.DELETE_EXPERIMENT_TAGS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully deleted tags for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to delete tags for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for tag deletion, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }
}
