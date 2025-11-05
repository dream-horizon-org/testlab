package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.TagsDAOImpl;
import com.ascend.testlab.util.MaintenanceUtil;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for TagsDAO.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("TagsDAO Tests")
class TagsDAOTest {

  @Mock private PgReaderClient pgReaderClient;

  private TagsDAO tagsDAO;

  private Vertx vertx;

  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp(Vertx vertx) {
    this.vertx = vertx;
    tagsDAO = new TagsDAOImpl(pgReaderClient);
  }

  @AfterEach
  void tearDown() {
    // Clear maintenance mode after each test
    MaintenanceUtil.clearMaintenance(vertx);
  }

  @Nested
  @DisplayName("Tag Fetching Success Cases")
  class FetchTagsSuccessTests {
    @Test
    @DisplayName("Should return tags when DB returns tag list")
    void testFetchTags_Success() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class)))
          .thenReturn(Single.just(Arrays.asList("A/B-test", "feature-flag")));

      // Act
      Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);
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
          .fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class));
    }
  }

  @Nested
  @DisplayName("Tag Fetching with No Results")
  class FetchTagsEmptyResultTests {
    @Test
    @DisplayName("Should return empty list when DB returns no tags")
    void testFetchTags_EmptyResult() {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class)))
          .thenReturn(Single.just(List.of()));

      // Act
      Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      List<String> actualTags = testObserver.values().get(0);
      assertNotNull(actualTags);
      assertTrue(actualTags.isEmpty());
      verify(pgReaderClient, times(1))
          .fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class));
    }
  }

  @Nested
  @DisplayName("Tag Fetching Error Handling")
  class FetchTagsErrorTests {
    @Test
    @DisplayName("Should throw RuntimeException when DB fails")
    void testFetchTags_DatabaseError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class)))
          .thenReturn(Single.error(dbException));

      // Act
      Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);
      TestObserver<List<String>> testObserver = result.test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class));
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
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.FETCH_TAGS),
              any(io.vertx.rxjava3.sqlclient.Tuple.class),
              any(java.util.function.Function.class)))
          .thenReturn(Single.just(mockTags));

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<List<String>> testObserver = tagsDAO.fetchTags(testProjectKey).test();

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
                .fetchAll(
                    eq(ReadQuery.FETCH_TAGS),
                    any(io.vertx.rxjava3.sqlclient.Tuple.class),
                    any(java.util.function.Function.class));
            testContext.completeNow();
          });
    }
  }
}
