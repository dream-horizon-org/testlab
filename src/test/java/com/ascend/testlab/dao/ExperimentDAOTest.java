package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.impl.ExperimentDAOImpl;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Row;
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
class ExperimentDAOTest {

  @Mock private PgReaderClient pgReaderClient;

  private ExperimentDAO experimentDAO;

  private static final String PROJECT_KEY = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_ID = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    this.experimentDAO = new ExperimentDAOImpl(pgReaderClient);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create DAO with valid dependencies")
    void testConstructorWithValidDependencies(VertxTestContext testContext) {
      // Act
      ExperimentDAO dao = new ExperimentDAOImpl(pgReaderClient);

      // Assert
      assertNotNull(dao);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Get Experiment Tests")
  class GetExperimentTests {

    @Test
    @DisplayName("Should return experiment when found")
    void testGetExperimentSuccess(VertxTestContext testContext) {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          experiment -> experiment.getExperimentId().equals(UUID.fromString(EXPERIMENT_ID)));
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when experiment not found")
    void testGetExperimentNotFound(VertxTestContext testContext) {
      // Arrange
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertNoErrors();
      testObserver.assertResult();
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database connection error")
    void testGetExperimentDatabaseError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchOne(
              eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class)))
          .thenReturn(Maybe.error(expectedException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class));
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
      Experiment experiment = createMockExperiment();
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
                  && response.getPagination().getTotalCount() == 1
                  && response.getPagination().getCurrentPage() == 1
                  && response.getPagination().getPageSize() == 20);
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
                  && response.getPagination().getTotalCount() == 0
                  && response.getPagination().getCurrentPage() == 1
                  && response.getPagination().getPageSize() == 20);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by name")
    void testFilterExperimentsWithNameFilter(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by status")
    void testFilterExperimentsWithStatusFilter(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by type")
    void testFilterExperimentsWithTypeFilter(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by tag")
    void testFilterExperimentsWithTagFilter(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by owner")
    void testFilterExperimentsWithOwnerFilter(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments with multiple filters")
    void testFilterExperimentsWithMultipleFilters(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getExperiments().size() == 1
                  && response.getPagination().getTotalCount() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFilterExperimentsWithPagination(VertxTestContext testContext) {
      // Arrange
      Experiment experiment = createMockExperiment();
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
              response.getPagination().getTotalCount() == 15
                  && response.getPagination().getCurrentPage() == 2
                  && response.getPagination().getPageSize() == 10);
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
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(20).page(1).build();
      List<Row> mockRows = createMockRows(experiment, 1);

      doReturn(Maybe.just(experiment))
          .when(pgReaderClient)
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class));
      doReturn(Single.just(mockRows))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any(Function.class));

      // Act
      TestObserver<Experiment> getObserver =
          experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();
      TestObserver<FilterExperimentsResponse> filterObserver =
          experimentDAO.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      filterObserver.assertComplete().assertNoErrors();

      verify(pgReaderClient, times(1))
          .fetchOne(eq(ReadQuery.GET_EXPERIMENT), any(Tuple.class), any(Function.class));
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any(Function.class));
      testContext.completeNow();
    }
  }

  /**
   * Helper method to create a mock Experiment object for testing.
   *
   * @return a mock Experiment object
   */
  private Experiment createMockExperiment() {
    return Experiment.builder()
        .experimentId(UUID.fromString(EXPERIMENT_ID))
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
        .tags("tag1,tag2")
        .owner("owner1")
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
  private List<Row> createMockRows(Experiment experiment, int totalCount) {
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
    when(mockRow.getString("tags")).thenReturn(experiment.getTags());
    when(mockRow.getString("owners")).thenReturn(experiment.getOwner());
    // Add total_count for pagination
    when(mockRow.getInteger("total_count")).thenReturn(totalCount);

    return List.of(mockRow);
  }
}
