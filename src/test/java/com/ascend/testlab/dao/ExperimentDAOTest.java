package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.impl.ExperimentDAOImpl;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
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

  @Mock private PgReaderClient pgReaderClient;

  @Mock private Row mockRow;

  private ExperimentDAO experimentDAO;

  private static final String PROJECT_ID = "project-123";
  private static final String EXPERIMENT_ID = "experiment-456";
  private static final UUID PROJECT_UUID = UUID.randomUUID();
  private static final UUID EXPERIMENT_UUID = UUID.randomUUID();

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
      when(pgReaderClient.fetchOne(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiment -> experiment.getExperimentId().equals(EXPERIMENT_UUID));
      verify(pgReaderClient, times(1)).fetchOne(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when experiment not found")
    void testGetExperimentNotFound(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Experiment not found");
      when(pgReaderClient.fetchOne(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1)).fetchOne(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle database connection error")
    void testGetExperimentDatabaseError(VertxTestContext testContext) {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database connection failed");
      when(pgReaderClient.fetchOne(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1)).fetchOne(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Fetch Experiments Tests")
  class FetchExperimentsTests {

    @Test
    @DisplayName("Should return experiments without filters")
    void testFetchExperimentsNoFilters(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty list when no experiments found")
    void testFetchExperimentsEmpty(VertxTestContext testContext) {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      doReturn(Single.just(Collections.<Experiment>emptyList()))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by name")
    void testFetchExperimentsWithNameFilter(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().name("test experiment").build();
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by status")
    void testFetchExperimentsWithStatusFilter(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .status(Arrays.asList(ExperimentStatus.LIVE, ExperimentStatus.PAUSED))
              .build();
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments by type")
    void testFetchExperimentsWithTypeFilter(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().type(Arrays.asList("A_B")).build();
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should filter experiments with multiple filters")
    void testFetchExperimentsWithMultipleFilters(VertxTestContext testContext) {
      // Arrange
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .name("test")
              .status(Arrays.asList(ExperimentStatus.LIVE))
              .type(Arrays.asList("A_B"))
              .build();
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when fetching experiments")
    void testFetchExperimentsError(VertxTestContext testContext) {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RuntimeException expectedException = new RuntimeException("Database error");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Get Experiment IDs By Tags Tests")
  class GetExperimentIdsByTagsTests {

    @Test
    @DisplayName("Should return experiment IDs when tags match")
    void testGetExperimentIdsByTagsSuccess(VertxTestContext testContext) {
      // Arrange
      Set<UUID> expectedIds = new HashSet<>(Arrays.asList(EXPERIMENT_UUID));
      List<String> tags = Arrays.asList("tag1", "tag2");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(new ArrayList<>(expectedIds)));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByTags(PROJECT_ID, tags).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(ids -> ids.size() == 1 && ids.contains(EXPERIMENT_UUID));
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty set when no tags match")
    void testGetExperimentIdsByTagsEmpty(VertxTestContext testContext) {
      // Arrange
      List<String> tags = Arrays.asList("nonexistent-tag");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(Collections.emptyList()));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByTags(PROJECT_ID, tags).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(Set::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error and return empty set")
    void testGetExperimentIdsByTagsError(VertxTestContext testContext) {
      // Arrange
      List<String> tags = Arrays.asList("tag1");
      RuntimeException expectedException = new RuntimeException("Database error");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByTags(PROJECT_ID, tags).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(Set::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Get Experiment IDs By Owners Tests")
  class GetExperimentIdsByOwnersTests {

    @Test
    @DisplayName("Should return experiment IDs when owners match")
    void testGetExperimentIdsByOwnersSuccess(VertxTestContext testContext) {
      // Arrange
      Set<UUID> expectedIds = new HashSet<>(Arrays.asList(EXPERIMENT_UUID));
      List<String> owners = Arrays.asList("owner1", "owner2");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(new ArrayList<>(expectedIds)));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByOwners(PROJECT_ID, owners).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(ids -> ids.size() == 1 && ids.contains(EXPERIMENT_UUID));
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty set when no owners match")
    void testGetExperimentIdsByOwnersEmpty(VertxTestContext testContext) {
      // Arrange
      List<String> owners = Arrays.asList("nonexistent-owner");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(Collections.emptyList()));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByOwners(PROJECT_ID, owners).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(Set::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error and return empty set")
    void testGetExperimentIdsByOwnersError(VertxTestContext testContext) {
      // Arrange
      List<String> owners = Arrays.asList("owner1");
      RuntimeException expectedException = new RuntimeException("Database error");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<Set<UUID>> testObserver =
          experimentDAO.getExperimentIdsByOwners(PROJECT_ID, owners).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(Set::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Fetch Experiments By IDs Tests")
  class FetchExperimentsByIdsTests {

    @Test
    @DisplayName("Should return experiments by IDs without filters")
    void testFetchExperimentsByIdsNoFilters(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return experiments by IDs with name filter")
    void testFetchExperimentsByIdsWithNameFilter(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request = FilterExperimentsRequest.builder().name("test").build();
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return experiments by IDs with status filter")
    void testFetchExperimentsByIdsWithStatusFilter(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().status(Arrays.asList(ExperimentStatus.LIVE)).build();
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return experiments by IDs with multiple filters")
    void testFetchExperimentsByIdsWithMultipleFilters(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .name("test")
              .status(Arrays.asList(ExperimentStatus.LIVE))
              .type(Arrays.asList("A_B"))
              .build();
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return empty list when no experiments match IDs and filters")
    void testFetchExperimentsByIdsEmpty(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.just(Collections.emptyList()));

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(List::isEmpty);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle error when fetching experiments by IDs")
    void testFetchExperimentsByIdsError(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID));
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RuntimeException expectedException = new RuntimeException("Database error");
      when(pgReaderClient.fetchAll(anyString(), any(Tuple.class), any()))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple experiment IDs")
    void testFetchExperimentsByIdsMultipleIds(VertxTestContext testContext) {
      // Arrange
      Set<String> experimentIds = new HashSet<>(Arrays.asList(EXPERIMENT_ID, "experiment-789"));
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      List<Experiment> expectedExperiments = Arrays.asList(createMockExperiment());
      doReturn(Single.just(expectedExperiments))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<List<Experiment>> testObserver =
          experimentDAO.fetchExperimentsByIds(PROJECT_ID, experimentIds, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(experiments -> experiments.size() == 1);
      verify(pgReaderClient, times(1)).fetchAll(anyString(), any(Tuple.class), any());
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
      Set<UUID> tagIds = new HashSet<>(Arrays.asList(EXPERIMENT_UUID));
      FilterExperimentsRequest request = new FilterExperimentsRequest();

      doReturn(Single.just(experiment))
          .when(pgReaderClient)
          .fetchOne(anyString(), any(Tuple.class), any());
      doReturn(Single.just(Arrays.asList(experiment)))
          .doReturn(Single.just(new ArrayList<>(tagIds)))
          .when(pgReaderClient)
          .fetchAll(anyString(), any(Tuple.class), any());

      // Act
      TestObserver<Experiment> getObserver =
          experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();
      TestObserver<List<Experiment>> fetchObserver =
          experimentDAO.fetchExperiments(PROJECT_ID, request).test();
      TestObserver<Set<UUID>> tagsObserver =
          experimentDAO.getExperimentIdsByTags(PROJECT_ID, Arrays.asList("tag1")).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      fetchObserver.assertComplete().assertNoErrors();
      tagsObserver.assertComplete().assertNoErrors();

      verify(pgReaderClient, times(1)).fetchOne(anyString(), any(Tuple.class), any());
      verify(pgReaderClient, times(2)).fetchAll(anyString(), any(Tuple.class), any());
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
        .experimentId(EXPERIMENT_UUID)
        .projectId(PROJECT_UUID)
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
        .createdAt(Timestamp.valueOf(LocalDateTime.now()))
        .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
        .tags("tag1,tag2")
        .owner("owner1")
        .build();
  }
}
