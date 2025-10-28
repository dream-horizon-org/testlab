package com.ascend.testlab.client.mysql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Interface for the client to interact with MySQL reader database. Contains methods to fetch data
 * from the database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface MySQLReaderClient {

  /**
   * Close the client and release resources.
   *
   * @return a Completable that completes when the client is closed
   */
  Completable close();

  /**
   * Check if the client is connected to MySQL reader database.
   *
   * @return a Single that emits true if the client is connected, false otherwise
   */
  Single<Boolean> isConnected();

  /**
   * Fetch all rows from the database.
   *
   * @param query the query to execute
   * @param rowMapper the function to map the rows to the result type
   * @param <T> the type of the result
   * @return a Single that emits the list of rows
   */
  <T> Single<List<T>> fetchAll(String query, Function<Row, T> rowMapper);

  /**
   * Fetch all rows from the database.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param rowMapper the function to map the rows to the result type
   * @param <T> the type of the result
   * @return a Single that emits the list of rows
   */
  <T> Single<List<T>> fetchAll(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);

  /**
   * Fetch one row from the database.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param rowMapper the function to map the row to the result type
   * @param <T> the type of the result
   * @return a Single that emits the row
   */
  <T> Single<T> fetchOne(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);

  /**
   * Fetch a map from the database.
   *
   * @param query the query to execute
   * @param keyMapper the function to map the key to the result type
   * @param valueMapper the function to map the value to the result type
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return a Single that emits the map
   */
  <K, V> Single<Map<K, V>> fetchMap(
      String query, Function<Row, K> keyMapper, Function<Row, V> valueMapper);

  /**
   * Fetch a map from the database.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param keyMapper the function to map the key to the result type
   * @param valueMapper the function to map the value to the result type
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return a Single that emits the map
   */
  <K, V> Single<Map<K, V>> fetchMap(
      String preparedQuery, Tuple tuple, Function<Row, K> keyMapper, Function<Row, V> valueMapper);
}
