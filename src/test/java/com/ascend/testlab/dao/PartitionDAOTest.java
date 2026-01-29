package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.PartitionStatus;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.impl.PartitionDAOImpl;
import com.ascend.testlab.dto.response.PartitionResponse;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.sqlclient.PreparedQuery;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowIterator;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for PartitionDAO.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("PartitionDAO Tests")
public class PartitionDAOTest {

  @Mock private PgWriterClient pgWriterClient;
  @Mock private SqlConnection sqlConnection;
  @Mock private PreparedQuery<RowSet<Row>> preparedQuery;
  @Mock private RowSet<Row> rowSet;
  @Mock private Row row;

  private PartitionDAO partitionDAO;

  private static final String PROJECT_KEY = "test-project-key";
  private static final String USER_ID = "test-user";

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
    partitionDAO = new PartitionDAOImpl(pgWriterClient);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create DAO with valid dependencies")
    void testConstructorWithValidDependencies() {
      // Act
      PartitionDAO dao = new PartitionDAOImpl(pgWriterClient);

      // Assert
      assertNotNull(dao);
    }
  }

  @Nested
  @DisplayName("Create Project Partition Tests")
  class CreateProjectPartitionTests {

    @Test
    @DisplayName("Should return success response when partition is created successfully")
    void testCreateProjectPartition_Success() {
      // Arrange
      mockUpsertReturningStatus(PartitionStatus.CREATING);
      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString(), any(Tuple.class));
      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString());

      // Mock: Transaction execution - return success response
      mockExecuteWithTransaction();

      // Act
      Single<PartitionResponse> result = partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      PartitionResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(PROJECT_KEY, response.getProjectKey());
      assertEquals(PartitionStatus.SUCCESS.name(), response.getStatus());
      assertEquals("Partitions created successfully", response.getMessage());
    }

    @Test
    @DisplayName("Should return idempotent response when partition already exists")
    void testCreateProjectPartition_Idempotent() {
      // Arrange
      mockUpsertReturningStatus(PartitionStatus.SUCCESS);

      mockExecuteWithTransaction();

      // Act
      Single<PartitionResponse> result = partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      PartitionResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(PROJECT_KEY, response.getProjectKey());
      assertEquals(PartitionStatus.SUCCESS.name(), response.getStatus());
      assertEquals("Partitions already exist", response.getMessage());
    }

    @Test
    @DisplayName("Should retry creation when metadata status is not SUCCESS")
    void testCreateProjectPartition_RetryCreation() {
      // Arrange
      mockUpsertReturningStatus(PartitionStatus.CREATING);

      mockExecuteWithTransaction();

      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString(), any(Tuple.class));

      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString());

      // Act
      Single<PartitionResponse> result = partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      PartitionResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(PROJECT_KEY, response.getProjectKey());
      assertEquals(PartitionStatus.SUCCESS.name(), response.getStatus());
      assertEquals("Partitions created successfully", response.getMessage());
    }

    @Test
    @DisplayName("Should throw RuntimeException when metadata upsert fails")
    void testCreateProjectPartition_MetadataUpsertError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(sqlConnection.preparedQuery(WriteQuery.UPSERT_PARTITION_METADATA_RETURNING_STATUS))
          .thenReturn(preparedQuery);
      when(preparedQuery.rxExecute(any(Tuple.class))).thenReturn(Single.error(dbException));

      // Mock: Transaction execution
      mockExecuteWithTransaction();

      // Act
      Single<PartitionResponse> result = partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw RuntimeException when partition creation fails")
    void testCreateProjectPartition_PartitionCreationError() {
      // Arrange
      mockUpsertReturningStatus(PartitionStatus.CREATING);

      // Mock: Transaction execution
      mockExecuteWithTransaction();

      // Mock: Partition creation fails
      RuntimeException dbException = new RuntimeException("Partition creation failed");
      doReturn(Single.error(dbException))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString());

      // Act
      Single<PartitionResponse> result = partitionDAO.createProjectPartition(PROJECT_KEY, USER_ID);
      TestObserver<PartitionResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle different project keys correctly")
    void testCreateProjectPartition_DifferentProjectKeys() {
      // Arrange
      String projectKey1 = "project-key-1";
      String projectKey2 = "project-key-2";

      // Mock: Transaction execution
      mockExecuteWithTransaction();

      // Mock: All operations succeed
      mockUpsertReturningStatus(PartitionStatus.CREATING);
      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString(), any(Tuple.class));
      doReturn(Single.just(true))
          .when(pgWriterClient)
          .execute(any(SqlConnection.class), anyString());

      // Act
      TestObserver<PartitionResponse> testObserver1 =
          partitionDAO.createProjectPartition(projectKey1, USER_ID).test();
      TestObserver<PartitionResponse> testObserver2 =
          partitionDAO.createProjectPartition(projectKey2, USER_ID).test();

      // Assert
      testObserver1.assertComplete();
      testObserver1.assertNoErrors();
      assertEquals(projectKey1, testObserver1.values().get(0).getProjectKey());

      testObserver2.assertComplete();
      testObserver2.assertNoErrors();
      assertEquals(projectKey2, testObserver2.values().get(0).getProjectKey());
    }
  }

  @SuppressWarnings("unchecked")
  private void mockExecuteWithTransaction() {
    doAnswer(
            invocation -> {
              Function<SqlConnection, Maybe<PartitionResponse>> transactionFunction =
                  invocation.getArgument(0);
              PartitionResponse defaultValue = invocation.getArgument(1);
              try {
                return transactionFunction.apply(sqlConnection).defaultIfEmpty(defaultValue);
              } catch (Exception e) {
                return Single.error(e);
              }
            })
        .when(pgWriterClient)
        .executeWithTransaction(
            ArgumentMatchers.<Function<SqlConnection, Maybe<PartitionResponse>>>any(),
            any(PartitionResponse.class));
  }

  private void mockUpsertReturningStatus(PartitionStatus status) {
    // Create row iterator FIRST before setting up when().thenReturn()
    List<Row> rows = new ArrayList<>();
    rows.add(row);
    RowIterator<Row> rowIterator = createRowIterator(rows);

    when(sqlConnection.preparedQuery(WriteQuery.UPSERT_PARTITION_METADATA_RETURNING_STATUS))
        .thenReturn(preparedQuery);
    when(preparedQuery.rxExecute(any(Tuple.class))).thenReturn(Single.just(rowSet));
    when(rowSet.size()).thenReturn(1);
    when(rowSet.iterator()).thenReturn(rowIterator);
    when(row.getString(Columns.STATUS)).thenReturn(status.name());
  }
}
