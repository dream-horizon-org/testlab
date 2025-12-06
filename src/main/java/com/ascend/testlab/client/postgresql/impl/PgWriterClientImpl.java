package com.ascend.testlab.client.postgresql.impl;

import com.ascend.testlab.client.postgresql.AbstractPostgreSQLClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.config.PostgreSQLConfig;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Implementation of the PgWriterClient interface. Uses the AbstractPostgreSQLClient to interact
 * with the PostgreSQL database.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see AbstractPostgreSQLClient
 * @see PgWriterClient
 */
public class PgWriterClientImpl extends AbstractPostgreSQLClient implements PgWriterClient {

  /**
   * Constructor for the PgWriterClientImpl.
   *
   * @param vertx the Vertx instance
   * @param postgreSQLConfig the PostgreSQL configuration
   */
  @Inject
  public PgWriterClientImpl(Vertx vertx, PostgreSQLConfig postgreSQLConfig) {
    super(vertx, postgreSQLConfig.getWriterConfig());
  }

  /** {@inheritDoc} */
  @Override
  public Completable close() {
    return super.rxClose();
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> execute(String preparedQuery) {
    return rxExecute(preparedQuery).map(rows -> rows.rowCount() > 0);
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> execute(SqlConnection connection, String preparedQuery) {
    return rxExecute(connection, preparedQuery).map(rows -> rows.rowCount() > 0);
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> execute(String preparedQuery, Tuple tuple) {
    return rxExecute(preparedQuery, tuple).map(rows -> rows.rowCount() > 0);
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> execute(SqlConnection connection, String preparedQuery, Tuple tuple) {
    return rxExecute(connection, preparedQuery, tuple).map(rows -> rows.rowCount() > 0);
  }

  /** {@inheritDoc} */
  @Override
  public <N extends Number> Single<N> executeAndGenerateId(
      SqlConnection connection, String preparedQuery, Tuple tuple, Function<Row, N> idMapper) {
    return rxExecute(connection, preparedQuery, tuple)
        .map(
            rows -> {
              if (rows.size() == 0) {
                throw new IllegalStateException("No rows inserted");
              }
              return idMapper.apply(rows.iterator().next());
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> executeMultiple(
      SqlConnection connection, String preparedQuery, List<Tuple> tuples) {
    return rxExecute(connection, preparedQuery, tuples).map(rows -> rows.rowCount() > 0);
  }

  /** {@inheritDoc} */
  @Override
  public <N extends Number> Single<N> executeAndGenerateId(
      String preparedQuery, Tuple tuple, Function<Row, N> idMapper) {
    return rxExecute(preparedQuery, tuple)
        .map(
            rows -> {
              if (rows.size() == 0) {
                throw new IllegalStateException("No rows inserted");
              }
              return idMapper.apply(rows.iterator().next());
            });
  }

  /** {@inheritDoc} */
  @Override
  public <T> Single<T> executeWithTransaction(
      Function<SqlConnection, Maybe<T>> transactionalFunction, T defaultValue) {
    return super.rxWithTransaction(transactionalFunction, defaultValue);
  }

  /** {@inheritDoc} */
  @Override
  public <T> Single<T> executeAndFetchOne(
      String preparedQuery, Tuple tuple, Function<Row, T> rowMapper) {
    return rxExecute(preparedQuery, tuple)
        .map(
            rows -> {
              if (rows.size() == 0) {
                throw new NoSuchElementException("No rows found");
              }
              return rowMapper.apply(rows.iterator().next());
            });
  }
}
