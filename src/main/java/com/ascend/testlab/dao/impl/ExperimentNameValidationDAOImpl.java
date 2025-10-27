package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.ExperimentNameValidationDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
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
        .<Long>fetchAll(
            ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS,
            Tuple.tuple().addString(projectId.toString()).addString(name),
            (row -> row.getLong(0)))
        .map(List::isEmpty);
  }
}
