package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.NameAvailabilityDAOImpl;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Tuple;
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
 * Comprehensive unit tests for NameAvailabilityDAO.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("NameAvailabilityDAO Tests")
class NameAvailabilityDAOTest {

  @Mock private PgReaderClient pgReaderClient;

  private NameAvailabilityDAO nameAvailabilityDAO;

  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";
  private final String testExperimentName = "test-experiment";

  @BeforeEach
  void setUp(Vertx vertx) {
    nameAvailabilityDAO = new NameAvailabilityDAOImpl(pgReaderClient);
  }

  @Nested
  @DisplayName("Name Availability Check Success Cases")
  class NameAvailabilitySuccessTests {
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
          nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
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
          nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
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
  }

  @Nested
  @DisplayName("Name Availability Check Edge Cases")
  class NameAvailabilityEdgeCaseTests {
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
          nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
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
  }

  @Nested
  @DisplayName("Name Availability Check Error Handling")
  class NameAvailabilityErrorTests {
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
          nameAvailabilityDAO.isExperimentNameAvailable(testProjectKey, testExperimentName);
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
    @DisplayName("Should check name availability asynchronously using Vert.x event loop context")
    void testIsExperimentNameAvailable_Success_Async(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchAll(
              eq(ReadQuery.CHECK_EXPERIMENT_NAME), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.just(Arrays.asList(false)));

      // Act
      vertx.runOnContext(
          v -> {
            TestObserver<Boolean> testObserver =
                nameAvailabilityDAO
                    .isExperimentNameAvailable(testProjectKey, testExperimentName)
                    .test();

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
}
