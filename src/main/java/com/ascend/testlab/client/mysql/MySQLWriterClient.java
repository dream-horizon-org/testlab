package com.ascend.testlab.client.mysql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.function.Function;

public interface MySQLWriterClient {
  Completable close();

  Single<Boolean> execute(SqlConnection connection, String preparedQuery, Tuple tuple);

  Single<Long> executeAndGenerateId(SqlConnection connection, String preparedQuery, Tuple tuple);

  Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples);

  <T> Maybe<T> executeWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction);
}
