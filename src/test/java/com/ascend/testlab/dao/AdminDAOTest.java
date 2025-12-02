package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.AdminDAOImpl;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for AdminDAO.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("AdminDAO Tests")
class AdminDAOTest {

  @Mock private PgReaderClient pgReaderClient;
  @Mock private AerospikeClient aerospikeClient;
  @Mock private AerospikeConfig aerospikeConfig;

  private AdminDAO adminDAO;

  private static final String PROJECT_KEY = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_KEY = "test_experiment_key";
  private static final String EXPERIMENT_ID = "123e4567-e89b-12d3-a456-426614174001";

  @BeforeEach
  void setUp() {
    adminDAO = new AdminDAOImpl(pgReaderClient, aerospikeClient, aerospikeConfig);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create DAO with valid dependencies")
    void testConstructorWithValidDependencies() {
      // Act
      AdminDAO dao = new AdminDAOImpl(pgReaderClient, aerospikeClient, aerospikeConfig);

      // Assert
      assertNotNull(dao);
    }
  }

  @Nested
  @DisplayName("Tag Fetching Tests")
  class FetchTagsTests {
    @Test
    @DisplayName("Should return tags when DB returns tag list")
    void testFetchTags_Success() {
      // Arrange
      when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(Arrays.asList("A/B-test", "feature-flag")));

      // Act
      Single<List<String>> result = adminDAO.fetchTags(PROJECT_KEY);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      List<String> actualTags = testObserver.values().get(0);
      assertNotNull(actualTags);
      assertEquals(2, actualTags.size());
      assertTrue(actualTags.contains("A/B-test"));
      assertTrue(actualTags.contains("feature-flag"));
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return empty list when DB returns no tags")
    void testFetchTags_EmptyResult() {
      // Arrange
      when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      Single<List<String>> result = adminDAO.fetchTags(PROJECT_KEY);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      List<String> actualTags = testObserver.values().get(0);
      assertNotNull(actualTags);
      assertTrue(actualTags.isEmpty());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testFetchTags_DatabaseError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      Single<List<String>> result = adminDAO.fetchTags(PROJECT_KEY);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class));
    }
  }

  @Nested
  @DisplayName("Key Availability Check Tests")
  class ExperimentKeyAvailabilityTests {
    @Test
    @DisplayName("Should return true when key is available (EXISTS returns false)")
    void testIsExperimentKeyAvailable_Available() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of(false)));

      // Act
      Single<Boolean> result = adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertTrue(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return false when key is not available (EXISTS returns true)")
    void testIsExperimentKeyAvailable_NotAvailable() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of(true)));

      // Act
      Single<Boolean> result = adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertFalse(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return true when list is empty (safe default)")
    void testIsExperimentKeyAvailable_EmptyList() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      Single<Boolean> result = adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertTrue(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testIsExperimentKeyAvailable_DatabaseError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      Single<Boolean> result = adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_KEY), any(Tuple.class), any(Function.class));
    }
  }

  @Nested
  @DisplayName("Experiment History Fetching Tests")
  class FetchExperimentHistoryTests {
    @Test
    @DisplayName("Should return experiment history when DB returns history entries")
    void testFetchExperimentHistory_Success() {
      // Arrange
      int limit = 20;
      int page = 1;
      int totalCount = 2;
      List<Row> mockRows = createMockHistoryRows(totalCount);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockRows));

      // Act
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      Single<ExperimentHistoryResponse> result = adminDAO.fetchExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentHistoryResponse actualResult = testObserver.values().get(0);
      assertNotNull(actualResult);
      assertEquals(EXPERIMENT_ID, actualResult.experimentId());
      assertEquals(2, actualResult.history().size());
      assertEquals(totalCount, actualResult.pagination().totalCount());
      assertEquals(page, actualResult.pagination().currentPage());
      assertEquals(actualResult.history().size(), actualResult.pagination().pageSize());
      assertEquals("user1", actualResult.history().get(0).updatedBy());
      assertEquals("user2", actualResult.history().get(1).updatedBy());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return paginated results with correct limit and page")
    void testFetchExperimentHistory_Pagination() {
      // Arrange
      int limit = 10;
      int page = 2;
      int totalCount = 25;
      List<Row> mockRows = createMockHistoryRows(10, totalCount);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockRows));

      // Act
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      Single<ExperimentHistoryResponse> result = adminDAO.fetchExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      ExperimentHistoryResponse actualResult = testObserver.values().get(0);
      assertNotNull(actualResult);
      assertEquals(10, actualResult.history().size());
      assertEquals(totalCount, actualResult.pagination().totalCount());
      assertEquals(page, actualResult.pagination().currentPage());
      assertEquals(actualResult.history().size(), actualResult.pagination().pageSize());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return last page with fewer rows than limit")
    void testFetchExperimentHistory_LastPage_FewerRows() {
      // Arrange
      int limit = 10;
      int page = 3;
      int totalCount = 23;
      int actualRowsReturned = 3;
      List<Row> mockRows = createMockHistoryRows(actualRowsReturned, totalCount);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockRows));

      // Act
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      Single<ExperimentHistoryResponse> result = adminDAO.fetchExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentHistoryResponse actualResult = testObserver.values().get(0);
      assertNotNull(actualResult);
      assertEquals(actualRowsReturned, actualResult.history().size());
      assertEquals(totalCount, actualResult.pagination().totalCount());
      assertEquals(page, actualResult.pagination().currentPage());
      assertEquals(actualRowsReturned, actualResult.pagination().pageSize());
      assertNotEquals(
          limit,
          actualResult.pagination().pageSize(),
          "pageSize should be actual rows returned, not the requested limit");
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return empty list when DB returns no history")
    void testFetchExperimentHistory_EmptyResult() {
      // Arrange
      int limit = 20;
      int page = 1;
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      Single<ExperimentHistoryResponse> result = adminDAO.fetchExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentHistoryResponse actualResult = testObserver.values().get(0);
      assertNotNull(actualResult);
      assertTrue(actualResult.history().isEmpty());
      assertEquals(0, actualResult.pagination().totalCount());
      assertEquals(page, actualResult.pagination().currentPage());
      assertEquals(actualResult.history().size(), actualResult.pagination().pageSize());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testFetchExperimentHistory_DatabaseError() {
      // Arrange
      int limit = 20;
      int page = 1;
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      Single<ExperimentHistoryResponse> result = adminDAO.fetchExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should fetch experiment history asynchronously using Vert.x event loop context")
    void testFetchExperimentHistory_Success_Async(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      int limit = 20;
      int page = 1;
      int totalCount = 1;
      List<Row> mockRows = createMockHistoryRows(totalCount);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockRows));

      // Act
      vertx.runOnContext(
          v -> {
            ExperimentHistoryRequest request =
                ExperimentHistoryRequest.builder()
                    .projectKey(PROJECT_KEY)
                    .experimentId(EXPERIMENT_ID)
                    .limit(limit)
                    .page(page)
                    .build();
            TestObserver<ExperimentHistoryResponse> testObserver =
                adminDAO.fetchExperimentHistory(request).test();

            // Assert
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValueCount(1);
            ExperimentHistoryResponse actualResult = testObserver.values().get(0);
            assertNotNull(actualResult);
            assertEquals(1, actualResult.history().size());
            assertEquals("user1", actualResult.history().get(0).updatedBy());
            verify(pgReaderClient, times(1))
                .fetchAll(
                    eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
            testContext.completeNow();
          });
    }

    /**
     * Helper method to create mock Row objects for experiment history testing.
     *
     * @param count the number of rows to create
     * @param totalCount the total count value for pagination
     * @return a list of mock Row objects
     */
    private List<Row> createMockHistoryRows(int count, int totalCount) {
      List<Row> rows = new ArrayList<>();
      LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

      for (int i = 0; i < count; i++) {
        Row mockRow = mock(Row.class);
        when(mockRow.getString(Columns.UPDATED_BY)).thenReturn("user" + (i + 1));
        when(mockRow.getJsonObject(Columns.PREVIOUS_DATA))
            .thenReturn(new JsonObject().put("status", "DRAFT"));
        when(mockRow.getJsonObject(Columns.CURRENT_DATA))
            .thenReturn(new JsonObject().put("status", "LIVE"));
        when(mockRow.getOffsetDateTime(Columns.CREATED_AT))
            .thenReturn(now.minusHours(i + 1).atZone(ZoneOffset.UTC).toOffsetDateTime());
        when(mockRow.getOffsetDateTime(Columns.UPDATED_AT))
            .thenReturn(now.minusHours(i + 1).atZone(ZoneOffset.UTC).toOffsetDateTime());
        if (i == 0) {
          when(mockRow.getInteger(Columns.TOTAL_COUNT)).thenReturn(totalCount);
        }
        rows.add(mockRow);
      }

      return rows;
    }

    /**
     * Helper method to create mock Row objects for experiment history testing with default total
     * count.
     *
     * @param count the number of rows to create
     * @return a list of mock Row objects
     */
    private List<Row> createMockHistoryRows(int count) {
      return createMockHistoryRows(count, count);
    }
  }

  @Nested
  @DisplayName("Experiment History Count Tests")
  class ExperimentHistoryCountTests {
    @Test
    @DisplayName("Should return count when DB returns count value")
    void testGetExperimentHistoryCount_Success() {
      // Arrange
      int expectedCount = 42;
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any()))
          .thenReturn(Maybe.just(expectedCount));

      // Act
      Single<Integer> result = adminDAO.getExperimentHistoryCount(PROJECT_KEY, EXPERIMENT_ID);
      TestObserver<Integer> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(expectedCount);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any());
    }

    @Test
    @DisplayName("Should complete with no value when DB returns empty result")
    void testGetExperimentHistoryCount_Empty() {
      // Arrange
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any()))
          .thenReturn(Maybe.empty());

      // Act
      Single<Integer> result = adminDAO.getExperimentHistoryCount(PROJECT_KEY, EXPERIMENT_ID);
      TestObserver<Integer> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any());
    }

    @Test
    @DisplayName("Should emit error when DB fails")
    void testGetExperimentHistoryCount_Error() {
      // Arrange
      RuntimeException dbException = new RuntimeException("DB error");
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any()))
          .thenReturn(Maybe.error(dbException));

      // Act
      Single<Integer> result = adminDAO.getExperimentHistoryCount(PROJECT_KEY, EXPERIMENT_ID);
      TestObserver<Integer> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT_HISTORY_COUNT), any(Tuple.class), any());
    }
  }
}
