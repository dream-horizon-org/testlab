package com.ascend.testlab.dao.impl;

import com.aerospike.client.Key;
import com.aerospike.client.policy.BatchPolicy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.entity.ExperimentHistoryEntry;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.util.CommonUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
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

  /** The Aerospike client. */
  private final AerospikeClient aerospikeClient;

  /** The Aerospike config. */
  private final AerospikeConfig aerospikeConfig;

  /**
   * Constructor for the AdminDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public AdminDAOImpl(
      PgReaderClient pgReaderClient,
      AerospikeClient aerospikeClient,
      AerospikeConfig aerospikeConfig) {
    this.pgReaderClient = pgReaderClient;
    this.aerospikeClient = aerospikeClient;
    this.aerospikeConfig = aerospikeConfig;
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
            ReadQuery.GET_EXPERIMENT_HISTORY_COUNT,
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

  /** {@inheritDoc} */
  @Override
  public Single<Map<String, Long>> getVariantCount(String projectKey, Experiment experiment) {
    if (Objects.isNull(experiment.getVariants()) || experiment.getVariants().isEmpty()) {
      return Single.just(new HashMap<>());
    }

    List<String> variants = experiment.getVariants().keySet().stream().toList();

    List<Key> keys =
        getKeyFromVariantName(projectKey, String.valueOf(experiment.getExperimentId()), variants);

    BatchPolicy batchPolicy = new BatchPolicy();
    batchPolicy.sendKey = true;

    return aerospikeClient
        .get(batchPolicy, keys, aerospikeConfig.getVariantCountBin())
        .map(
            records -> {
              if (records == null || records.isEmpty()) return new HashMap<String, Long>();

              Map<String, Long> variantCountMap = new HashMap<>();

              for (int i = 0; i < records.size(); i++) {
                if (records.get(i) == null) continue;
                Long count = records.get(i).getLong(aerospikeConfig.getVariantCountBin());
                String variantName = variants.get(i);

                variantCountMap.put(variantName, count);
              }

              return variantCountMap;
            })
        .onErrorReturn(
            err -> {
              log.error(
                  "Failed to fetch variant count for project: {}", experiment.getProjectKey(), err);
              return new HashMap<>();
            });
  }

  /** Generates Aerospike Key objects from variant names for a given experiment. */
  private List<Key> getKeyFromVariantName(
      String projectKey, String experimentId, List<String> variants) {
    String set = CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey);
    return variants.stream()
        .map(
            variantName ->
                new Key(
                    aerospikeConfig.getNamespace(),
                    set,
                    experimentId + Constants.COLON + variantName))
        .toList();
  }
}
