package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.DeleteExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.impl.ExperimentServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import java.time.Instant;
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
  @Mock private AdminDAO adminDAO;
  @Mock private AllocationDAO allocationDAO;
  @Mock private AllocationService allocationService;

  private static final String PROJECT_KEY = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_ID = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    experimentService =
        new ExperimentServiceImpl(experimentDAO, adminDAO, allocationDAO, allocationService);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid DAO")
    void testConstructorWithValidDAO() {
      // Act
      ExperimentServiceImpl service =
          new ExperimentServiceImpl(experimentDAO, adminDAO, allocationDAO, allocationService);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create service with null DAO")
    void testConstructorWithNullDAO() {
      // Act - Constructor doesn't validate null, but will fail at runtime
      ExperimentServiceImpl service = new ExperimentServiceImpl(null, null, null, null);

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
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(
          experiment -> experiment.getExperimentId().equals(UUID.fromString(EXPERIMENT_ID)));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }

    @Test
    @DisplayName("Should return experiment with correct project Key")
    void testGetExperimentWithCorrectProjectKey() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      Experiment result = testObserver.values().get(0);
      assertEquals(PROJECT_KEY, result.getProjectKey());
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }

    @Test
    @DisplayName("Should return experiment with variant counts when available")
    void testGetExperimentWithVariantCounts() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      Map<String, Long> variantCounts = new HashMap<>();
      variantCounts.put("variant1", 100L);
      variantCounts.put("variant2", 10L);
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(variantCounts));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      Experiment result = testObserver.values().get(0);
      assertNotNull(result.getVariantCounts());
      assertEquals(100L, result.getVariantCounts().get("variant1"));
      assertEquals(10L, result.getVariantCounts().get("variant2"));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }

    @Test
    @DisplayName("Should return experiment with null variant counts when empty")
    void testGetExperimentWithEmptyVariantCounts() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      Experiment result = testObserver.values().get(0);
      assertNull(result.getVariantCounts());
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }
  }

  @Nested
  @DisplayName("Get Experiment Error Handling Tests")
  class GetExperimentErrorHandlingTests {

    @Test
    @DisplayName("Should return EXPERIMENT_NOT_FOUND when experiment not found")
    void testGetExperimentNotFound() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID)).thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

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
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should propagate RestException as-is")
    void testGetExperimentRestException() {
      // Arrange
      RestException restException = new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED);
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.error(restException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, never()).getVariantCount(anyString(), any());
    }

    @Test
    @DisplayName("Should propagate RuntimeException from DAO")
    void testGetExperimentRuntimeException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.error(runtimeException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, never()).getVariantCount(anyString(), any());
    }

    @Test
    @DisplayName("Should handle error from variantCountDAO")
    void testGetExperimentVariantCountDAOError() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      RuntimeException runtimeException = new RuntimeException("Aerospike connection failed");
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.error(runtimeException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
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
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.getExperiments().size() == 1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with status filter")
    void testFilterExperimentsWithStatusFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().status("LIVE,PAUSED").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with type filter")
    void testFilterExperimentsWithTypeFilter() {
      // Arrange
      FilterExperimentsRequest request = FilterExperimentsRequest.builder().type("A/B,A/A").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with name filter")
    void testFilterExperimentsWithNameFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().name("test experiment").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with tag filter")
    void testFilterExperimentsWithTagFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().tag("tag1,tag2").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with owner filter")
    void testFilterExperimentsWithOwnerFilter() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().owner("owner1,owner2").build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return filtered experiments with multiple filters")
    void testFilterExperimentsWithMultipleFilters() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder()
              .status("LIVE")
              .type("A/B")
              .name("test")
              .tag("tag1")
              .owner("owner1")
              .limit(10)
              .page(1)
              .build();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return empty list when no experiments match")
    void testFilterExperimentsEmptyResult() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse emptyResponse =
          new FilterExperimentsResponse(
              Collections.emptyList(), new PaginationMeta(1, 0, 0, false));
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(emptyResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(response -> response.getExperiments().isEmpty());
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return paginated results with correct metadata")
    void testFilterExperimentsWithPagination() {
      // Arrange
      FilterExperimentsRequest request =
          FilterExperimentsRequest.builder().limit(10).page(2).build();
      List<Experiment> mockExperiments = List.of(createMockExperiment());
      PaginationMeta paginationMeta = new PaginationMeta(2, mockExperiments.size(), 25, true);
      FilterExperimentsResponse expectedResponse =
          new FilterExperimentsResponse(mockExperiments, paginationMeta);
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            PaginationMeta meta = response.getPagination();
            return meta.currentPage() == 2
                && meta.pageSize() == response.getExperiments().size()
                && meta.totalCount() == 25;
          });
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
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
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should propagate RuntimeException from DAO")
    void testFilterExperimentsRuntimeException() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      RuntimeException runtimeException = new RuntimeException("Database error");
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.error(runtimeException));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
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
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }

    @Test
    @DisplayName("Should call DAO method exactly once for filterExperiments")
    void testFilterExperimentsDAOCalledOnce() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle multiple subscriptions correctly for getExperiment")
    void testGetExperimentMultipleSubscriptions() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(new HashMap<>()));

      // Act - Each subscription triggers the Single chain again
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(experimentDAO, times(2)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(2)).getVariantCount(PROJECT_KEY, expectedExperiment);
    }

    @Test
    @DisplayName("Should handle multiple subscriptions correctly for filterExperiments")
    void testFilterExperimentsMultipleSubscriptions() {
      // Arrange
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse));

      // Act - Each subscription triggers the Single chain again
      TestObserver<FilterExperimentsResponse> testObserver1 =
          experimentService.filterExperiments(PROJECT_KEY, request).test();
      TestObserver<FilterExperimentsResponse> testObserver2 =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(experimentDAO, times(2)).filterExperiments(PROJECT_KEY, request);
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
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(expectedExperiment).delay(100, TimeUnit.MILLISECONDS));
      Map<String, Long> emptyMap = new HashMap<>();
      when(adminDAO.getVariantCount(PROJECT_KEY, expectedExperiment))
          .thenReturn(Single.just(emptyMap).delay(100, TimeUnit.MILLISECONDS));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

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
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResponse).delay(100, TimeUnit.MILLISECONDS));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Wait for completion
      testObserver.awaitDone(1, TimeUnit.SECONDS);

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle null project Key")
    void testGetExperimentWithNullProjectKey() {
      // Arrange
      when(experimentDAO.getExperiment(null, EXPERIMENT_ID)).thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(null, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.EXPERIMENT_NOT_FOUND.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(null, EXPERIMENT_ID);
    }

    @Test
    @DisplayName("Should handle null experiment ID")
    void testGetExperimentWithNullExperimentId() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, null)).thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, null).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.EXPERIMENT_NOT_FOUND.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, null);
    }

    @Test
    @DisplayName("Should handle null request for filterExperiments")
    void testFilterExperimentsWithNullRequest() {
      // Arrange
      FilterExperimentsResponse expectedResponse = createMockFilterResponse();
      when(experimentDAO.filterExperiments(PROJECT_KEY, null))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<FilterExperimentsResponse> testObserver =
          experimentService.filterExperiments(PROJECT_KEY, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, null);
    }
  }

  @Nested
  @DisplayName("Delete Experiment Success Tests")
  class DeleteExperimentSuccessTests {
    Experiment experiment = createMockExperiment();

    @Test
    @DisplayName("Should return true when experiment is deleted successfully")
    void testDeleteExperimentSuccess() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment)).thenReturn(Single.just(true));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(DeleteExperimentResponse::isSuccess);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
    }

    @Test
    @DisplayName("Should call DAO methods correctly for deleteExperiment")
    void testDeleteExperimentDAOCalledOnce() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment)).thenReturn(Single.just(true));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
    }
  }

  @Nested
  @DisplayName("Delete Experiment Error Handling Tests")
  class DeleteExperimentErrorHandlingTests {
    Experiment experiment = createMockExperiment();

    @Test
    @DisplayName("Should return EXPERIMENT_NOT_FOUND when experiment not found in getExperiment")
    void testDeleteExperimentNotFound() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID)).thenReturn(Maybe.empty());

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

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
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, never()).deleteExperiment(anyString(), any(Experiment.class));
    }

    @Test
    @DisplayName("Should propagate RestException when getExperiment fails")
    void testDeleteExperimentGetExperimentRestException() {
      // Arrange
      RestException restException = new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED);
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.error(restException));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, never()).deleteExperiment(anyString(), any(Experiment.class));
    }

    @Test
    @DisplayName("Should propagate RestException when getExperiment throws error")
    void testDeleteExperimentGetExperimentRuntimeException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.error(runtimeException));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      // getExperiment wraps RuntimeException in REST_GET_EXPERIMENT_BY_ID_FAILED,
      // and ErrorEnum.handleException preserves the original RestException
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, never()).deleteExperiment(anyString(), any(Experiment.class));
    }

    @Test
    @DisplayName(
        "Should wrap RuntimeException from deleteExperiment in REST_DELETE_EXPERIMENT_FAILED")
    void testDeleteExperimentDeleteRuntimeException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Delete operation failed");
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment))
          .thenReturn(Single.error(runtimeException));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
    }

    @Test
    @DisplayName("Should propagate RestException from deleteExperiment")
    void testDeleteExperimentDeleteRestException() {
      // Arrange
      RestException restException = new RestException(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED);
      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment))
          .thenReturn(Single.error(restException));

      // Act
      TestObserver<DeleteExperimentResponse> testObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED.getErrorCode()));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
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

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, experiment))
          .thenReturn(Single.just(new HashMap<>()));
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(filterResponse));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment)).thenReturn(Single.just(true));

      // Act
      TestObserver<Experiment> getObserver =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();
      TestObserver<FilterExperimentsResponse> filterObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();
      TestObserver<DeleteExperimentResponse> deleteObserver =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      filterObserver.assertComplete().assertNoErrors();
      deleteObserver.assertComplete().assertNoErrors();

      // getExperiment is called once for getExperiment and once for deleteExperiment
      verify(experimentDAO, times(2)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(adminDAO, times(1)).getVariantCount(PROJECT_KEY, experiment);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
    }

    @Test
    @DisplayName("Should handle concurrent service calls")
    void testConcurrentServiceCalls() {
      // Arrange
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse filterResponse = createMockFilterResponse();

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(experiment));
      when(adminDAO.getVariantCount(PROJECT_KEY, experiment))
          .thenReturn(Single.just(new HashMap<>()));
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(filterResponse));
      when(experimentDAO.deleteExperiment(PROJECT_KEY, experiment)).thenReturn(Single.just(true));

      // Act
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_KEY, EXPERIMENT_ID).test();
      TestObserver<FilterExperimentsResponse> testObserver3 =
          experimentService.filterExperiments(PROJECT_KEY, request).test();
      TestObserver<DeleteExperimentResponse> testObserver4 =
          experimentService.deleteExperiment(PROJECT_KEY, EXPERIMENT_ID).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testObserver3.assertComplete();
      testObserver4.assertComplete();

      // getExperiment is called 3 times: twice for getExperiment calls and once for
      // deleteExperiment
      verify(experimentDAO, times(3)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
      verify(experimentDAO, times(1)).deleteExperiment(PROJECT_KEY, experiment);
    }
  }

  @Nested
  @DisplayName("Create Experiment With Overrides Tests")
  class CreateExperimentWithOverridesTests {

    @Test
    @DisplayName("Should create experiment and apply overrides successfully")
    void testCreateExperimentWithOverridesSuccess() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      List<UserExperimentMap> appliedOverrides =
          List.of(
              createMockUserExperimentMap("user1", "control"),
              createMockUserExperimentMap("user2", "control"),
              createMockUserExperimentMap("user3", "variant1"));

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.just(appliedOverrides));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.isStatus());
      verify(experimentDAO, times(1)).createExperiment(eq(PROJECT_KEY), any(Experiment.class));
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
    }

    @Test
    @DisplayName("Should create experiment without overrides when not provided")
    void testCreateExperimentWithoutOverrides() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      request.setOverrides(null);

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.isStatus());
      verify(experimentDAO, times(1)).createExperiment(eq(PROJECT_KEY), any(Experiment.class));
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should create experiment when overrides has empty override IDs")
    void testCreateExperimentWithEmptyOverrideIds() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      Overrides overrides = new Overrides();
      overrides.setOverrideIds(new HashMap<>());
      request.setOverrides(overrides);

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should still succeed when override application fails")
    void testCreateExperimentSucceedsWhenOverridesFail() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.error(new RuntimeException("Failed to apply overrides")));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert - Experiment should still be created successfully even if overrides fail
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.isStatus());
      verify(experimentDAO, times(1)).createExperiment(eq(PROJECT_KEY), any(Experiment.class));
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
    }

    @Test
    @DisplayName("Should fail when experiment creation fails")
    void testCreateExperimentFailsWhenDAOFails() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.error(new RuntimeException("Database error")));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      verify(experimentDAO, times(1)).createExperiment(eq(PROJECT_KEY), any(Experiment.class));
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should apply multiple variant overrides")
    void testCreateExperimentWithMultipleVariantOverrides() {
      // Arrange
      CreateExperimentRequest request = createMockCreateRequest();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put("control", List.of("user1", "user2", "user3"));
      overrideIds.put("variant1", List.of("user4", "user5"));
      overrideIds.put("variant2", List.of("user6", "user7", "user8", "user9"));
      overrides.setOverrideIds(overrideIds);
      request.setOverrides(overrides);

      List<UserExperimentMap> appliedOverrides =
          List.of(
              createMockUserExperimentMap("user1", "control"),
              createMockUserExperimentMap("user2", "control"),
              createMockUserExperimentMap("user3", "control"),
              createMockUserExperimentMap("user4", "variant1"),
              createMockUserExperimentMap("user5", "variant1"),
              createMockUserExperimentMap("user6", "variant2"),
              createMockUserExperimentMap("user7", "variant2"),
              createMockUserExperimentMap("user8", "variant2"),
              createMockUserExperimentMap("user9", "variant2"));

      when(experimentDAO.createExperiment(eq(PROJECT_KEY), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.just(appliedOverrides));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.createExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
    }
  }

  @Nested
  @DisplayName("Update Experiment With Overrides Tests")
  class UpdateExperimentWithOverridesTests {

    @Test
    @DisplayName("Should update experiment and apply overrides successfully")
    void testUpdateExperimentWithOverridesSuccess() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      Experiment existingExperiment = createMockExperiment();

      List<UserExperimentMap> appliedOverrides =
          List.of(
              createMockUserExperimentMap("user1", "control"),
              createMockUserExperimentMap("user2", "variant1"));

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.just(appliedOverrides));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.isStatus());
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, EXPERIMENT_ID);
      verify(experimentDAO, times(1))
          .updateExperiment(eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class));
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
    }

    @Test
    @DisplayName("Should update experiment without overrides when not provided")
    void testUpdateExperimentWithoutOverrides() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      request.setOverrides(null);

      Experiment existingExperiment = createMockExperiment();

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should update experiment when overrides has empty override IDs")
    void testUpdateExperimentWithEmptyOverrideIds() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = new Overrides();
      overrides.setOverrideIds(new HashMap<>());
      request.setOverrides(overrides);

      Experiment existingExperiment = createMockExperiment();

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should still succeed when override application fails during update")
    void testUpdateExperimentSucceedsWhenOverridesFail() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      Experiment existingExperiment = createMockExperiment();

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.error(new RuntimeException("Failed to apply overrides")));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert - Experiment should still be updated successfully even if overrides fail
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(response -> response.isStatus());
      verify(experimentDAO, times(1))
          .updateExperiment(eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class));
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
    }

    @Test
    @DisplayName("Should fail when experiment not found during update")
    void testUpdateExperimentFailsWhenExperimentNotFound() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID)).thenReturn(Maybe.empty());

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.EXPERIMENT_NOT_FOUND.getErrorCode()));
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should fail when experiment update fails")
    void testUpdateExperimentFailsWhenDAOFails() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = createMockOverrides();
      request.setOverrides(overrides);

      Experiment existingExperiment = createMockExperiment();

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.error(new RuntimeException("Database error")));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertNotComplete();
      verify(allocationService, never())
          .applyOverrides(anyString(), any(Experiment.class), any(Overrides.class));
    }

    @Test
    @DisplayName("Should apply overrides for adding new users during update")
    void testUpdateExperimentAddingNewUsersViaOverrides() {
      // Arrange
      UUID experimentId = UUID.fromString(EXPERIMENT_ID);
      UpdateExperimentRequest request = createMockUpdateRequest();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put("control", List.of("new_user1", "new_user2"));
      overrideIds.put("variant1", List.of("new_user3", "new_user4", "new_user5"));
      overrides.setOverrideIds(overrideIds);
      request.setOverrides(overrides);

      Experiment existingExperiment = createMockExperiment();

      List<UserExperimentMap> appliedOverrides =
          List.of(
              createMockUserExperimentMap("new_user1", "control"),
              createMockUserExperimentMap("new_user2", "control"),
              createMockUserExperimentMap("new_user3", "variant1"),
              createMockUserExperimentMap("new_user4", "variant1"),
              createMockUserExperimentMap("new_user5", "variant1"));

      when(experimentDAO.getExperiment(PROJECT_KEY, EXPERIMENT_ID))
          .thenReturn(Maybe.just(existingExperiment));
      when(experimentDAO.updateExperiment(
              eq(PROJECT_KEY), eq(existingExperiment), any(Experiment.class)))
          .thenReturn(Single.just(true));
      when(allocationService.applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides)))
          .thenReturn(Single.just(appliedOverrides));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.updateExperiment(PROJECT_KEY, experimentId, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationService, times(1))
          .applyOverrides(eq(PROJECT_KEY), any(Experiment.class), eq(overrides));
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
        .tags(List.of("tag1", "tag2"))
        .owners(List.of("owner1"))
        .build();
  }

  /**
   * Helper method to create a mock FilterExperimentsResponse object for testing.
   *
   * @return a mock FilterExperimentsResponse object
   */
  private FilterExperimentsResponse createMockFilterResponse() {
    PaginationMeta paginationMeta = new PaginationMeta(1, 20, 1, false);
    return new FilterExperimentsResponse(List.of(createMockExperiment()), paginationMeta);
  }

  /**
   * Helper method to create a mock CreateExperimentRequest for testing.
   *
   * @return a mock CreateExperimentRequest object
   */
  private CreateExperimentRequest createMockCreateRequest() {
    CreateExperimentRequest request = new CreateExperimentRequest();
    request.setName("Test Experiment");
    request.setDescription("Test Description");
    request.setHypothesis("Test Hypothesis");
    request.setStatus(ExperimentStatus.DRAFT.name());
    request.setType(ExperimentType.A_B.name());
    request.setDistributionStrategy(DistributionStrategy.RANDOM.name());
    request.setAssignmentDomain(AssignmentDomain.COHORT.name());
    request.setExposure(100);
    request.setThreshold(1000L);
    request.setCreatedBy("test-user");
    request.setTags(List.of("tag1", "tag2"));
    request.setOwners(List.of("owner1"));

    // Set up variants
    Map<String, Variant> variants = new HashMap<>();
    variants.put("control", Variant.builder().displayName("Control").build());
    variants.put("variant1", Variant.builder().displayName("Variant 1").build());
    request.setVariants(variants);

    // Set up variant weights
    CohortVariantWeights variantWeights =
        CohortVariantWeights.builder().weights(Map.of("control", 50.0, "variant1", 50.0)).build();
    request.setVariantWeights(variantWeights);

    return request;
  }

  /**
   * Helper method to create a mock UpdateExperimentRequest for testing.
   *
   * @return a mock UpdateExperimentRequest object
   */
  private UpdateExperimentRequest createMockUpdateRequest() {
    UpdateExperimentRequest request = new UpdateExperimentRequest();
    request.setName("Updated Experiment");
    request.setDescription("Updated Description");
    request.setUpdatedBy("test-user");
    return request;
  }

  /**
   * Helper method to create a mock Overrides object for testing.
   *
   * @return a mock Overrides object
   */
  private Overrides createMockOverrides() {
    Overrides overrides = new Overrides();
    Map<String, List<String>> overrideIds = new HashMap<>();
    overrideIds.put("control", List.of("user1", "user2"));
    overrideIds.put("variant1", List.of("user3", "user4"));
    overrides.setOverrideIds(overrideIds);
    return overrides;
  }

  /**
   * Helper method to create a mock UserExperimentMap for testing.
   *
   * @param userId the user ID
   * @param variantName the variant name
   * @return a mock UserExperimentMap object
   */
  private UserExperimentMap createMockUserExperimentMap(String userId, String variantName) {
    return UserExperimentMap.builder()
        .experimentId(UUID.fromString(EXPERIMENT_ID))
        .variantName(variantName)
        .status("ASSIGNED")
        .assignedAt(System.currentTimeMillis())
        .build();
  }
}
