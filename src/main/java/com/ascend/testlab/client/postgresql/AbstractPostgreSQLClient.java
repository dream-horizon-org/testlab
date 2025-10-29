package com.ascend.testlab.client.postgresql;

import com.ascend.testlab.config.PostgreSQLConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractPostgreSQLClient {

  private final PgPool pgPool;
  private final Integer retryCount;

  protected AbstractPostgreSQLClient(
      Vertx vertx, PostgreSQLConfig.BaseConfig postgreSQLBaseConfig) {
    this.pgPool =
        PgPool.pool(
            vertx,
            getConnectOptions(postgreSQLBaseConfig.getConnectOptions()),
            getPoolOptions(postgreSQLBaseConfig.getPoolOptions()));
    this.retryCount = postgreSQLBaseConfig.getRetryCount();
  }

  protected Completable rxClose() {
    return pgPool.rxClose();
  }

  protected Single<RowSet<Row>> rxExecute(String query) {
    return pgPool.query(query).rxExecute().retry(retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(SqlConnection connection, String query) {
    return connection.preparedQuery(query).rxExecute().retry(retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(String preparedQuery, Tuple tuple) {
    return pgPool.preparedQuery(preparedQuery).rxExecute(tuple).retry(retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, Tuple tuple) {
    return connection.preparedQuery(preparedQuery).rxExecute(tuple).retry(retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples) {
    return connection.preparedQuery(preparedQuery).rxExecuteBatch(tuples).retry(retryCount);
  }

  protected <T> Maybe<T> rxWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction) {
    return pgPool.rxWithTransaction(transactionalFunction).retry(retryCount);
  }

  protected static <T> List<T> toList(RowSet<Row> rows, Function<Row, T> rowMapper) {
    List<T> resultList = new ArrayList<>();
    for (Row row : rows) {
      resultList.add(rowMapper.apply(row));
    }
    return resultList;
  }

  protected static <K, V> Map<K, V> toMap(
      RowSet<Row> rows, Function<Row, K> keyMapper, Function<Row, V> valueMapper) {
    Map<K, V> resultMap = new HashMap<>();
    for (Row row : rows) {
      resultMap.put(keyMapper.apply(row), valueMapper.apply(row));
    }
    return resultMap;
  }

  protected static PgConnectOptions getConnectOptions(
      PostgreSQLConfig.ConnectOptions connectOptions) {
    return new PgConnectOptions()
        .setHost(connectOptions.getHost())
        .setPort(connectOptions.getPort())
        .setDatabase(connectOptions.getDatabase())
        .setUser(connectOptions.getUser())
        .setPassword(connectOptions.getPassword())
        .setConnectTimeout(connectOptions.getConnectTimeout())
        .setCachePreparedStatements(connectOptions.getCachePreparedStatements());
  }

  protected static PoolOptions getPoolOptions(PostgreSQLConfig.PoolOptions poolOptions) {
    return new PoolOptions()
        .setMaxSize(poolOptions.getMaxSize())
        .setMaxWaitQueueSize(poolOptions.getMaxWaitQueueSize());
  }
}
