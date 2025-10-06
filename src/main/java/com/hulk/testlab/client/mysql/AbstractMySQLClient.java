package com.hulk.testlab.client.mysql;

import com.hulk.testlab.config.MySQLConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
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

public abstract class AbstractMySQLClient {

  private final MySQLPool mySQLPool;
  private final Integer retryCount;

  protected AbstractMySQLClient(Vertx vertx, MySQLConfig.BaseConfig mySQLBaseConfig) {
    this.mySQLPool =
        MySQLPool.pool(
            vertx,
            getConnectOptions(mySQLBaseConfig.getConnectOptions()),
            getPoolOptions(mySQLBaseConfig.getPoolOptions()));
    this.retryCount = mySQLBaseConfig.getRetryCount();
  }

  public Completable rxClose() {
    return this.mySQLPool.rxClose();
  }

  protected Single<RowSet<Row>> rxExecute(String query) {
    return this.mySQLPool.query(query).rxExecute().retry(this.retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(String preparedQuery, Tuple tuple) {
    return this.mySQLPool.preparedQuery(preparedQuery).rxExecute(tuple).retry(this.retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, Tuple tuple) {
    return connection.preparedQuery(preparedQuery).rxExecute(tuple).retry(this.retryCount);
  }

  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples) {
    return connection.preparedQuery(preparedQuery).rxExecuteBatch(tuples).retry(this.retryCount);
  }

  protected <T> Maybe<T> rxWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction) {
    return this.mySQLPool.rxWithTransaction(transactionalFunction).retry(this.retryCount);
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

  protected static MySQLConnectOptions getConnectOptions(
      MySQLConfig.ConnectOptions connectOptions) {
    return new MySQLConnectOptions()
        .setHost(connectOptions.getHost())
        .setPort(connectOptions.getPort())
        .setUser(connectOptions.getUser())
        .setPassword(connectOptions.getPassword())
        .setDatabase(connectOptions.getDatabase())
        .setConnectTimeout(connectOptions.getConnectTimeout())
        .setUseAffectedRows(connectOptions.getUseAffectedRows())
        .setCachePreparedStatements(connectOptions.getCachePreparedStatements());
  }

  protected static PoolOptions getPoolOptions(MySQLConfig.PoolOptions poolOptions) {
    return new PoolOptions()
        .setMaxSize(poolOptions.getMaxSize())
        .setMaxWaitQueueSize(poolOptions.getMaxWaitQueueSize());
  }
}
