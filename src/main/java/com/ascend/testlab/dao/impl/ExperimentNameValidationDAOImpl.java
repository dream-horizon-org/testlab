package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentNameValidationDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ExperimentNameValidationDAOImpl implements ExperimentNameValidationDAO {

  private final MySQLReaderClient mySQLReaderClient;

  @Override
  public Single<Boolean> isExperimentNameExists(UUID projectId, String name) {
    return mySQLReaderClient
        .fetchOne(
            ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS,
            Tuple.tuple().addString(projectId.toString()).addString(name),
            this::mapRowToBoolean)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Error checking experiment name existence for project {} and name {}: {}",
                  projectId,
                  name,
                  err.getMessage());
              return Single.error(err);
            });
  }

  private Boolean mapRowToBoolean(Row row) {
    return row.getInteger(0) > 0;
  }
}
