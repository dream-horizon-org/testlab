package com.ascend.testlab.client.mysql.impl;

import com.ascend.testlab.client.mysql.AbstractMySQLClient;
import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.config.MySQLConfig;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.mysqlclient.MySQLClient;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.function.Function;

public class MySQLWriterClientImpl extends AbstractMySQLClient implements MySQLWriterClient {

  @Inject
  public MySQLWriterClientImpl(Vertx vertx, MySQLConfig mySQLConfig) {
    super(vertx, mySQLConfig.getWriterConfig());
  }

  @Override
  public Completable close() {
    return super.rxClose();
  }

  @Override
  public Single<Boolean> execute(SqlConnection connection, String preparedQuery, Tuple tuple) {
    return rxExecute(connection, preparedQuery, tuple).map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Single<Long> executeAndGenerateId(
      SqlConnection connection, String preparedQuery, Tuple tuple) {
    return rxExecute(connection, preparedQuery, tuple)
        .map(res -> res.property(MySQLClient.LAST_INSERTED_ID));
  }

  @Override
  public Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples) {
    return rxExecute(connection, preparedQuery, tuples).map(rows -> rows.rowCount() > 0);
  }

  @Override
  public <T> Maybe<T> executeWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction) {
    return super.rxWithTransaction(transactionalFunction);
  }
}
