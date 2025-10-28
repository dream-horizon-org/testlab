package com.ascend.testlab.client.mysql;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.function.Function;

/**
 * Interface for the client to interact with MySQL writer database. Contains methods to insert data
 * into the database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface MySQLWriterClient {

  /**
   * Close the client and release resources.
   *
   * @return a Completable that completes when the client is closed
   */
  Completable close();

  /**
   * Execute a prepared query with a tuple.
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
   * @return a Single that emits the ID of the inserted row
   */
  Single<Long> executeAndGenerateId(SqlConnection connection, String preparedQuery, Tuple tuple);

  /**
   * Execute a prepared query with multiple tuples.
   *
   * @param connection the connection to the database
   * @param preparedQuery the prepared query to execute
   * @param tuples the tuples to execute the query with
   * @return a Single that emits true if the query is executed successfully, false otherwise
   */
  Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples);

  /**
   * Execute a transactional function with a connection.
   *
   * @param transactionalFunction the function to execute with the connection
   * @param <T> the type of the result
   * @return a Maybe that emits the result of the transactional function
   */
  // TODO: add support to begin/commit/rollback a transaction
  <T> Maybe<T> executeWithTransaction(Function<SqlConnection, Maybe<T>> transactionalFunction);
}
