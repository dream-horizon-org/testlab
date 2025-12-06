package com.ascend.testlab.client.postgresql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.function.Function;

/**
 * Interface for the client to interact with PostgreSQL writer database. Contains methods to insert
 * data into the database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface PgWriterClient {

  /**
   * Close the client and release resources.
   *
   * @return a Completable that completes when the client is closed
   */
  Completable close();

  /**
   * Execute a prepared query.
   *
   * @param preparedQuery the prepared query to execute
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> execute(String preparedQuery);

  /**
   * Execute a prepared query with a connection.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> execute(SqlConnection connection, String preparedQuery);

  /**
   * Execute a prepared query with a tuple.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> execute(String preparedQuery, Tuple tuple);

  /**
   * Execute a prepared query with a tuple and a connection.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> execute(SqlConnection connection, String preparedQuery, Tuple tuple);

  /**
   * Execute a prepared query with a tuple and generate an ID.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param idMapper the function to map the row to the ID
   * @param <N> the type of the ID, must extend Number
   * @return a Single that emits the ID of the inserted row
   */
  <N extends Number> Single<N> executeAndGenerateId(
      SqlConnection connection, String preparedQuery, Tuple tuple, Function<Row, N> idMapper);

  /**
   * Execute a prepared query with multiple tuples and a connection.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuples the tuples to execute the query with
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples);

  /**
   * Execute a prepared query with a tuple and generate an ID.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param idMapper the function to map the row to the ID
   * @param <N> the type of the ID, must extend Number
   * @return a Single that emits the ID of the inserted row
   */
  <N extends Number> Single<N> executeAndGenerateId(
      String preparedQuery, Tuple tuple, Function<Row, N> idMapper);

  /**
   * Execute a transactional function with a connection.
   *
   * @param transactionalFunction the function to execute with the connection
   * @param defaultValue the default value to return if the transaction returns empty
   * @param <T> the type of the result
   * @return a Single that emits the result of the transactional function
   */
  <T> Single<T> executeWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction, T defaultValue);

  /**
   * Execute a prepared query with a tuple and fetch one row.
   *
   * @param preparedQuery the prepared query to execute
   * @param tuple the tuple to execute the query with
   * @param rowMapper the function to map the row to the result type
   * @param <T> the type of the result
   * @return a Single that emits the row
   */
  <T> Single<T> executeAndFetchOne(String preparedQuery, Tuple tuple, Function<Row, T> rowMapper);
}
