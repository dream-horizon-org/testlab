package com.ascend.testlab.dao;

import static com.ascend.testlab.constants.postgresql.Columns.PROJECT_KEY;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.impl.ExperimentDAOImpl;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for ExperimentDAO.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("ExperimentDAO Tests")
public class ExperimentDAOTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Mock private PgWriterClient pgWriterClient;
  @Mock private PgReaderClient pgReaderClient;

  private ExperimentDAO experimentDAO;

  private String testProjectKey;
  private UUID testExperimentId;

  @BeforeEach
  void setUp() {
    experimentDAO =
        new ExperimentDAOImpl(
            pgReaderClient, pgWriterClient, new com.fasterxml.jackson.databind.ObjectMapper());
    testProjectKey = UUID.randomUUID().toString();
    testExperimentId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("Create Experiment Tests")
  class CreateExperimentTests {

    @Test
    @DisplayName("Should successfully create experiment with all fields")
    void testCreateExperimentSuccess() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(true));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();
      ExperimentDAO dao = new ExperimentDAOImpl(pgReaderClient, pgWriterClient, objectMapper);

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(request.getExperimentId().toString());
    }

    @Test
    @DisplayName("Should create experiment with minimal required fields")
    void testCreateExperimentMinimalFields() {
      // Arrange
      Experiment request = new Experiment();
      request.setProjectKey(testProjectKey.toString());
      request.setExperimentId(testExperimentId);
      request.setName("minimal_experiment");
      request.setDescription("Minimal description");
      request.setHypothesis("Minimal hypothesis");
      request.setExposure(50);
      request.setThreshold(1000);
      request.setStartTime(System.currentTimeMillis() / 1000);
      request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
      request.setCreatedBy("test@example.com");

      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(true));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(testExperimentId.toString());
    }

    @Test
    @DisplayName("Should handle database insert failure")
    void testCreateExperimentInsertFailure() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Single<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(false));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertNotComplete();
      testObserver.assertError(err -> err instanceof RestException);
    }

    @Test
    @DisplayName("Should handle database error during insert")
    void testCreateExperimentDatabaseError() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Single<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.error(exception));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertNotComplete();
      testObserver.assertError(
          err ->
              err instanceof RuntimeException
                  && err.getMessage().equals("Database connection failed"));
    }

    @Test
    @DisplayName("Should handle experiment with cohorts array")
    void testCreateExperimentWithCohorts() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();
      request.setCohorts(Arrays.asList("premium_users", "mobile_users", "web_users"));
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(true));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(testExperimentId.toString());
    }

    @Test
    @DisplayName("Should handle experiment with JSONB fields")
    void testCreateExperimentWithJsonbFields() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();

      // Note: variantWeights is now VariantWeights type, not Map
      // Skipping variant weights for this test

      request.setOverrides(List.of("user1@example.com", "user2@example.com"));

      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(true));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(testExperimentId.toString());
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void testCreateExperimentWithNullFields() {
      // Arrange
      Experiment request = new Experiment();
      request.setProjectKey(testProjectKey.toString());
      request.setExperimentId(testExperimentId);
      request.setName("test_experiment");
      request.setDescription("Test description");
      request.setHypothesis("Test hypothesis");
      request.setStatus(null);
      request.setType(null);
      request.setGuardrailHealthStatus(null);
      request.setCohorts(null);
      request.setVariantWeights(null);
      request.setExposure(50);
      request.setThreshold(1000);
      request.setStartTime(System.currentTimeMillis() / 1000);
      request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
      request.setCreatedBy("test@example.com");

      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenReturn(Single.just(true));
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(testExperimentId.toString());
    }
  }

  @Nested
  @DisplayName("Update Experiment Tests")
  class UpdateExperimentTests {

    @Test
    @DisplayName("Should successfully update single field")
    void testUpdateSingleField() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(pgWriterClient, times(1)).execute(anyString(), any(Tuple.class));
    }

    @Test
    @DisplayName("Should successfully update multiple fields")
    void testUpdateMultipleFields() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      updates.put("status", "LIVE");
      updates.put("exposure", 75);
      updates.put("threshold", 15000L);
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should update enum fields correctly")
    void testUpdateEnumFields() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("status", "LIVE");
      updates.put("type", "A_B");
      updates.put("guardrail_health_status", "PASSING");
      updates.put("distribution_strategy", "RANDOM");
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should update cohorts array")
    void testUpdateCohortsArray() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("cohorts", Arrays.asList("premium_users", "mobile_users"));
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should update JSONB fields")
    void testUpdateJsonbFields() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      Map<String, Object> variantWeights = new HashMap<>();
      variantWeights.put("control", 0.3);
      variantWeights.put("variant_a", 0.7);
      updates.put("variant_weights", variantWeights);

      List<String> overrides =
          Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com");
      updates.put("overrides", overrides);

      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should return true when no fields to update")
    void testUpdateWithEmptyMap() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
      verify(pgWriterClient, never()).execute(anyString(), any(Tuple.class));
    }

    @Test
    @DisplayName("Should ignore null values in update map")
    void testUpdateWithNullValues() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      updates.put("status", null);
      updates.put("exposure", 80);
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should ignore disallowed fields")
    void testUpdateWithDisallowedFields() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      updates.put("project_key", "should_be_ignored");
      updates.put("experiment_id", "should_be_ignored");
      updates.put("created_at", "should_be_ignored");
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should handle database update failure")
    void testUpdateDatabaseFailure() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle database error during update")
    void testUpdateDatabaseError() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(pgWriterClient.execute(anyString(), any(Tuple.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle end_date to end_time conversion")
    void testUpdateEndDateConversion() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("end_date", "2025-12-31T23:59:59Z");
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle serialization error for JSONB fields")
    void testJsonbSerializationError() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Valid update");

      // Mock the client to simulate serialization error
      when(pgWriterClient.execute(anyString(), any(Tuple.class)))
          .thenReturn(Single.error(new RuntimeException("JSON serialization failed")));

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert - Should return false on error (error is caught by onErrorReturn in executeUpdate)
      testObserver.assertComplete();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle exception in create method")
    void testCreateExceptionHandling() {
      // Arrange
      com.ascend.testlab.dto.request.CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Unexpected error");
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Single<String>> function = invocation.getArgument(0);
                SqlConnection mockConnection = mock(SqlConnection.class);
                when(pgWriterClient.execute(
                        any(SqlConnection.class), anyString(), any(Tuple.class)))
                    .thenThrow(exception);
                return function.apply(mockConnection);
              });

      // Act
      TestObserver<String> testObserver = experimentDAO.create(testProjectKey, request).test();

      // Assert
      testObserver.assertNotComplete();
      testObserver.assertError(
          err -> err instanceof RuntimeException && err.getMessage().equals("Unexpected error"));
    }
  }

  // Helper method to create a valid request
  private com.ascend.testlab.dto.request.CreateExperimentRequest createValidRequest() {
    com.ascend.testlab.dto.request.CreateExperimentRequest request =
        new com.ascend.testlab.dto.request.CreateExperimentRequest();
    request.setProjectKey(testProjectKey.toString());
    request.setExperimentId(testExperimentId);
    request.setName("test_experiment");
    request.setDescription("Test description");
    request.setHypothesis("Test hypothesis");
    request.setStatus(ExperimentStatus.DRAFT.name());
    request.setType(ExperimentType.A_B.name());
    request.setCohorts(Arrays.asList("test_cohort"));
    request.setExposure(50);
    request.setThreshold(1000L);
    request.setStartTime(System.currentTimeMillis() / 1000);
    request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
    request.setCreatedBy("test@example.com");
    return request;
  }

  @Nested
  @DisplayName("Get Experiment Tests")
  class GetExperimentTests {

    @Test
    @DisplayName("Should return experiment when found")
    void testGetExperimentSuccess(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment expectedExperiment =
          createMockExperiment();
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<com.ascend.testlab.dto.entity.experiment.Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiment -> experiment.getExperimentId().equals(testExperimentId));
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when experiment not found")
    void testGetExperimentNotFound(VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.empty());

      // Act
      TestObserver<com.ascend.testlab.dto.entity.experiment.Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertNoErrors();
      testObserver.assertResult();
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database connection error")
    void testGetExperimentDatabaseError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.error(expectedException));

      // Act
      TestObserver<com.ascend.testlab.dto.entity.experiment.Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Filter Experiments Tests")
  class FilterExperimentsTests {

    @Test
    @DisplayName("Should return experiments without filters")
    void testFilterExperimentsNoFilters(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1
                  && response.getPagination().totalCount() == 1
                  && response.getPagination().currentPage() == 1
                  && response.getPagination().pageSize() == response.getExperiments().size());
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty list when no experiments found")
    void testFilterExperimentsEmpty(VertxTestContext testContext) {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(20).page(1).build();
      doReturn(Single.just(List.<Row>of()))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().isEmpty()
                  && response.getPagination().totalCount() == 0
                  && response.getPagination().currentPage() == 1
                  && response.getPagination().pageSize() == 0);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by name")
    void testFilterExperimentsWithNameFilter(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().name("test experiment").limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by status")
    void testFilterExperimentsWithStatusFilter(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().status("LIVE,PAUSED").limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by type")
    void testFilterExperimentsWithTypeFilter(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().type("A/B").limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by tag")
    void testFilterExperimentsWithTagFilter(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().tag("tag1").limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by owner")
    void testFilterExperimentsWithOwnerFilter(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().owner("owner1").limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments with multiple filters")
    void testFilterExperimentsWithMultipleFilters(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .name("test")
              .status("LIVE")
              .type("A/B")
              .limit(20)
              .page(1)
              .build();
      List<Row> mockRows = createMockRows(experiment, 1);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getExperiments().size() == 1 && response.getPagination().totalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFilterExperimentsWithPagination(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().page(2).limit(10).build();
      List<Row> mockRows = createMockRows(experiment, 15);
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response ->
              response.getPagination().totalCount() == 15
                  && response.getPagination().currentPage() == 2
                  && response.getPagination().pageSize() == response.getExperiments().size());
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when fetching experiments")
    void testFilterExperimentsError(VertxTestContext testContext) {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RuntimeException expectedException = new RuntimeException("Unexpected error");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any(Function.class)))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle multiple operations sequentially")
    void testMultipleOperationsSequentially(VertxTestContext testContext) {
      // Arrange
      com.ascend.testlab.dto.entity.experiment.Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);
      SqlConnection mockConnection = mock(SqlConnection.class);

      doReturn(Maybe.just(experiment))
          .when(pgReaderClient)
          .fetchOne(eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class));
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));
      when(pgWriterClient.executeWithTransaction(any()))
          .thenAnswer(
              invocation -> {
                Function<SqlConnection, Maybe<Boolean>> function = invocation.getArgument(0);
                return function.apply(mockConnection);
              });
      when(pgWriterClient.execute(
              eq(mockConnection), eq(WriteQuery.DELETE_EXPERIMENT_BY_ID), any(Tuple.class)))
          .thenReturn(Single.just(true));
      when(pgWriterClient.execute(
              eq(mockConnection), eq(WriteQuery.DELETE_TAG_FOR_EXPERIMENT), any(Tuple.class)))
          .thenReturn(Single.just(true));
      when(pgWriterClient.execute(
              eq(mockConnection), eq(WriteQuery.DELETE_OWNER_FOR_EXPERIMENT), any(Tuple.class)))
          .thenReturn(Single.just(true));
      when(pgWriterClient.execute(
              eq(mockConnection), eq(WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG), any(Tuple.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<com.ascend.testlab.dto.entity.experiment.Experiment> getObserver =
          experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();
      TestObserver<FilterExperimentsResponse> filterObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();
      TestObserver<Boolean> deleteObserver =
          experimentDAO.deleteExperiment(PROJECT_KEY, experiment).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      filterObserver.assertComplete().assertNoErrors();
      deleteObserver.assertComplete().assertNoErrors();

      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.FETCH_EXPERIMENT), any(Tuple.class), any(Function.class));
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      verify(pgWriterClient, times(1)).executeWithTransaction(any());
      testContext.completeNow();
    }
  }

  /**
   * Helper method to create a mock Experiment object for testing.
   *
   * @return a mock Experiment object
   */
  private com.ascend.testlab.dto.entity.experiment.Experiment createMockExperiment() {
    return com.ascend.testlab.dto.entity.experiment.Experiment.builder()
        .experimentId(testExperimentId)
        .projectKey(PROJECT_KEY)
        .name("Test Experiment")
        .description("Test Description")
        .hypothesis("Test Hypothesis")
        .status(ExperimentStatus.LIVE)
        .type(ExperimentType.A_B)
        .exposure(100)
        .threshold(1000L)
        .startTime(System.currentTimeMillis())
        .endTime(System.currentTimeMillis() + 86400000L)
        .createdBy("test-user")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .tags(List.of("tag1", "tag2"))
        .owners(List.of("owner1"))
        .build();
  }

  /**
   * Helper method to create mock Row objects for testing. Creates a list of Row objects that can be
   * mapped to Experiment entities.
   *
   * @param experiment the experiment to use for populating row data
   * @param totalCount the total count value to set in the row (for pagination)
   * @return a list of mock Row objects
   */
  private List<Row> createMockRows(
      com.ascend.testlab.dto.entity.experiment.Experiment experiment, int totalCount) {
    Row mockRow = mock(Row.class);
    OffsetDateTime now = OffsetDateTime.now();

    // Set up all the fields that ExperimentMapper expects
    when(mockRow.getString("project_key")).thenReturn(experiment.getProjectKey());
    when(mockRow.getString("experiment_id")).thenReturn(experiment.getExperimentId().toString());
    when(mockRow.getString("name")).thenReturn(experiment.getName());
    when(mockRow.getString("description")).thenReturn(experiment.getDescription());
    when(mockRow.getString("hypothesis")).thenReturn(experiment.getHypothesis());
    when(mockRow.getString("status"))
        .thenReturn(experiment.getStatus() != null ? experiment.getStatus().name() : null);
    when(mockRow.getString("type"))
        .thenReturn(experiment.getType() != null ? experiment.getType().toString() : null);
    when(mockRow.getString("guardrail_health_status")).thenReturn(null);
    when(mockRow.getArrayOfStrings("cohorts")).thenReturn(new String[0]);
    when(mockRow.getJsonObject("variant_weights")).thenReturn(null);
    when(mockRow.getString("assignment_strategy")).thenReturn(null);
    when(mockRow.getJsonObject("overrides")).thenReturn(null);
    when(mockRow.getJsonObject("rule_attributes")).thenReturn(null);
    when(mockRow.getJsonObject("winning_variant")).thenReturn(null);
    when(mockRow.getInteger("exposure")).thenReturn(experiment.getExposure());
    when(mockRow.getLong("threshold")).thenReturn(experiment.getThreshold());
    when(mockRow.getLong("start_time")).thenReturn(experiment.getStartTime());
    when(mockRow.getLong("end_time")).thenReturn(experiment.getEndTime());
    when(mockRow.getString("created_by")).thenReturn(experiment.getCreatedBy());
    when(mockRow.getOffsetDateTime("created_at")).thenReturn(now);
    when(mockRow.getOffsetDateTime("updated_at")).thenReturn(now);
    when(mockRow.getString("tags"))
        .thenReturn(experiment.getTags() != null ? String.join(",", experiment.getTags()) : null);
    when(mockRow.getString("owners"))
        .thenReturn(
            experiment.getOwners() != null ? String.join(",", experiment.getOwners()) : null);
    // Add total_count for pagination
    when(mockRow.getInteger("total_count")).thenReturn(totalCount);

    return List.of(mockRow);
  }
}
