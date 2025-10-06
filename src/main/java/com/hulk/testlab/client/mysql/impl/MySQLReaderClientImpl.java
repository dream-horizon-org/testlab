package com.hulk.testlab.client.mysql.impl;

import com.hulk.testlab.client.mysql.AbstractMySQLClient;
import com.hulk.testlab.client.mysql.MySQLReaderClient;
import com.hulk.testlab.config.MySQLConfig;
import com.hulk.testlab.constants.mysql.ReadQuery;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class MySQLReaderClientImpl extends AbstractMySQLClient implements MySQLReaderClient {

  @Inject
  public MySQLReaderClientImpl(Vertx vertx, MySQLConfig mySQLConfig) {
    super(vertx, mySQLConfig.getReaderConfig());
  }

  @Override
  public Completable close() {
    return super.rxClose();
  }

  @Override
  public Single<Boolean> isConnected() {
    return rxExecute(ReadQuery.HEALTH_CHECK).map(rows -> 1 == rows.size());
  }

  @Override
  public <T> Single<List<T>> fetchAll(String query, Function<Row, T> rowMapper) {
    return rxExecute(query).map(rows -> toList(rows, rowMapper));
  }

  @Override
  public <T> Single<List<T>> fetchAll(
      String preparedQuery, Tuple tuple, Function<Row, T> rowMapper) {
    return rxExecute(preparedQuery, tuple).map(rows -> toList(rows, rowMapper));
  }

  @Override
  public <T> Single<T> fetchOne(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper) {
    return rxExecute(preparedQuery, tuple)
        .map(
            rows -> {
              if (rows.size() == 0) {
                throw new NoSuchElementException("No rows fetched for the query");
              }
              return rowMapper.apply(rows.iterator().next());
            });
  }

  @Override
  public <K, V> Single<Map<K, V>> fetchMap(
      String query, Function<Row, K> keyMapper, Function<Row, V> valueMapper) {
    return rxExecute(query).map(rows -> toMap(rows, keyMapper, valueMapper));
  }

  @Override
  public <K, V> Single<Map<K, V>> fetchMap(
      String preparedQuery, Tuple tuple, Function<Row, K> keyMapper, Function<Row, V> valueMapper) {
    return rxExecute(preparedQuery, tuple).map(rows -> toMap(rows, keyMapper, valueMapper));
  }
}
