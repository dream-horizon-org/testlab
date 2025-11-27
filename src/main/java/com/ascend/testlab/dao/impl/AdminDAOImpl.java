package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.entity.ExperimentHistoryEntry;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.util.CommonUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AdminDAO interface for database operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminDAO
 * @see PgReaderClient
 */
@Slf4j
public class AdminDAOImpl implements AdminDAO {

  /** The PostgreSQL reader client. */
  private final PgReaderClient pgReaderClient;

  /**
   * Constructor for the AdminDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public AdminDAOImpl(PgReaderClient pgReaderClient) {
    this.pgReaderClient = pgReaderClient;
  }

  /** {@inheritDoc} */
  @Override
  public Single<List<String>> fetchTags(String projectKey) {
    return pgReaderClient.fetchAll(
        ReadQuery.FETCH_TAGS,
        Tuple.tuple().addString(projectKey),
        (row -> row.getString(Columns.TAG)));
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isExperimentKeyAvailable(String projectKey, String experimentKey) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.CHECK_EXPERIMENT_KEY,
            Tuple.tuple().addString(projectKey).addString(experimentKey),
            row -> row.getBoolean(0))
        .map(list -> list.isEmpty() || !list.get(0));
  }

  /** {@inheritDoc} */
  @Override
  public Single<ExperimentHistoryResponse> fetchExperimentHistory(
      ExperimentHistoryRequest request) {
    int offset = CommonUtil.calculateOffset(request.getPage(), request.getLimit());
    return pgReaderClient
        .fetchAll(
            ReadQuery.FETCH_EXPERIMENT_HISTORY,
            Tuple.tuple()
                .addString(request.getProjectKey())
                .addString(request.getExperimentId())
                .addInteger(request.getLimit())
                .addInteger(offset),
            row -> row)
        .map(
            rows -> {
              int totalCount = rows.isEmpty() ? 0 : rows.get(0).getInteger(Columns.TOTAL_COUNT);
              List<ExperimentHistoryEntry> historyEntries =
                  rows.stream().map(this::mapRowToExperimentHistoryEntry).toList();
              return ExperimentHistoryResponse.builder()
                  .experimentId(request.getExperimentId())
                  .history(historyEntries)
                  .pagination(
                      PaginationMeta.builder()
                          .currentPage(request.getPage())
                          .pageSize(historyEntries.size())
                          .totalCount(totalCount)
                          .build())
                  .build();
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Integer> getExperimentHistoryCount(String projectKey, String experimentId) {
    return pgReaderClient
        .fetchOne(
            ReadQuery.FETCH_EXPERIMENT_HISTORY_COUNT,
            Tuple.tuple().addString(projectKey).addString(experimentId),
            row -> row.getInteger(0))
        .switchIfEmpty(Single.just(0));
  }

  /**
   * Maps a database row to an ExperimentHistoryEntry.
   *
   * @param row the database row
   * @return the mapped ExperimentHistoryEntry
   */
  private ExperimentHistoryEntry mapRowToExperimentHistoryEntry(Row row) {
    return ExperimentHistoryEntry.builder()
        .updatedBy(row.getString(Columns.UPDATED_BY))
        .previousData(row.getJsonObject(Columns.PREVIOUS_DATA))
        .currentData(row.getJsonObject(Columns.CURRENT_DATA))
        .createdAt(row.getOffsetDateTime(Columns.CREATED_AT).toInstant())
        .updatedAt(row.getOffsetDateTime(Columns.UPDATED_AT).toInstant())
        .build();
  }
}
