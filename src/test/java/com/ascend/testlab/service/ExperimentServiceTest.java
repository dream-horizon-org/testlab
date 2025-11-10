package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.impl.ExperimentServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for ExperimentServiceImpl.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("ExperimentService Tests")
public class ExperimentServiceTest {

  private ExperimentService experimentService;

  @Mock private ExperimentDAO experimentDAO;

  private static final String PROJECT_ID = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_ID = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    experimentService = new ExperimentServiceImpl(experimentDAO);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid DAO")
    void testConstructorWithValidDAO() {
      // Act
      ExperimentServiceImpl service = new ExperimentServiceImpl(experimentDAO);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create service with null DAO")
    void testConstructorWithNullDAO() {
      // Act - Constructor doesn't validate null, but will fail at runtime
      ExperimentServiceImpl service = new ExperimentServiceImpl(null);

      // Assert
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Get Experiment Success Tests")
  class GetExperimentSuccessTests {

    @Test
    @DisplayName("Should return experiment when found")
    void testGetExperimentSuccess() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(
          experiment -> experiment.getExperimentId().equals(UUID.fromString(EXPERIMENT_ID)));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should return experiment with correct project ID")
    void testGetExperimentWithCorrectProjectId() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      Experiment result = testObserver.values().get(0);
      assertEquals(UUID.fromString(PROJECT_ID), result.getProjectId());
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }
  }

  @Nested
  @DisplayName("Get Experiment Error Handling Tests")
  class GetExperimentErrorHandlingTests {

    @Test
    @DisplayName("Should return EXPERIMENT_NOT_FOUND when experiment not found")
    void testGetExperimentNotFound() {
      // Arrange
      NoSuchElementException notFoundException = new NoSuchElementException("Experiment not found");
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.error(notFoundException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error -> {
            if (error instanceof RestException restException) {
              return restException
                  .getErrorCode()
                  .equals(ErrorEnum.EXPERIMENT_NOT_FOUND.getErrorCode());
            }
            return false;
          });
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should propagate RestException as-is")
    void testGetExperimentRestException() {
      // Arrange
      RestException restException = new RestException(ErrorEnum.INVALID_EXPERIMENT_STATUS);
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.error(restException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.INVALID_EXPERIMENT_STATUS.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should propagate RuntimeException from DAO")
    void testGetExperimentRuntimeException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.error(runtimeException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should propagate IllegalArgumentException from DAO")
    void testGetExperimentIllegalArgumentException() {
      // Arrange
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Invalid argument");
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.error(illegalArgumentException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(IllegalArgumentException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }
  }

  @Nested
  @DisplayName("Filter Experiments Success Tests")
  class FilterExperimentsSuccessTests {

    @Test
    @DisplayName("Should return filtered experiments without filters")
    void testFilterExperimentsNoFilters() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.getExperimentList().size() == 1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with status filter")
    void testFilterExperimentsWithStatusFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().status("LIVE,PAUSED").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with type filter")
    void testFilterExperimentsWithTypeFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().type("A_B,MULTI_VARIANT").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with name filter")
    void testFilterExperimentsWithNameFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().name("test experiment").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with tag filter")
    void testFilterExperimentsWithTagFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().tag("tag1,tag2").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with owner filter")
    void testFilterExperimentsWithOwnerFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().owner("owner1,owner2").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with multiple filters")
    void testFilterExperimentsWithMultipleFilters() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .status("LIVE")
              .type("A_B")
              .name("test")
              .tag("tag1")
              .owner("owner1")
              .limit(10)
              .page(1)
              .build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return empty list when no experiments match")
    void testFilterExperimentsEmptyResult() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse emptyResponse =
          new FilterExperimentsResponse(
              Collections.emptyList(), new FilterExperimentsResponse.PaginationMeta(1, 20, 0));
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(emptyResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(response -> response.getExperimentList().isEmpty());
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should return paginated results with correct metadata")
    void testFilterExperimentsWithPagination() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(10).page(2).build();
      FilterExperimentsResponse.PaginationMeta paginationMeta =
          new FilterExperimentsResponse.PaginationMeta(2, 10, 25);
      FilterExperimentsResponse expectedResponse =
          new FilterExperimentsResponse(Arrays.asList(createMockExperiment()), paginationMeta);
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            FilterExperimentsResponse.PaginationMeta meta = response.getPagination();
            return meta.getCurrentPage() == 2
                && meta.getPageSize() == 10
                && meta.getTotalCount() == 25;
          });
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }
  }

  @Nested
  @DisplayName("Filter Experiments Error Handling Tests")
  class FilterExperimentsErrorHandlingTests {

    @Test
    @DisplayName("Should propagate RestException from DAO")
    void testFilterExperimentsRestException() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RestException restException = new RestException(ErrorEnum.INVALID_EXPERIMENT_STATUS);
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.error(restException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should propagate RuntimeException from DAO")
    void testFilterExperimentsRuntimeException() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RuntimeException runtimeException = new RuntimeException("Database error");
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.error(runtimeException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should propagate IllegalArgumentException from DAO")
    void testFilterExperimentsIllegalArgumentException() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      IllegalArgumentException illegalArgumentException =
          new IllegalArgumentException("Invalid argument");
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.error(illegalArgumentException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertError(IllegalArgumentException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }
  }

  @Nested
  @DisplayName("Reactive Behavior Tests")
  class ReactiveBehaviorTests {

    @Test
    @DisplayName("Should call DAO method exactly once for getExperiment")
    void testGetExperimentDAOCalledOnce() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should call DAO method exactly once for filterExperiments")
    void testFilterExperimentsDAOCalledOnce() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should handle multiple subscriptions correctly for getExperiment")
    void testGetExperimentMultipleSubscriptions() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment));

      // Act - Each subscription triggers the Single chain again
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(experimentDAO, times(2)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should handle multiple subscriptions correctly for filterExperiments")
    void testFilterExperimentsMultipleSubscriptions() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse));

      // Act - Each subscription triggers the Single chain again
      TestObserver<FilterExperimentsResponse> testObserver1 =
          experimentService.filterExperiments(PROJECT_ID, request).test();
      TestObserver<FilterExperimentsResponse> testObserver2 =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(experimentDAO, times(2)).filterExperiments(PROJECT_ID, request);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle delayed DAO responses for getExperiment")
    void testGetExperimentDelayedResponse() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment).delay(100, TimeUnit.MILLISECONDS));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();

      // Wait for completion
      testObserver.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle delayed DAO responses for filterExperiments")
    void testFilterExperimentsDelayedResponse() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(expectedResponse).delay(100, TimeUnit.MILLISECONDS));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Wait for completion
      testObserver.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle null project ID")
    void testGetExperimentWithNullProjectId() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(null, EXPERIMENT_ID))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(null, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(experimentDAO, times(1)).getExperiment(null, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should handle null experiment ID")
    void testGetExperimentWithNullExperimentId() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_ID, null))
          .thenReturn(Single.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_ID, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, null);
    }

    @Test
    @DisplayName("Should handle null request for filterExperiments")
    void testFilterExperimentsWithNullRequest() {
      // Arrange
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_ID, null))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_ID, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, null);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle multiple operations sequentially")
    void testMultipleOperationsSequentially() {
      // Arrange
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse filterResponse = createMockFilterResponse();

      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(experiment));
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(filterResponse));

      // Act
      TestObserver<Experiment> getObserver =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();
      TestObserver<FilterExperimentsResponse> filterObserver =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      filterObserver.assertComplete().assertNoErrors();

      verify(experimentDAO, times(1)).getExperiment(PROJECT_ID, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_ID, request);
    }

    @Test
    @DisplayName("Should handle concurrent service calls")
    void testConcurrentServiceCalls() {
      // Arrange
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse filterResponse = createMockFilterResponse();

      when(experimentDAO.getExperiment(PROJECT_ID, EXPERIMENT_ID))
          .thenReturn(Single.just(experiment));
      when(experimentDAO.filterExperiments(PROJECT_ID, request))
          .thenReturn(Single.just(filterResponse));

      // Act
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_ID, EXPERIMENT_ID).test();
      TestObserver<FilterExperimentsResponse> testObserver3 =
          experimentService.filterExperiments(PROJECT_ID, request).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testObserver3.assertComplete();
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
        .projectId(UUID.fromString(PROJECT_ID))
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

  /**
   * Helper method to create a mock FilterExperimentsResponse object for testing.
   *
   * @return a mock FilterExperimentsResponse object
   */
  private FilterExperimentsResponse createMockFilterResponse() {
    FilterExperimentsResponse.PaginationMeta paginationMeta =
        new FilterExperimentsResponse.PaginationMeta(1, 20, 1);
    return new FilterExperimentsResponse(Arrays.asList(createMockExperiment()), paginationMeta);
  }
}
