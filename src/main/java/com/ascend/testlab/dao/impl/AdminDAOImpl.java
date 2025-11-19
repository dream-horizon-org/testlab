package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import com.ascend.testlab.util.CommonUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the AdminDAO interface for database operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminDAO
 * @see PgReaderClient
 */
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
        ReadQuery.FETCH_TAGS, Tuple.tuple().addString(projectKey), (row -> row.getString("tag")));
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isExperimentNameAvailable(String projectKey, String experimentName) {
    String experimentKey = CommonUtil.getExperimentKey(experimentName);
    return pgReaderClient
        .fetchAll(
            ReadQuery.CHECK_EXPERIMENT_NAME,
            Tuple.tuple().addString(projectKey).addString(experimentKey),
            row -> row.getBoolean(0))
        .map(list -> list.isEmpty() ? true : !list.get(0));
  }

  /** {@inheritDoc} */
  @Override
  public Single<AdminDAO.ExperimentHistoryResult> fetchExperimentHistory(
      String projectKey, String experimentId, int limit, int offset) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.FETCH_EXPERIMENT_HISTORY,
            Tuple.tuple()
                .addString(projectKey)
                .addString(experimentId)
                .addInteger(limit)
                .addInteger(offset),
            row -> row)
        .map(
            rows -> {
              int totalCount = rows.isEmpty() ? 0 : rows.get(0).getInteger(Columns.TOTAL_COUNT);
              List<ExperimentHistoryEntry> historyEntries =
                  rows.stream()
                      .map(this::mapRowToExperimentHistoryEntry)
                      .collect(Collectors.toList());
              return new AdminDAO.ExperimentHistoryResult(historyEntries, totalCount);
            });
  }

  /**
   * Maps a database row to an ExperimentHistoryEntry.
   *
   * @param row the database row
   * @return the mapped ExperimentHistoryEntry
   */
  private ExperimentHistoryEntry mapRowToExperimentHistoryEntry(Row row) {
    return ExperimentHistoryEntry.builder()
        .updatedBy(row.getString("updated_by"))
        .previousData(row.getValue("previous_data"))
        .currentData(row.getValue("current_data"))
        .createdAt(
            row.getLocalDateTime("created_at") != null
                ? row.getLocalDateTime("created_at")
                    .atZone(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
                : null)
        .updatedAt(
            row.getLocalDateTime("updated_at") != null
                ? row.getLocalDateTime("updated_at")
                    .atZone(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
                : null)
        .build();
  }
}
