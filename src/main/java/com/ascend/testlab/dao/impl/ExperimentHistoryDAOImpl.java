package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentHistoryDAO;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ExperimentHistoryDAOImpl implements ExperimentHistoryDAO {

  private final MySQLReaderClient mySQLReaderClient;

  @Override
  public Single<List<ExperimentHistoryEntry>> fetchExperimentHistory(
      UUID projectId, UUID experimentId) {
    return mySQLReaderClient
        .fetchAll(
            ReadQuery.FETCH_EXPERIMENT_HISTORY,
            Tuple.tuple().addString(projectId.toString()).addString(experimentId.toString()),
            this::mapRowToHistoryEntry)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error fetching experiment history for project {} and experiment {}: {}",
                  projectId,
                  experimentId,
                  err.getMessage());
              return Single.error(err);
            });
  }

  private ExperimentHistoryEntry mapRowToHistoryEntry(Row row) {
    return new ExperimentHistoryEntry(
        row.getString("updated_by"),
        row.getJsonObject("previous_data") != null
            ? row.getJsonObject("previous_data").encode()
            : null,
        row.getJsonObject("current_data") != null
            ? row.getJsonObject("current_data").encode()
            : null,
        row.getLocalDateTime("created_at") != null
            ? row.getLocalDateTime("created_at").toString()
            : null,
        row.getLocalDateTime("updated_at") != null
            ? row.getLocalDateTime("updated_at").toString()
            : null);
  }
}
