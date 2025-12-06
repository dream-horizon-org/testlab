package com.ascend.testlab.client.postgresql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.impl.PgReaderClientImpl;
import com.ascend.testlab.config.PostgreSQLConfig;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.PreparedQuery;
import io.vertx.rxjava3.sqlclient.Query;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowIterator;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PgReaderClient}.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("PgReaderClient Tests")
class PgReaderClientTest {

  @Mock private PgPool mockPgPool;
  @Mock private Query<RowSet<Row>> mockQuery;
  @Mock private PreparedQuery<RowSet<Row>> mockPreparedQuery;
  @Mock private RowSet<Row> mockRowSet;
  @Mock private Row mockRow;

  private PgReaderClient client;
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

    PostgreSQLConfig.BaseConfig readerConfig = new PostgreSQLConfig.BaseConfig();
    PostgreSQLConfig.ConnectOptions connectOptions = new PostgreSQLConfig.ConnectOptions();
    connectOptions.setHost("localhost");
    connectOptions.setPort(8142);
    connectOptions.setUser("test_user");
    connectOptions.setPassword("test_password");
    connectOptions.setDatabase("test_db");
    connectOptions.setConnectTimeout(5000);
    connectOptions.setCachePreparedStatements(true);

    PostgreSQLConfig.PoolOptions poolOptions = new PostgreSQLConfig.PoolOptions();
    poolOptions.setMaxSize(10);
    poolOptions.setMaxWaitQueueSize(50);

    readerConfig.setConnectOptions(connectOptions);
    readerConfig.setPoolOptions(poolOptions);
    readerConfig.setRetryCount(3);

    postgreSQLConfig.setReaderConfig(readerConfig);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create PgReaderClientImpl with valid config")
    void testConstructor(io.vertx.core.Vertx coreVertx, VertxTestContext testContext) {
      // Arrange & Act
      try (MockedStatic<PgPool> pgPoolMock = mockStatic(PgPool.class)) {
        pgPoolMock
            .when(
                () ->
                    PgPool.pool(
                        any(Vertx.class), any(PgConnectOptions.class), any(PoolOptions.class)))
            .thenReturn(mockPgPool);

        PgReaderClient client =
            new PgReaderClientImpl(Vertx.newInstance(coreVertx), postgreSQLConfig);

        // Assert
        assertNotNull(client);
        testContext.completeNow();
      }
    }

    @Test
    @DisplayName("Should extract reader config from PostgreSQLConfig")
    void testExtractReaderConfig(io.vertx.core.Vertx coreVertx, VertxTestContext testContext) {
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
                  assertEquals(8142, options.getPort());
                  assertEquals("test_user", options.getUser());
                  assertEquals("test_password", options.getPassword());
                  assertEquals("test_db", options.getDatabase());
                  return mockPgPool;
                });

        new PgReaderClientImpl(Vertx.newInstance(coreVertx), postgreSQLConfig);
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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
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
  @DisplayName("Connection Check Tests")
  class ConnectionCheckTests {

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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should return true when connected")
    void testIsConnectedSuccess(VertxTestContext testContext) {
      // Arrange
      when(mockPgPool.query(ReadQuery.HEALTH_CHECK)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(1);

      // Act
      TestObserver<Boolean> testObserver = client.isConnected().test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(true);
      verify(mockPgPool, times(1)).query(ReadQuery.HEALTH_CHECK);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return false when health check returns no rows")
    void testIsConnectedNoRows(VertxTestContext testContext) {
      // Arrange
      when(mockPgPool.query(ReadQuery.HEALTH_CHECK)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(0);

      // Act
      TestObserver<Boolean> testObserver = client.isConnected().test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(false);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle connection error")
    void testIsConnectedError(VertxTestContext testContext) {
      // Arrange
      Exception error = new RuntimeException("Connection error");
      when(mockPgPool.query(ReadQuery.HEALTH_CHECK)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.error(error));

      // Act
      TestObserver<Boolean> testObserver = client.isConnected().test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> e.getMessage().equals("Connection error"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchAll Tests")
  class FetchAllTests {

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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should fetch all rows with simple query")
    void testFetchAllSimpleQuery(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator); // Now use the pre-configured iterator
      when(mockRow.getString("name")).thenReturn("John Doe");

      // Act
      TestObserver<List<String>> testObserver =
          client.fetchAll(query, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(names -> names.size() == 1 && "John Doe".equals(names.get(0)));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should fetch all rows with prepared query")
    void testFetchAllPreparedQuery(VertxTestContext testContext) {
      // Arrange
      String preparedQuery = "SELECT * FROM users WHERE id = $1";
      Tuple tuple = Tuple.of(1);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.preparedQuery(preparedQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getInteger("id")).thenReturn(1);

      // Act
      TestObserver<List<Integer>> testObserver =
          client.fetchAll(preparedQuery, tuple, row -> row.getInteger("id")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(ids -> ids.size() == 1 && Integer.valueOf(1).equals(ids.get(0)));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty list when no rows found")
    void testFetchAllNoRows(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(new ArrayList<>());

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);

      // Act
      TestObserver<List<String>> testObserver =
          client.fetchAll(query, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(List::isEmpty);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should fetch multiple rows")
    void testFetchAllMultipleRows(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";
      Row mockRow1 = mock(Row.class);
      Row mockRow2 = mock(Row.class);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow1);
      rows.add(mockRow2);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow1.getString("name")).thenReturn("John");
      when(mockRow2.getString("name")).thenReturn("Jane");

      // Act
      TestObserver<List<String>> testObserver =
          client.fetchAll(query, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(
          names -> names.size() == 2 && "John".equals(names.get(0)) && "Jane".equals(names.get(1)));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchOne Tests")
  class FetchOneTests {

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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should fetch one row")
    void testFetchOne(VertxTestContext testContext) {
      // Arrange
      String preparedQuery = "SELECT * FROM users WHERE id = $1";
      Tuple tuple = Tuple.of(1);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.preparedQuery(preparedQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(1);
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getString("name")).thenReturn("John Doe");

      // Act
      TestObserver<String> testObserver =
          client.fetchOne(preparedQuery, tuple, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue("John Doe");
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty when no rows found")
    void testFetchOneNoRows(VertxTestContext testContext) {
      // Arrange
      String preparedQuery = "SELECT * FROM users WHERE id = $1";
      Tuple tuple = Tuple.of(999);

      when(mockPgPool.preparedQuery(preparedQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.size()).thenReturn(0);

      // Act
      TestObserver<String> testObserver =
          client.fetchOne(preparedQuery, tuple, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertResult();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle query error")
    void testFetchOneError(VertxTestContext testContext) {
      // Arrange
      String preparedQuery = "SELECT * FROM users WHERE id = $1";
      Tuple tuple = Tuple.of(1);
      Exception error = new RuntimeException("Query error");

      when(mockPgPool.preparedQuery(preparedQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.error(error));

      // Act
      TestObserver<String> testObserver =
          client.fetchOne(preparedQuery, tuple, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);
      testObserver.assertError(e -> e.getMessage().equals("Query error"));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("FetchMap Tests")
  class FetchMapTests {

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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should fetch map with simple query")
    void testFetchMapSimpleQuery(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT id, name FROM users";
      Row mockRow1 = mock(Row.class);
      Row mockRow2 = mock(Row.class);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow1);
      rows.add(mockRow2);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow1.getInteger("id")).thenReturn(1);
      when(mockRow1.getString("name")).thenReturn("John");
      when(mockRow2.getInteger("id")).thenReturn(2);
      when(mockRow2.getString("name")).thenReturn("Jane");

      // Act
      TestObserver<Map<Integer, String>> testObserver =
          client.fetchMap(query, row -> row.getInteger("id"), row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(
          map -> map.size() == 2 && map.get(1).equals("John") && map.get(2).equals("Jane"));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should fetch map with prepared query")
    void testFetchMapPreparedQuery(VertxTestContext testContext) {
      // Arrange
      String preparedQuery = "SELECT id, name FROM users WHERE active = $1";
      Tuple tuple = Tuple.of(true);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.preparedQuery(preparedQuery)).thenReturn(mockPreparedQuery);
      when(mockPreparedQuery.rxExecute(tuple)).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow.getInteger("id")).thenReturn(1);
      when(mockRow.getString("name")).thenReturn("John");

      // Act
      TestObserver<Map<Integer, String>> testObserver =
          client
              .fetchMap(
                  preparedQuery, tuple, row -> row.getInteger("id"), row -> row.getString("name"))
              .test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(map -> map.size() == 1 && map.get(1).equals("John"));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty map when no rows found")
    void testFetchMapNoRows(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT id, name FROM users";

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(new ArrayList<>());

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);

      // Act
      TestObserver<Map<Integer, String>> testObserver =
          client.fetchMap(query, row -> row.getInteger("id"), row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(Map::isEmpty);
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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should retry on failure and eventually succeed")
    void testRetryOnFailure(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";
      RuntimeException error = new RuntimeException("Temporary error");
      AtomicInteger attemptCount = new AtomicInteger(0);
      RowIterator<Row> rowIterator = createRowIterator(new ArrayList<>());

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      // Use Single.defer to create a new Single on each subscription (retry)
      when(mockQuery.rxExecute())
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
      when(mockRowSet.iterator()).thenReturn(rowIterator);

      // Act
      TestObserver<List<String>> testObserver =
          client.fetchAll(query, row -> row.getString("name")).test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      // Verify 2 attempts were made (initial failure + 1 successful retry)
      assertEquals(2, attemptCount.get(), "Expected 2 subscription attempts");
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should propagate error after exhausting all retries")
    void testErrorPropagationAfterRetries(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";
      RuntimeException error = new RuntimeException("Persistent error");
      AtomicInteger attemptCount = new AtomicInteger(0);

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      // Use Single.defer to always return error, tracking each attempt
      when(mockQuery.rxExecute())
          .thenReturn(
              Single.defer(
                  () -> {
                    attemptCount.incrementAndGet();
                    return Single.error(error);
                  }));

      // Act
      TestObserver<List<String>> testObserver =
          client.fetchAll(query, row -> row.getString("name")).test();

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

        client = new PgReaderClientImpl(vertx, postgreSQLConfig);
      }
    }

    @Test
    @DisplayName("Should handle complex data mapping")
    void testComplexDataMapping(VertxTestContext testContext) {
      // Arrange
      String query = "SELECT * FROM users";
      Row mockRow1 = mock(Row.class);
      List<Row> rows = new ArrayList<>();
      rows.add(mockRow1);

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(rows);

      when(mockPgPool.query(query)).thenReturn(mockQuery);
      when(mockQuery.rxExecute()).thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);
      when(mockRow1.getInteger("id")).thenReturn(1);
      when(mockRow1.getString("name")).thenReturn("John");
      when(mockRow1.getInteger("age")).thenReturn(30);

      // Act
      TestObserver<List<String>> testObserver =
          client
              .fetchAll(
                  query,
                  row ->
                      String.format(
                          "User{id=%d, name=%s, age=%d}",
                          row.getInteger("id"), row.getString("name"), row.getInteger("age")))
              .test();

      // Assert
      testObserver.awaitDone(1, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertValue(
          users -> users.size() == 1 && "User{id=1, name=John, age=30}".equals(users.get(0)));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple operations sequentially")
    void testMultipleOperations(VertxTestContext testContext) {
      // Arrange
      String query1 = "SELECT COUNT(*) FROM users";
      String query2 = "SELECT * FROM users";

      // IMPORTANT: Create the iterator FIRST, before using it in when().thenReturn()
      RowIterator<Row> rowIterator = createRowIterator(new ArrayList<>());

      when(mockPgPool.query(query1)).thenReturn(mockQuery);
      when(mockPgPool.query(query2)).thenReturn(mockQuery);
      when(mockQuery.rxExecute())
          .thenReturn(Single.just(mockRowSet))
          .thenReturn(Single.just(mockRowSet));
      when(mockRowSet.iterator()).thenReturn(rowIterator);

      // Act
      TestObserver<List<String>> testObserver1 =
          client.fetchAll(query1, row -> row.getString("count")).test();
      testObserver1.awaitDone(1, TimeUnit.SECONDS);

      TestObserver<List<String>> testObserver2 =
          client.fetchAll(query2, row -> row.getString("name")).test();
      testObserver2.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testContext.completeNow();
    }
  }
}
