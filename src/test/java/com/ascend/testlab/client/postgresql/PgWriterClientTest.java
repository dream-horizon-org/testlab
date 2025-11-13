package com.ascend.testlab.client.postgresql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.impl.PgWriterClientImpl;
import com.ascend.testlab.config.PostgreSQLConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.PreparedQuery;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowIterator;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PgWriterClient}.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("PgWriterClient Tests")
class PgWriterClientTest {

  @Mock private PgPool mockPgPool;
  @Mock private PreparedQuery<RowSet<Row>> mockPreparedQuery;
  @Mock private RowSet<Row> mockRowSet;
  @Mock private Row mockRow;
  @Mock private SqlConnection mockConnection;

  private PgWriterClient client;
  private PostgreSQLConfig postgreSQLConfig;

  /**
   * Helper method to create a mock RowIterator from a list of rows. This must be called BEFORE
   * setting up the {@code when().thenReturn()} for mockRowSet.iterator() to avoid
   * UnfinishedStubbingException.
   *
   * @param rows the list of rows
   * @return a mock RowIterator
   */
  private RowIterator<Row> createRowIterator(List<Row> rows) {
    RowIterator<Row> mockIterator = mock(RowIterator.class);

    if (rows.isEmpty()) {
      lenient().when(mockIterator.hasNext()).thenReturn(false);
    } else {
      // For a single row: hasNext() returns true once, then false
      if (rows.size() == 1) {
        lenient().when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(rows.get(0));
      } else {
        // For multiple rows: create Boolean array with proper number of elements
        Boolean[] hasNextResults = new Boolean[rows.size() - 1];
        Arrays.fill(hasNextResults, true);
        lenient().when(mockIterator.hasNext()).thenReturn(true, hasNextResults).thenReturn(false);

        // Setup next() to return rows in sequence
        Row[] remainingRows = rows.subList(1, rows.size()).toArray(new Row[0]);
        when(mockIterator.next()).thenReturn(rows.get(0), remainingRows);
      }
    }

    return mockIterator;
  }

  @BeforeEach
  void setUp() {
    // Set up PostgreSQL config
    postgreSQLConfig = new PostgreSQLConfig();

    PostgreSQLConfig.BaseConfig writerConfig = new PostgreSQLConfig.BaseConfig();
    PostgreSQLConfig.ConnectOptions connectOptions = new PostgreSQLConfig.ConnectOptions();
    connectOptions.setHost("localhost");
    connectOptions.setPort(5432);
    connectOptions.setUser("test_user");
    connectOptions.setPassword("test_password");
    connectOptions.setDatabase("test_db");
    connectOptions.setConnectTimeout(5000);
    connectOptions.setCachePreparedStatements(true);

    PostgreSQLConfig.PoolOptions poolOptions = new PostgreSQLConfig.PoolOptions();
    poolOptions.setMaxSize(10);
    poolOptions.setMaxWaitQueueSize(50);

    writerConfig.setConnectOptions(connectOptions);
    writerConfig.setPoolOptions(poolOptions);
    writerConfig.setRetryCount(3);

    postgreSQLConfig.setWriterConfig(writerConfig);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create PgWriterClientImpl with valid config")
    void testConstructor(io.vertx.core.Vertx coreVertx, VertxTestContext testContext) {
      // Arrange & Act
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        PgWriterClient client =
            new PgWriterClientImpl(Vertx.newInstance(coreVertx), postgreSQLConfig);

        // Assert
        assertNotNull(client);
        testContext.completeNow();
      }
    }

    @Test
    @DisplayName("Should extract writer config from PostgreSQLConfig")
    void testExtractWriterConfig(io.vertx.core.Vertx coreVertx, VertxTestContext testContext) {
      // Arrange & Act
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenAnswer(
                invocation -> {
                  PgConnectOptions options = invocation.getArgument(1);
                  assertEquals("localhost", options.getHost());
                  assertEquals(5432, options.getPort());
                  assertEquals("test_user", options.getUser());
                  assertEquals("test_password", options.getPassword());
                  assertEquals("test_db", options.getDatabase());
                  return mockPgPool;
                });

        new PgWriterClientImpl(Vertx.newInstance(coreVertx), postgreSQLConfig);
        testContext.completeNow();
      }
    }
  }

  @Nested
  @DisplayName("Close Operation Tests")
  class CloseOperationTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should close pool successfully")
    void testClose(VertxTestContext testContext) {
      // Arrange
      when(mockPgPool.rxClose()).thenReturn(Completable.complete());

      // Act
      TestObserver<Void> testObserver = client.close().test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(mockPgPool, times(1)).rxClose();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle close error")
    void testCloseError(VertxTestContext testContext) {
      // Arrange
      Exception error = new RuntimeException("Close error");
      when(mockPgPool.rxClose()).thenReturn(Completable.error(error));

      // Act
      TestObserver<Void> testObserver = client.close().test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> e.getMessage().equals("Close error"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Execute Tests - Simple Query")
  class ExecuteSimpleQueryTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute query successfully and return true")
    void testExecuteSuccess(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ('John')";
      when(mockPgPool.query(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when no rows affected")
    void testExecuteNoRowsAffected(VertxTestContext testContext) {
      // Arrange
      String query = "UPDATE users SET name = 'Jane' WHERE id = 999";
      when(mockPgPool.query(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(0);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(false);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle execution error")
    void testExecuteError(VertxTestContext testContext) {
      // Arrange
      String query = "INVALID SQL";
      Exception error = new RuntimeException("SQL syntax error");
      when(mockPgPool.query(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute()).thenReturn(Single.error(error));

      // Act
      TestObserver<Boolean> testObserver = client.execute(query).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> e.getMessage().equals("SQL syntax error"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Execute Tests - With Connection")
  class ExecuteWithConnectionTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute query with connection")
    void testExecuteWithConnection(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ('John')";
      when(mockConnection.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(mockConnection, query).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      verify(mockConnection, times(1)).preparedQuery(query);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should execute query with connection and tuple")
    void testExecuteWithConnectionAndTuple(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1)";
      Tuple tuple = Tuple.of("John");
      when(mockConnection.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(mockConnection, query, tuple).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      verify(mockConnection, times(1)).preparedQuery(query);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Execute Tests - Prepared Query with Tuple")
  class ExecutePreparedQueryTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute prepared query with tuple")
    void testExecutePreparedQuery(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name, age) VALUES ($1, $2)";
      Tuple tuple = Tuple.of("John", 30);
      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple parameters")
    void testExecuteMultipleParameters(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name, age, email) VALUES ($1, $2, $3)";
      Tuple tuple = Tuple.of("John", 30, "john@example.com");
      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ExecuteAndGenerateId Tests")
  class ExecuteAndGenerateIdTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute and generate Integer ID")
    void testExecuteAndGenerateIntegerId(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1) RETURNING id";
      Tuple tuple = Tuple.of("John");
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(1);
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getInteger("id")).thenReturn(123);

      // Act
      TestObserver<Integer> testObserver =
          client.executeAndGenerateId(query, tuple, row -> row.getInteger("id")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(123);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should execute and generate Long ID with connection")
    void testExecuteAndGenerateLongIdWithConnection(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1) RETURNING id";
      Tuple tuple = Tuple.of("John");
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockConnection.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(1);
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getLong("id")).thenReturn(456L);

      // Act
      TestObserver<Long> testObserver =
          client
              .executeAndGenerateId(mockConnection, query, tuple, row -> row.getLong("id"))
              .test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(456L);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no rows inserted")
    void testExecuteAndGenerateIdNoRows(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1) RETURNING id";
      Tuple tuple = Tuple.of("John");

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(0);

      // Act
      TestObserver<Integer> testObserver =
          client.executeAndGenerateId(query, tuple, row -> row.getInteger("id")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(IllegalStateException.class);
      testObserver.assertError(e -> e.getMessage().equals("No rows inserted"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ExecuteMultiple Tests")
  class ExecuteMultipleTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute batch with multiple tuples")
    void testExecuteMultiple(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name, age) VALUES ($1, $2)";
      List<Tuple> tuples = new ArrayList<>();
      tuples.add(Tuple.of("John", 30));
      tuples.add(Tuple.of("Jane", 25));
      tuples.add(Tuple.of("Bob", 35));

      when(mockConnection.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecuteBatch(tuples)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(3);

      // Act
      TestObserver<Boolean> testObserver =
          client.executeMultiple(mockConnection, query, tuples).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      verify(mockPreparedQuery, times(1)).rxExecuteBatch(tuples);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when no rows affected in batch")
    void testExecuteMultipleNoRowsAffected(VertxTestContext testContext) {
      // Arrange
      String query = "UPDATE users SET active = $1 WHERE id = $2";
      List<Tuple> tuples = new ArrayList<>();
      tuples.add(Tuple.of(false, 999));
      tuples.add(Tuple.of(false, 998));

      when(mockConnection.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecuteBatch(tuples)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(0);

      // Act
      TestObserver<Boolean> testObserver =
          client.executeMultiple(mockConnection, query, tuples).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(false);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ExecuteWithTransaction Tests")
  class ExecuteWithTransactionTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute transactional function")
    void testExecuteWithTransaction(VertxTestContext testContext) {
      // Arrange
      Function<SqlConnection, Single<String>> transactionalFunction =
          conn -> Single.just("transaction result");

      when(mockPgPool.rxWithTransaction(any())).thenReturn(Maybe.just("transaction result"));

      // Act
      TestObserver<String> testObserver =
          client.executeWithTransaction(transactionalFunction).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue("transaction result");
      verify(mockPgPool, times(1)).rxWithTransaction(any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle transaction error")
    void testExecuteWithTransactionError(VertxTestContext testContext) {
      // Arrange
      Exception error = new RuntimeException("Transaction failed");
      Function<SqlConnection, Single<String>> transactionalFunction = conn -> Single.error(error);

      when(mockPgPool.rxWithTransaction(any())).thenReturn(Maybe.error(error));

      // Act
      TestObserver<String> testObserver =
          client.executeWithTransaction(transactionalFunction).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> e.getMessage().equals("Transaction failed"));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle empty transaction result")
    void testExecuteWithTransactionEmpty(VertxTestContext testContext) {
      // Arrange
      Function<SqlConnection, Single<String>> transactionalFunction = conn -> Single.never();

      when(mockPgPool.rxWithTransaction(any())).thenReturn(Maybe.empty());

      // Act
      TestObserver<String> testObserver =
          client.executeWithTransaction(transactionalFunction).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertNoValues();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("ExecuteAndFetchOne Tests")
  class ExecuteAndFetchOneTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should execute and fetch one row")
    void testExecuteAndFetchOne(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1) RETURNING id, name";
      Tuple tuple = Tuple.of("John");
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(1);
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getString("name")).thenReturn("John");

      // Act
      TestObserver<String> testObserver =
          client.executeAndFetchOne(query, tuple, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue("John");
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no rows found")
    void testExecuteAndFetchOneNoRows(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1) RETURNING id, name";
      Tuple tuple = Tuple.of("John");

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(0);

      // Act
      TestObserver<String> testObserver =
          client.executeAndFetchOne(query, tuple, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(NoSuchElementException.class);
      testObserver.assertError(e -> e.getMessage().equals("No rows found"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Retry Mechanism Tests")
  class RetryMechanismTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should retry on failure and eventually succeed")
    void testRetryOnFailure(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1)";
      Tuple tuple = Tuple.of("John");
      RuntimeException error = new RuntimeException("Temporary error");
      AtomicInteger attemptCount = new AtomicInteger(0);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      // Use Single.defer to create a new Single on each subscription (retry)
      when(mockPreparedQuery.rxExecute(tuple))
          .thenReturn(
              Single.defer(
                  () -> {
                    int attempt = attemptCount.incrementAndGet();
                    if (attempt == 1) {
                      // First attempt fails
                      return Single.error(error);
                    } else {
                      // Second attempt (first retry) succeeds
                      return Single.just(mockRowSet);
                    }
                  }));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      // Verify 2 attempts were made (initial failure + 1 successful retry)
      assertEquals(2, attemptCount.get(), "Expected 2 subscription attempts");
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should propagate error after exhausting all retries")
    void testErrorPropagationAfterRetries(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name) VALUES ($1)";
      Tuple tuple = Tuple.of("John");
      RuntimeException error = new RuntimeException("Persistent error");
      AtomicInteger attemptCount = new AtomicInteger(0);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      // Use Single.defer to always return error, tracking each attempt
      when(mockPreparedQuery.rxExecute(tuple))
          .thenReturn(
              Single.defer(
                  () -> {
                    attemptCount.incrementAndGet();
                    return Single.error(error);
                  }));

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertNotComplete();
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> "Persistent error".equals(e.getMessage()));
      // Verify 4 attempts were made (initial + 3 retries, based on retryCount=3 in config)
      assertEquals(4, attemptCount.get(), "Expected 4 attempts (initial + 3 retries)");
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @BeforeEach
    void setUpClient() {
      Vertx vertx = Vertx.vertx();
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        client = new PgWriterClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should handle complex insert with multiple fields")
    void testComplexInsert(VertxTestContext testContext) {
      // Arrange
      String query = "INSERT INTO users (name, age, email, active) VALUES ($1, $2, $3, $4)";
      Tuple tuple = Tuple.of("John", 30, "john@example.com", true);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle update operation")
    void testUpdate(VertxTestContext testContext) {
      // Arrange
      String query = "UPDATE users SET name = $1 WHERE id = $2";
      Tuple tuple = Tuple.of("Jane", 1);

      when(mockPgPool.preparedQuery(query)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.execute(query, tuple).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple operations sequentially")
    void testMultipleOperations(VertxTestContext testContext) {
      // Arrange
      String insertQuery = "INSERT INTO users (name) VALUES ($1)";
      String updateQuery = "UPDATE users SET active = $1 WHERE name = $2";
      Tuple insertTuple = Tuple.of("John");
      Tuple updateTuple = Tuple.of(true, "John");

      when(mockPgPool.preparedQuery(insertQuery)).thenReturn(mockPreparedQuery);
      when(mockPgPool.preparedQuery(updateQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(insertTuple)).thenReturn(Single.just(mockRowSet));
      when(mockPreparedQuery.rxExecute(updateTuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.rowCount()).thenReturn(1);

      // Act
      TestObserver<Boolean> insertObserver = client.execute(insertQuery, insertTuple).test();
      insertObserver.awaitDone(1, TimeUnit.SECONDS);

      TestObserver<Boolean> updateObserver = client.execute(updateQuery, updateTuple).test();
      updateObserver.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      insertObserver.assertComplete();
      insertObserver.assertValue(true);
      updateObserver.assertComplete();
      updateObserver.assertValue(true);
      testContext.completeNow();
    }
  }
}
