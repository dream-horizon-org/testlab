package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.MySQLColumn;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentHistoryDAO;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
            (row ->
                new ExperimentHistoryEntry(
                    row.getString(MySQLColumn.UPDATED_BY.getColumn()),
                    row.getJsonObject(MySQLColumn.PREVIOUS_DATA.getColumn()) != null
                        ? row.getJsonObject(MySQLColumn.PREVIOUS_DATA.getColumn()).encode()
                        : null,
                    row.getJsonObject(MySQLColumn.CURRENT_DATA.getColumn()) != null
                        ? row.getJsonObject(MySQLColumn.CURRENT_DATA.getColumn()).encode()
                        : null,
                    row.getLocalDateTime(MySQLColumn.CREATED_AT.getColumn()) != null
                        ? row.getLocalDateTime(MySQLColumn.CREATED_AT.getColumn()).toString()
                        : null,
                    row.getLocalDateTime(MySQLColumn.UPDATED_AT.getColumn()) != null
                        ? row.getLocalDateTime(MySQLColumn.UPDATED_AT.getColumn()).toString()
                        : null)));
  }
}
