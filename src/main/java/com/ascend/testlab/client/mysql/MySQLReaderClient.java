package com.ascend.testlab.client.mysql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface MySQLReaderClient {
  Completable close();

  Single<Boolean> isConnected();

  <T> Single<List<T>> fetchAll(String query, Function<Row, T> rowMapper);

  <T> Single<List<T>> fetchAll(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);

  <T> Single<T> fetchOne(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);

  <K, V> Single<Map<K, V>> fetchMap(
      String query, Function<Row, K> keyMapper, Function<Row, V> valueMapper);

  <K, V> Single<Map<K, V>> fetchMap(
      String preparedQuery, Tuple tuple, Function<Row, K> keyMapper, Function<Row, V> valueMapper);
}
