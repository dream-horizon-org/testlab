package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.PartitionStatus;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.PartitionDAO;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the PartitionDAO interface.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 * @see PartitionDAO
 */
@Slf4j
public class PartitionDAOImpl implements PartitionDAO {

  private static final List<String> PARTITIONED_TABLES =
      List.of("experiments", "owners", "tags", "experiment_update_log", "experiment_analysis");

  /** The PostgreSQL writer client. */
  private final PgWriterClient pgWriterClient;

  /**
   * Constructor for the PartitionDAOImpl.
   *
   * @param pgWriterClient the PostgreSQL writer client
   */
  @Inject
  public PartitionDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
  }

  /** {@inheritDoc} */
  @Override
  public Single<PartitionResponse> createProjectPartition(String projectKey, String userId) {

    Instant now = Instant.now();
    long nowMillis = now.toEpochMilli();
    String createdBy = (userId == null || userId.isBlank()) ? "system" : userId;

    return pgWriterClient.executeWithTransaction(
        connection ->
            upsertPartitionMetadataReturningStatus(connection, projectKey, createdBy, nowMillis)
                .flatMap(
                    status -> {
                      if (status == PartitionStatus.SUCCESS) {
                        return Maybe.just(
                            PartitionResponse.builder()
                                .projectKey(projectKey)
                                .status(PartitionStatus.SUCCESS.name())
                                .message("Partitions already exist")
                                .build());
                      }
                      return runPartitionCreationFlow(connection, projectKey, nowMillis);
                    }),
        PartitionResponse.builder()
            .projectKey(projectKey)
            .status(PartitionStatus.SUCCESS.name())
            .message("Partitions already exist")
            .build());
  }

  /**
   * Upserts the partition metadata and returns the resulting status.
   *
   * @param connection the SQL connection
   * @param projectKey the project key
   * @param createdBy the created by
   * @param nowMillis the current time in epoch milliseconds
   * @return a Maybe that emits the partition status
   */
  private Maybe<PartitionStatus> upsertPartitionMetadataReturningStatus(
      SqlConnection connection, String projectKey, String createdBy, long nowMillis) {
    return connection
        .preparedQuery(WriteQuery.UPSERT_PARTITION_METADATA_RETURNING_STATUS)
        .rxExecute(
            Tuple.of(projectKey, PartitionStatus.CREATING.name(), createdBy, nowMillis, nowMillis))
        .map(
            rows -> {
              if (rows.size() == 0) {
                throw new IllegalStateException(
                    "No partition metadata returned for projectKey=" + projectKey);
              }
              Row row = rows.iterator().next();
              return PartitionStatus.valueOf(row.getString(Columns.STATUS));
            })
        .toMaybe();
  }

  /**
   * Runs the partition creation flow.
   *
   * @param connection the SQL connection
   * @param projectKey the project key
   * @param nowMillis the current time in epoch milliseconds
   * @return a Maybe that emits the partition response
   */
  private Maybe<PartitionResponse> runPartitionCreationFlow(
      SqlConnection connection, String projectKey, long nowMillis) {
    return createAllPartitions(connection, projectKey)
        .andThen(
            updatePartitionMetadataStatus(
                connection, projectKey, PartitionStatus.SUCCESS, nowMillis))
        .andThen(
            Maybe.just(
                PartitionResponse.builder()
                    .projectKey(projectKey)
                    .status(PartitionStatus.SUCCESS.name())
                    .message("Partitions created successfully")
                    .build()));
  }

  /**
   * Creates all partitions for a given project key.
   *
   * @param connection the SQL connection
   * @param projectKey the project key
   * @return a Completable that completes when the partitions are created
   */
  private Completable createAllPartitions(SqlConnection connection, String projectKey) {

    return Completable.concat(
        PARTITIONED_TABLES.stream()
            .map(table -> createPartition(connection, table, projectKey))
            .toList());
  }

  /**
   * Creates a partition for a given parent table and project key.
   *
   * @param connection the SQL connection
   * @param parentTable the parent table
   * @param projectKey the project key
   * @return a Completable that completes when the partition is created
   */
  private Completable createPartition(
      SqlConnection connection, String parentTable, String projectKey) {

    String ddl =
        WriteQuery.buildCreateListPartitionQuery(Constants.SCHEMA, parentTable, projectKey);

    return pgWriterClient.execute(connection, ddl).ignoreElement();
  }

  /**
   * Updates the partition metadata status for a given project key.
   *
   * @param connection the SQL connection
   * @param projectKey the project key
   * @param status the status
   * @param nowMillis the current time in epoch milliseconds
   * @return a Completable that completes when the partition metadata status is updated
   */
  private Completable updatePartitionMetadataStatus(
      SqlConnection connection, String projectKey, PartitionStatus status, long nowMillis) {

    return pgWriterClient
        .execute(
            connection,
            WriteQuery.UPDATE_PARTITION_STATUS,
            Tuple.of(status.name(), nowMillis, projectKey))
        .ignoreElement();
  }
}
