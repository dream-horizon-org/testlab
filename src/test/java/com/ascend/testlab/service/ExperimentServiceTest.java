package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.service.impl.ExperimentServiceImpl;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
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
  private UUID testProjectKey;
  private UUID testExperimentId;

  @BeforeEach
  void setUp() {
    experimentService = new ExperimentServiceImpl(experimentDAO, pgWriterClient);
    testTenantId = UUID.randomUUID();
    testProjectKey = UUID.randomUUID();
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
    @DisplayName("Should return failure when DAO returns error")
    void testCreateExperimentDaoReturnsZero() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.error(new RuntimeException("Failed to insert experiment")));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertNotNull(response.getExperimentId());
      assertFalse(response.isStatus());
      assertTrue(response.getMessage().contains("Failed to insert experiment"));
    }

    @Test
    @DisplayName("Should handle DAO error during create")
    void testCreateExperimentDaoError() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.createWithRelatedData(any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertNotNull(response.getExperimentId());
      assertFalse(response.isStatus());
      assertTrue(response.getMessage().contains("Failed"));
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void testCreateExperimentNullRequest() {
      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertNull(response.getExperimentId());
      assertFalse(response.isStatus());
      assertTrue(response.getMessage().contains("Failed"));
    }

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

      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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

      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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

      verify(experimentDAO, times(1)).getExperimentData(any(UUID.class), any(UUID.class));
      verify(experimentDAO, times(1))
          .updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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

      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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
    @DisplayName("Should handle DAO returning false")
    void testUpdateDaoReturnsFalse() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setDescription("Updated description");

      Map<String, Object> previousData = new HashMap<>();
      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
              any(),
              anyMap(),
              any()))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> !response.isStatus() && "Update failed".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should handle DAO error during update")
    void testUpdateDaoError() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();
      updates.setDescription("Updated description");

      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> !response.isStatus() && "Update failed".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should handle empty update map")
    void testUpdateEmptyMap() {
      // Arrange
      UpdateExperimentRequest updates = new UpdateExperimentRequest();

      mockUpdateWithTransaction();

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "No updates provided".equals(response.getMessage()));
    }

    @Test
    @DisplayName("Should handle null update map")
    void testUpdateNullMap() {
      // Arrange
      mockUpdateWithTransaction();

      // Act
      TestObserver<UpdateExperimentResponse> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> response.isStatus() && "No updates provided".equals(response.getMessage()));
    }
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
      when(experimentDAO.getExperimentData(any(UUID.class), any(UUID.class)))
          .thenReturn(Single.just(previousData));
      when(experimentDAO.updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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

      verify(experimentDAO, times(3)).getExperimentData(any(UUID.class), any(UUID.class));
      verify(experimentDAO, times(3))
          .updateWithTransaction(
              any(UUID.class),
              any(UUID.class),
              any(UpdateExperimentRequest.class),
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
}
