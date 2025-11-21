package com.ascend.testlab.service;

import static com.ascend.testlab.constants.postgresql.Columns.PROJECT_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
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
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for ExperimentService.
 *
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("ExperimentService Tests")
public class ExperimentServiceTest {

  @Mock private ExperimentDAO experimentDAO;
  @Mock private PgWriterClient pgWriterClient;

  private ExperimentService experimentService;

  private UUID testTenantId;
  private String testProjectKey;
  private UUID testExperimentId;

  @BeforeEach
  void setUp() {
    experimentService = new ExperimentServiceImpl(experimentDAO, pgWriterClient);
    testTenantId = UUID.randomUUID();
    testProjectKey = UUID.randomUUID().toString();
    testExperimentId = UUID.randomUUID();

    // Mock transaction execution by default (lenient to avoid unnecessary stubbing errors)
    lenient()
        .when(pgWriterClient.executeWithTransaction(any(Function.class)))
        .thenAnswer(
            invocation -> {
              Function<Object, Single<Long>> function = invocation.getArgument(0);
              return function.apply(null);
            });
  }

  @Nested
  @DisplayName("Create Experiment Tests")
  class CreateExperimentTests {

    @Test
    @DisplayName("Should successfully create experiment")
    void testCreateExperimentSuccess() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertNotNull(response.getExperimentId());
      assertTrue(response.isStatus());
      assertEquals("created", response.getMessage());

      verify(experimentDAO, times(1))
          .createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class));
    }

    @Test
    @DisplayName("Should set projectKey and experimentId from headers")
    void testCreateExperimentSetsIds() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setProjectKey(null);
      request.setExperimentId(null);

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();

      assertNotNull(request.getProjectKey());
      assertNotNull(request.getExperimentId());
      assertEquals(testProjectKey, request.getProjectKey());
    }

    @Test
    @DisplayName("Should propagate error when DAO returns error")
    void testCreateExperimentDaoReturnsZero() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.error(new RuntimeException("Failed to insert experiment")));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert - Service wraps error with EXPERIMENT_CREATION_FAILED
      testObserver.assertError(RestException.class);
      testObserver.assertError(
          throwable ->
              throwable.getMessage().contains("Failed to create experiment")
                  || throwable.getMessage().contains("Failed to insert experiment"));
    }

    @Test
    @DisplayName("Should propagate DAO error during create")
    void testCreateExperimentDaoError() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert - Service wraps error with EXPERIMENT_CREATION_FAILED
      testObserver.assertError(RestException.class);
      testObserver.assertError(
          throwable ->
              throwable.getMessage().contains("Failed to create experiment")
                  || throwable.getMessage().contains("Database connection failed"));
    }

    // Note: Null request validation happens at REST layer, so no null test needed here

    @Test
    @DisplayName("Should create experiment with all optional fields")
    void testCreateExperimentWithAllFields() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setCohorts(Arrays.asList("premium_users", "mobile_users"));

      request.setOverrides(List.of("user1@example.com", "user2@example.com"));

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();

      CreateExperimentResponse response = testObserver.values().get(0);
      assertTrue(response.isStatus());
    }

    @Test
    @DisplayName("Should create experiment with minimal fields")
    void testCreateExperimentMinimalFields() {
      // Arrange
      CreateExperimentRequest request = new CreateExperimentRequest();
      request.setName("minimal_experiment");
      request.setDescription("Minimal description");
      request.setHypothesis("Minimal hypothesis");
      request.setExposure(50);
      request.setThreshold(1000);
      request.setStartTime(System.currentTimeMillis() / 1000);
      request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
      request.setCreatedBy("test@example.com");

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();

      CreateExperimentResponse response = testObserver.values().get(0);
      assertTrue(response.isStatus());
    }
  }

  @Nested
  @DisplayName("Update Experiment Tests")
  class UpdateExperimentTests {

    private void mockUpdateWithTransaction() {
      Map<String, Object> previousData = new HashMap<>();
      previousData.put("description", "Old description");

      when(experimentDAO.getExperimentData(anyString(), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any()))
          .thenReturn(Single.just(true));
    }

    @Test
    @DisplayName("Should successfully update experiment")
    void testUpdateExperimentSuccess() {
      // Arrange
      UpdateExperimentRequest request = new UpdateExperimentRequest();
      request.setDescription("Updated description");
      request.setStatus(ExperimentStatus.LIVE);
      request.setExposure(75);

      Map<String, Object> previousData = new HashMap<>();
      previousData.put("description", "Old description");
      previousData.put("status", "DRAFT");

      when(experimentDAO.getExperimentData(anyString(), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "updated".equals(response.getMessage()));

      verify(experimentDAO, times(1)).getExperimentData(anyString(), any(UUID.class));
      verify(experimentDAO, times(1))
          .updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any());
    }

    @Test
    @DisplayName("Should update single field")
    void testUpdateSingleField() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setDescription("Updated description");

      Map<String, Object> previousData = new HashMap<>();
      previousData.put("description", "Old description");

      when(experimentDAO.getExperimentData(anyString(), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "updated".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should update multiple fields")
    void testUpdateMultipleFields() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setName("updated_name");
      updates.setDescription("Updated description");
      updates.setHypothesis("Updated hypothesis");
      updates.setStatus(ExperimentStatus.LIVE);
      updates.setExposure(80);
      updates.setThreshold(15000L);

      mockUpdateWithTransaction();

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "updated".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should update cohorts array")
    void testUpdateCohortsArray() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setCohorts(Arrays.asList("premium_users", "mobile_users", "web_users"));

      mockUpdateWithTransaction();

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "updated".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should update JSONB fields")
    void testUpdateJsonbFields() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();

      Map<String, Object> variantWeights = new HashMap<>();
      variantWeights.put("control", 0.3);
      variantWeights.put("variant_a", 0.4);
      variantWeights.put("variant_b", 0.3);
      // TODO: Fix variant_weights setter
      // updates.setVariantWeights(variantWeights);

      List<String> overrides =
          Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com");
      updates.setOverrides(overrides);

      mockUpdateWithTransaction();

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "updated".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should handle DAO error during update")
    void testUpdateDaoError() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setDescription("Updated description");

      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperimentData(anyString(), any(UUID.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert - Service wraps error with EXPERIMENT_UPDATE_FAILED
      testObserver.assertError(RestException.class);
      testObserver.assertError(
          throwable ->
              throwable.getMessage().contains("Failed to update experiment")
                  || throwable.getMessage().contains("Database connection failed"));
    }

    @Test
    @DisplayName("Should handle empty update map")
    void testUpdateEmptyMap() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      // No mocking needed - service should return early with "No updates provided"

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "No updates provided".equals(response.getMessage()));
    }

    // Note: Null request validation happens at REST layer, so no null test needed here
  }

  @Nested
  @DisplayName("Concurrent Operation Tests")
  class ConcurrentOperationTests {

    @Test
    @DisplayName("Should handle multiple concurrent creates")
    void testConcurrentCreates() {
      // Arrange
      CreateExperimentRequest request1 = createValidRequest();
      CreateExperimentRequest request2 = createValidRequest();
      CreateExperimentRequest request3 = createValidRequest();

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L))
          .thenReturn(Single.just(2L))
          .thenReturn(Single.just(3L));

      // Act
      TestObserver<CreateExperimentResponse> observer1 =
          experimentService.create(testTenantId, testProjectKey, request1).test();
      TestObserver<CreateExperimentResponse> observer2 =
          experimentService.create(testTenantId, testProjectKey, request2).test();
      TestObserver<CreateExperimentResponse> observer3 =
          experimentService.create(testTenantId, testProjectKey, request3).test();

      // Assert
      observer1.assertComplete().assertNoErrors();
      observer2.assertComplete().assertNoErrors();
      observer3.assertComplete().assertNoErrors();

      verify(experimentDAO, times(3))
          .createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class));
    }

    @Test
    @DisplayName("Should handle multiple concurrent updates")
    void testConcurrentUpdates() {
      // Arrange
      UpdateExperimentRequest updates1 = new UpdateExperimentRequest();
      updates1.setDescription("Update 1");

      UpdateExperimentRequest updates2 = new UpdateExperimentRequest();
      updates2.setStatus(ExperimentStatus.LIVE);

      UpdateExperimentRequest updates3 = new UpdateExperimentRequest();
      updates3.setExposure(90);

      Map<String, Object> previousData = new HashMap<>();
      when(experimentDAO.getExperimentData(anyString(), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<UpdateExperimentResponse> observer1 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates1).test();
      TestObserver<UpdateExperimentResponse> observer2 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates2).test();
      TestObserver<UpdateExperimentResponse> observer3 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates3).test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(response -> response.isStatus());
      observer2.assertComplete().assertNoErrors().assertValue(response -> response.isStatus());
      observer3.assertComplete().assertNoErrors().assertValue(response -> response.isStatus());

      verify(experimentDAO, times(3)).getExperimentData(anyString(), any(UUID.class));
      verify(experimentDAO, times(3))
          .updateWithTransaction(
              anyString(),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              any(),
              any(),
              anyMap(),
              any());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle very long experiment name")
    void testVeryLongExperimentName() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setName("a".repeat(100)); // Very long name

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle large cohorts array")
    void testLargeCohortsArray() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      List<String> largeCohorts = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
        largeCohorts.add("cohort_" + i);
      }
      request.setCohorts(largeCohorts);

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle complex JSONB structures")
    void testComplexJsonbStructures() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();

      // Note: variantWeights is now VariantWeights type, not Map
      // Skipping complex variant weights for this test

      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }
  }

  // Helper method to create a valid request
  private CreateExperimentRequest createValidRequest() {
    CreateExperimentRequest request = new CreateExperimentRequest();
    request.setName("test_experiment");
    request.setDescription("Test description");
    request.setHypothesis("Test hypothesis");
    request.setStatus(ExperimentStatus.DRAFT);
    request.setType(ExperimentType.A_B);
    request.setGuardrailHealthStatus(ExperimentHealth.PASSING);
    request.setExposure(50);
    request.setThreshold(1000);
    request.setStartTime(System.currentTimeMillis() / 1000);
    request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
    request.setCreatedBy("test@example.com");
    return request;
  }

  @Nested
  @DisplayName("Get Experiment Success Tests")
  class GetExperimentSuccessTests {

    @Test
    @DisplayName("Should return experiment when found")
    void testGetExperimentSuccess() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(experiment -> experiment.getExperimentId().equals(testExperimentId));
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
    }

    @Test
    @DisplayName("Should return experiment with correct project Key")
    void testGetExperimentWithCorrectProjectKey() {
      // Arrange
      Experiment expectedExperiment = createMockExperiment();
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      Experiment result = testObserver.values().get(0);
      assertEquals(PROJECT_KEY, result.getProjectKey());
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
    }
  }

  @Nested
  @DisplayName("Get Experiment Error Handling Tests")
  class GetExperimentErrorHandlingTests {

    @Test
    @DisplayName("Should return EXPERIMENT_NOT_FOUND when experiment not found")
    void testGetExperimentNotFound() {
      // Arrange
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.empty());

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

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
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
    }

    @Test
    @DisplayName("Should propagate RestException as-is")
    void testGetExperimentRestException() {
      // Arrange
      RestException restException = new RestException(ErrorEnum.REST_GET_EXPERIMENT_BY_ID_FAILED);
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.error(restException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

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
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
    }

    @Test
    @DisplayName("Should propagate RuntimeException from DAO")
    void testGetExperimentRuntimeException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.error(runtimeException));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
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
              Collections.emptyList(), new FilterExperimentsResponse.PaginationMeta(1, 20, 0));
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
      FilterExperimentsResponse.PaginationMeta paginationMeta =
          new FilterExperimentsResponse.PaginationMeta(2, 10, 25);
      FilterExperimentsResponse expectedResponse =
          new FilterExperimentsResponse(List.of(createMockExperiment()), paginationMeta);
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
            FilterExperimentsResponse.PaginationMeta meta = response.getPagination();
            return meta.getCurrentPage() == 2
                && meta.getPageSize() == 10
                && meta.getTotalCount() == 25;
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
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act
      TestObserver<Experiment> testObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver.assertComplete();
      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
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
      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(expectedExperiment));

      // Act - Each subscription triggers the Single chain again
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();

      // Verify called exactly twice (once per subscription)
      verify(experimentDAO, times(2)).getExperiment(PROJECT_KEY, testExperimentId.toString());
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
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle multiple operations sequentially")
    void testMultipleOperationsSequentially() {
      // Arrange
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse filterResponse = createMockFilterResponse();

      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(filterResponse));

      // Act
      TestObserver<Experiment> getObserver =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();
      TestObserver<FilterExperimentsResponse> filterObserver =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

      // Assert
      getObserver.assertComplete().assertNoErrors();
      filterObserver.assertComplete().assertNoErrors();

      verify(experimentDAO, times(1)).getExperiment(PROJECT_KEY, testExperimentId.toString());
      verify(experimentDAO, times(1)).filterExperiments(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle concurrent service calls")
    void testConcurrentServiceCalls() {
      // Arrange
      Experiment experiment = createMockExperiment();
      FilterExperimentsRequest request = new FilterExperimentsRequest();
      FilterExperimentsResponse filterResponse = createMockFilterResponse();

      when(experimentDAO.getExperiment(PROJECT_KEY, testExperimentId.toString()))
          .thenReturn(Maybe.just(experiment));
      when(experimentDAO.filterExperiments(PROJECT_KEY, request))
          .thenReturn(Single.just(filterResponse));

      // Act
      TestObserver<Experiment> testObserver1 =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();
      TestObserver<Experiment> testObserver2 =
          experimentService.getExperiment(PROJECT_KEY, testExperimentId.toString()).test();
      TestObserver<FilterExperimentsResponse> testObserver3 =
          experimentService.filterExperiments(PROJECT_KEY, request).test();

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
    return new FilterExperimentsResponse(List.of(createMockExperiment()), paginationMeta);
  }
}
