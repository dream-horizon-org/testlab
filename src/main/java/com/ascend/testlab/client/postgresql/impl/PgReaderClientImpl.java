package com.ascend.testlab.client.postgresql.impl;

import com.ascend.testlab.client.postgresql.AbstractPostgreSQLClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.config.PostgreSQLConfig;
import com.ascend.testlab.constants.postgresql.ReadQuery;
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

public class PgReaderClientImpl extends AbstractPostgreSQLClient implements PgReaderClient {

  @Inject
  public PgReaderClientImpl(Vertx vertx, PostgreSQLConfig postgreSQLConfig) {
    super(vertx, postgreSQLConfig.getReaderConfig());
  }

  @Override
  public Single<Boolean> isConnected() {
    return rxExecute(ReadQuery.HEALTH_CHECK).map(rows -> 1 == rows.size());
  }

  @Override
  public Completable close() {
    return super.rxClose();
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
