package com.ascend.testlab.client.postgresql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.function.Function;

public interface PgWriterClient {
  Completable close();

  Single<Boolean> execute(String preparedQuery);

  Single<Boolean> execute(SqlConnection connection, String preparedQuery);

  Single<Boolean> execute(String preparedQuery, Tuple tuple);

  Single<Boolean> execute(SqlConnection connection, String preparedQuery, Tuple tuple);

  <N extends Number> Single<N> executeAndGenerateId(
      SqlConnection connection, String preparedQuery, Tuple tuple, Function<Row, N> idMapper);

  Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples);

  <N extends Number> Single<N> executeAndGenerateId(
      String preparedQuery, Tuple tuple, Function<Row, N> idMapper);

  <T> Maybe<T> executeWithTransaction(Function<SqlConnection, Maybe<T>> transactionalFunction);

  <T> Single<T> executeAndFetchOne(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);
}
