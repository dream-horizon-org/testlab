package com.ascend.testlab.client.mysql;

import com.ascend.testlab.config.MySQLConfig;
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

/**
 * Abstract class for the MySQL client. Contains the common methods to interact with the MySQL
 * database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractMySQLClient {

  /** The MySQL pool. */
  private final MySQLPool mySQLPool;

  /** The retry count. */
  private final Integer retryCount;

  /**
   * Constructor for the AbstractMySQLClient.
   *
   * @param vertx the Vertx instance
   * @param mySQLBaseConfig the MySQL configuration
   */
  protected AbstractMySQLClient(Vertx vertx, MySQLConfig.BaseConfig mySQLBaseConfig) {
    this.mySQLPool =
        MySQLPool.pool(
            vertx,
            getConnectOptions(mySQLBaseConfig.getConnectOptions()),
            getPoolOptions(mySQLBaseConfig.getPoolOptions()));
    this.retryCount = mySQLBaseConfig.getRetryCount();
  }

  /**
   * Close the MySQL pool.
   *
   * @return a Completable that completes when the pool is closed
   */
  public Completable rxClose() {
    return this.mySQLPool.rxClose();
  }

  /**
   * Execute a query.
   *
   * @param query the query to execute
   * @return a Single that emits the result of the query
   */
  protected Single<RowSet<Row>> rxExecute(String query) {
    return this.mySQLPool.query(query).rxExecute().retry(this.retryCount);
  }

  /**
   * Execute a prepared query with a tuple.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @return a Single that emits the result of the query
   */
  protected Single<RowSet<Row>> rxExecute(String preparedQuery, Tuple tuple) {
    return this.mySQLPool.preparedQuery(preparedQuery).rxExecute(tuple).retry(this.retryCount);
  }

  /**
   * Execute a prepared query with a tuple.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @return a Single that emits the result of the query
   */
  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, Tuple tuple) {
    return connection.preparedQuery(preparedQuery).rxExecute(tuple).retry(this.retryCount);
  }

  /**
   * Execute a prepared query with multiple tuples.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuples the tuples to execute the query with
   * @return a Single that emits the result of the query
   */
  protected Single<RowSet<Row>> rxExecute(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples) {
    return connection.preparedQuery(preparedQuery).rxExecuteBatch(tuples).retry(this.retryCount);
  }

  /**
   * Execute a transactional function with a connection.
   *
   * @param transactionalFunction the function to execute with the connection
   * @param <T> the type of the result
   * @return a Maybe that emits the result of the transactional function
   */
  protected <T> Maybe<T> rxWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction) {
    return this.mySQLPool.rxWithTransaction(transactionalFunction).retry(this.retryCount);
  }

  /**
   * Convert a RowSet to a List.
   *
   * @param rows the RowSet to convert
   * @param rowMapper the function to map the rows to the result type
   * @param <T> the type of the result
   * @return a List that emits the result of the conversion
   */
  protected static <T> List<T> toList(RowSet<Row> rows, Function<Row, T> rowMapper) {
    List<T> resultList = new ArrayList<>();
    for (Row row : rows) {
      resultList.add(rowMapper.apply(row));
    }
    return resultList;
  }

  /**
   * Convert a RowSet to a Map.
   *
   * @param rows the RowSet to convert
   * @param keyMapper the function to map the rows to the key type
   * @param valueMapper the function to map the rows to the value type
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return a Map that emits the result of the conversion
   */
  protected static <K, V> Map<K, V> toMap(
      RowSet<Row> rows, Function<Row, K> keyMapper, Function<Row, V> valueMapper) {
    Map<K, V> resultMap = new HashMap<>();
    for (Row row : rows) {
      resultMap.put(keyMapper.apply(row), valueMapper.apply(row));
    }
    return resultMap;
  }

  /**
   * Get the MySQL connect options.
   *
   * @param connectOptions the connect options
   * @return a MySQLConnectOptions
   */
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

  /**
   * Get the MySQL pool options.
   *
   * @param poolOptions the pool options
   * @return a PoolOptions
   */
  protected static PoolOptions getPoolOptions(MySQLConfig.PoolOptions poolOptions) {
    return new PoolOptions()
        .setMaxSize(poolOptions.getMaxSize())
        .setMaxWaitQueueSize(poolOptions.getMaxWaitQueueSize());
  }
}
