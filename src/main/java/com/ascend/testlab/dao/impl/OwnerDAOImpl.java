package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.OwnerDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of OwnerDAO for PostgreSQL database operations.
 *
 * <p>This class handles all owner-related database operations including insert and delete
 * operations with proper type conversions for PostgreSQL-specific types.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class OwnerDAOImpl implements OwnerDAO {

  private final PgWriterClient pgWriterClient;

  /**
   * Constructs OwnerDAOImpl with PostgreSQL writer client.
   *
   * @param pgWriterClient PostgreSQL writer client for database operations
   */
  @Inject
  public OwnerDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
  }

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param owner owner name/email
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> insertOwner(
      SqlConnection connection, UUID projectKey, UUID experimentId, String owner) {
    if (owner == null || owner.isEmpty()) {
      log.debug("No owner to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Inserting owner: {} for experimentId: {}, projectKey: {}",
        owner,
        experimentId,
        projectKey);

    Tuple params =
        Tuple.tuple()
            .addString(experimentId.toString())
            .addString(projectKey.toString())
            .addString(owner);

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_OWNER, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted owner: {} for experimentId: {}, projectKey: {}",
                    owner,
                    experimentId,
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to insert owner: {} for experimentId: {}, error: {}",
                    owner,
                    experimentId,
                    error.getMessage(),
                    error))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for owner insert, experimentId: {}, owner: {}, error: {}",
                  experimentId,
                  owner,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Deletes all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> deleteOwners(
      SqlConnection connection, UUID projectKey, UUID experimentId) {
    log.debug(
        "DAO: Deleting owners for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple().addString(projectKey.toString()).addString(experimentId.toString());

    return pgWriterClient
        .execute(connection, WriteQuery.DELETE_EXPERIMENT_OWNERS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully deleted owners for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to delete owners for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for owner deletion, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }
}
