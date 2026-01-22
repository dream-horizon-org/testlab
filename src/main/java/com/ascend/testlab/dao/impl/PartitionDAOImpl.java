package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.PartitionStatus;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.PartitionDAO;
import com.ascend.testlab.dto.entity.experiment.PartitionMetadata;
import com.ascend.testlab.dto.request.PartitionRequest;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionDAOImpl implements PartitionDAO {

  private static final String SCHEMA = "experiment";

  private static final List<String> PARTITIONED_TABLES =
      List.of("experiments", "owners", "tags", "experiment_update_log", "experiment_analysis");

  private final PgReaderClient pgReaderClient;
  private final PgWriterClient pgWriterClient;
  private final ObjectMapper objectMapper;

  @Inject
  public PartitionDAOImpl(
      PgReaderClient pgReaderClient, PgWriterClient pgWriterClient, ObjectMapper objectMapper) {

    this.pgReaderClient = pgReaderClient;
    this.pgWriterClient = pgWriterClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public Single<PartitionResponse> createProjectPartition(PartitionRequest request) {

    String projectKey = request.getProjectKey();
    OffsetDateTime now = OffsetDateTime.now();
    String createdBy = "system";

    return pgWriterClient.executeWithTransaction(
        connection ->
            fetchPartitionMetadata(connection, projectKey)
                .flatMap(
                    metadata -> {
                      if (metadata.getStatus() == PartitionStatus.SUCCESS) {
                        return Maybe.just(
                            PartitionResponse.builder()
                                .projectKey(projectKey)
                                .status(PartitionStatus.SUCCESS.name())
                                .idempotent(true)
                                .build());
                      }
                      return runPartitionCreationFlow(connection, projectKey, createdBy, now);
                    })
                .switchIfEmpty(runPartitionCreationFlow(connection, projectKey, createdBy, now)),
        PartitionResponse.builder()
            .projectKey(projectKey)
            .status(PartitionStatus.SUCCESS.name())
            .idempotent(true)
            .build());
  }

  private Maybe<PartitionMetadata> fetchPartitionMetadata(
      SqlConnection connection, String projectKey) {

    return pgReaderClient
        .fetchOne(
            ReadQuery.GET_PARTITION_METADATA, Tuple.of(projectKey), this::mapPartitionMetadata)
        .onErrorResumeNext(
            error -> {
              log.error("Failed to fetch partition metadata for projectKey={}", projectKey, error);
              return Maybe.error(error);
            });
  }

  private Maybe<PartitionResponse> runPartitionCreationFlow(
      SqlConnection connection, String projectKey, String createdBy, OffsetDateTime now) {
    return upsertPartitionMetadataCreating(connection, projectKey, createdBy, now)
        .andThen(createAllPartitions(connection, projectKey))
        .andThen(
            updatePartitionMetadataStatus(connection, projectKey, PartitionStatus.SUCCESS, now))
        .andThen(
            Maybe.just(
                PartitionResponse.builder()
                    .projectKey(projectKey)
                    .status(PartitionStatus.SUCCESS.name())
                    .idempotent(false)
                    .build()));
  }

  private Completable upsertPartitionMetadataCreating(
      SqlConnection connection, String projectKey, String createdBy, OffsetDateTime now) {

    return pgWriterClient
        .execute(
            connection,
            WriteQuery.UPSERT_PARTITION_METADATA,
            Tuple.of(projectKey, PartitionStatus.CREATING.name(), createdBy, now, now))
        .ignoreElement();
  }

  private Completable createAllPartitions(SqlConnection connection, String projectKey) {

    return Completable.concat(
        PARTITIONED_TABLES.stream()
            .map(table -> createPartition(connection, table, projectKey))
            .toList());
  }

  private Completable createPartition(
      SqlConnection connection, String parentTable, String projectKey) {

    String ddl = WriteQuery.buildCreateListPartitionQuery(SCHEMA, parentTable, projectKey);

    return pgWriterClient.execute(connection, ddl).ignoreElement();
  }

  private Completable updatePartitionMetadataStatus(
      SqlConnection connection, String projectKey, PartitionStatus status, OffsetDateTime now) {

    return pgWriterClient
        .execute(
            connection,
            WriteQuery.UPDATE_PARTITION_STATUS,
            Tuple.of(status.name(), now, projectKey))
        .ignoreElement();
  }

  private PartitionMetadata mapPartitionMetadata(Row row) {
    return new PartitionMetadata(
        row.getString("project_key"),
        PartitionStatus.valueOf(row.getString("status")),
        row.getString("created_by"),
        row.getOffsetDateTime("created_at"),
        row.getOffsetDateTime("updated_at"));
  }
}
