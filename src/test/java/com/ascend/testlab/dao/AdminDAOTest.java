package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.AdminDAOImpl;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.Arrays;
import java.util.Collections;
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

  private AdminDAO adminDAO;

  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";
  private final String testExperimentName = "test-experiment";
  private final String testExperimentId = "123e4567-e89b-12d3-a456-426614174001";

  @BeforeEach
  void setUp(Vertx vertx) {
    adminDAO = new AdminDAOImpl(pgReaderClient);
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
      Single<List<String>> result = adminDAO.fetchTags(testProjectKey);
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
      Single<List<String>> result = adminDAO.fetchTags(testProjectKey);
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
      Single<List<String>> result = adminDAO.fetchTags(testProjectKey);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class));
    }
  }

  @Nested
  @DisplayName("Name Availability Check Tests")
  class NameAvailabilityTests {
    @Test
    @DisplayName("Should return true when name is available (EXISTS returns false)")
    void testIsExperimentNameAvailable_Available() {
      // Arrange
      // EXISTS returns false when name doesn't exist, so list contains [false]
      // After negation: !false = true (name is available)
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(Arrays.asList(false)));

      // Act
      Single<Boolean> result =
          adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertTrue(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return false when name is not available (EXISTS returns true)")
    void testIsExperimentNameAvailable_NotAvailable() {
      // Arrange
      // EXISTS returns true when name exists, so list contains [true]
      // After negation: !true = false (name is not available)
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(Arrays.asList(true)));

      // Act
      Single<Boolean> result =
          adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertFalse(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return true when list is empty (safe default)")
    void testIsExperimentNameAvailable_EmptyList() {
      // Arrange
      // Empty list should return true (name available) as safe default
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      Single<Boolean> result =
          adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      Boolean isAvailable = testObserver.values().get(0);
      assertNotNull(isAvailable);
      assertTrue(isAvailable);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testIsExperimentNameAvailable_DatabaseError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      Single<Boolean> result =
          adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<Boolean> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class));
    }
  }

  @Nested
  @DisplayName("Async Vert.x Style Demo")
  class VertxAsyncDemoTests {
    @Test
    @DisplayName("Should fetch tags asynchronously using Vert.x event loop context")
    void testFetchTags_Success_Async(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      List<String> mockTags = Arrays.asList("A/B-test", "feature-flag");
      when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockTags));

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<List<String>> testObserver = adminDAO.fetchTags(testProjectKey).test();

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
            testContext.completeNow();
          });
    }

    @Test
    @DisplayName("Should check name availability asynchronously using Vert.x event loop context")
    void testIsExperimentNameAvailable_Success_Async(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of(false)));

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> testObserver =
                adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName).test();

            // Assert
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValueCount(1);
            Boolean isAvailable = testObserver.values().get(0);
            assertNotNull(isAvailable);
            assertTrue(isAvailable);
            verify(pgReaderClient, times(1))
                .fetchAll(
                    eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class));
            testContext.completeNow();
          });
    }
  }

  @Nested
  @DisplayName("Experiment History Fetching Tests")
  class FetchExperimentHistoryTests {
    @Test
    @DisplayName("Should return experiment history when DB returns history entries")
    void testFetchExperimentHistory_Success() {
      // Arrange
      ExperimentHistoryEntry entry1 =
          ExperimentHistoryEntry.builder()
              .updatedBy("user1")
              .previousData("{\"status\":\"DRAFT\"}")
              .currentData("{\"status\":\"LIVE\"}")
              .createdAt(System.currentTimeMillis())
              .updatedAt(System.currentTimeMillis())
              .build();
      ExperimentHistoryEntry entry2 =
          ExperimentHistoryEntry.builder()
              .updatedBy("user2")
              .previousData("{\"status\":\"LIVE\"}")
              .currentData("{\"status\":\"PAUSED\"}")
              .createdAt(System.currentTimeMillis())
              .updatedAt(System.currentTimeMillis())
              .build();
      List<ExperimentHistoryEntry> mockHistory = Arrays.asList(entry1, entry2);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockHistory));

      // Act
      Single<List<ExperimentHistoryEntry>> result =
          adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId);
      TestObserver<List<ExperimentHistoryEntry>> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      List<ExperimentHistoryEntry> actualHistory = testObserver.values().get(0);
      assertNotNull(actualHistory);
      assertEquals(2, actualHistory.size());
      assertEquals("user1", actualHistory.get(0).updatedBy());
      assertEquals("user2", actualHistory.get(1).updatedBy());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should return empty list when DB returns no history")
    void testFetchExperimentHistory_EmptyResult() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      Single<List<ExperimentHistoryEntry>> result =
          adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId);
      TestObserver<List<ExperimentHistoryEntry>> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      List<ExperimentHistoryEntry> actualHistory = testObserver.values().get(0);
      assertNotNull(actualHistory);
      assertTrue(actualHistory.isEmpty());
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testFetchExperimentHistory_DatabaseError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      Single<List<ExperimentHistoryEntry>> result =
          adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId);
      TestObserver<List<ExperimentHistoryEntry>> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
    }

    @Test
    @DisplayName("Should fetch experiment history asynchronously using Vert.x event loop context")
    void testFetchExperimentHistory_Success_Async(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      ExperimentHistoryEntry entry =
          ExperimentHistoryEntry.builder()
              .updatedBy("user1")
              .previousData("{\"status\":\"DRAFT\"}")
              .currentData("{\"status\":\"LIVE\"}")
              .createdAt(System.currentTimeMillis())
              .updatedAt(System.currentTimeMillis())
              .build();
      List<ExperimentHistoryEntry> mockHistory = Collections.singletonList(entry);
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(mockHistory));

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<List<ExperimentHistoryEntry>> testObserver =
                adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId).test();

            // Assert
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValueCount(1);
            List<ExperimentHistoryEntry> actualHistory = testObserver.values().get(0);
            assertNotNull(actualHistory);
            assertEquals(1, actualHistory.size());
            assertEquals("user1", actualHistory.get(0).updatedBy());
            verify(pgReaderClient, times(1))
                .fetchAll(
                    eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(Tuple.class), any(Function.class));
            testContext.completeNow();
          });
    }
  }
}
