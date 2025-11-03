package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.ExperimentHealth;
import com.ascend.testlab.constants.ExperimentStatus;
import com.ascend.testlab.constants.ExperimentStrategy;
import com.ascend.testlab.constants.ExperimentType;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.impl.ExperimentServiceImpl;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import java.util.*;
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

  private ExperimentService experimentService;

  private UUID testTenantId;
  private UUID testProjectKey;
  private UUID testExperimentId;

  @BeforeEach
  void setUp() {
    experimentService = new ExperimentServiceImpl(experimentDAO);
    testTenantId = UUID.randomUUID();
    testProjectKey = UUID.randomUUID();
    testExperimentId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("Create Experiment Tests")
  class CreateExperimentTests {

    @Test
    @DisplayName("Should successfully create experiment")
    void testCreateExperimentSuccess() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(1L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertEquals(1L, response.getId());
      assertTrue(response.isStatus());
      assertEquals("created", response.getMessage());

      verify(experimentDAO, times(1))
          .create(any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class));
    }

    @Test
    @DisplayName("Should set projectKey and experimentId from headers")
    void testCreateExperimentSetsIds() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setProjectKey(null);
      request.setExperimentId(null);

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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
    @DisplayName("Should return failure when DAO returns 0")
    void testCreateExperimentDaoReturnsZero() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.just(0L));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertEquals(0L, response.getId());
      assertFalse(response.isStatus());
      assertEquals("Failed to insert experiment", response.getMessage());
    }

    @Test
    @DisplayName("Should handle DAO error during create")
    void testCreateExperimentDaoError() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<CreateExperimentResponse> testObserver =
          experimentService.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);

      CreateExperimentResponse response = testObserver.values().get(0);
      assertEquals(0L, response.getId());
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
      assertEquals(0L, response.getId());
      assertFalse(response.isStatus());
      assertTrue(response.getMessage().contains("Failed"));
    }

    @Test
    @DisplayName("Should create experiment with all optional fields")
    void testCreateExperimentWithAllFields() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setCohorts(Arrays.asList("premium_users", "mobile_users"));

      Map<String, Object> variantWeights = new HashMap<>();
      variantWeights.put("control", 0.5);
      variantWeights.put("variant_a", 0.5);
      request.setVariantWeights(variantWeights);

      Map<String, Object> overrides = new HashMap<>();
      overrides.put("test_users", Arrays.asList("user1", "user2"));
      request.setOverrides(overrides);

      Map<String, Object> ruleAttributes = new HashMap<>();
      ruleAttributes.put("country", Arrays.asList("US", "CA"));
      request.setRuleAttributes(ruleAttributes);

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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

    @Test
    @DisplayName("Should successfully update experiment")
    void testUpdateExperimentSuccess() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");
      updates.put("status", "LIVE");
      updates.put("exposure", 75);

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);

      verify(experimentDAO, times(1)).updatePartial(any(UUID.class), any(UUID.class), anyMap());
    }

    @Test
    @DisplayName("Should update single field")
    void testUpdateSingleField() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should update multiple fields")
    void testUpdateMultipleFields() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("name", "updated_name");
      updates.put("description", "Updated description");
      updates.put("hypothesis", "Updated hypothesis");
      updates.put("status", "LIVE");
      updates.put("exposure", 80);
      updates.put("threshold", 15000L);

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

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
      updates.put("cohorts", Arrays.asList("premium_users", "mobile_users", "web_users"));

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

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
      variantWeights.put("variant_a", 0.4);
      variantWeights.put("variant_b", 0.3);
      updates.put("variant_weights", variantWeights);

      Map<String, Object> overrides = new HashMap<>();
      overrides.put("test_users", Arrays.asList("user1", "user2", "user3"));
      updates.put("overrides", overrides);

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should handle DAO returning false")
    void testUpdateDaoReturnsFalse() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle DAO error during update")
    void testUpdateDaoError() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();
      updates.put("description", "Updated description");

      RuntimeException exception = new RuntimeException("Database connection failed");
      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle empty update map")
    void testUpdateEmptyMap() {
      // Arrange
      Map<String, Object> updates = new HashMap<>();

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
    }

    @Test
    @DisplayName("Should handle null update map")
    void testUpdateNullMap() {
      // Arrange
      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), isNull()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> testObserver =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(true);
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

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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
          .create(any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class));
    }

    @Test
    @DisplayName("Should handle multiple concurrent updates")
    void testConcurrentUpdates() {
      // Arrange
      Map<String, Object> updates1 = new HashMap<>();
      updates1.put("description", "Update 1");

      Map<String, Object> updates2 = new HashMap<>();
      updates2.put("status", "LIVE");

      Map<String, Object> updates3 = new HashMap<>();
      updates3.put("exposure", 90);

      when(experimentDAO.updatePartial(any(UUID.class), any(UUID.class), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<Boolean> observer1 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates1).test();
      TestObserver<Boolean> observer2 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates2).test();
      TestObserver<Boolean> observer3 =
          experimentService.update(testTenantId, testProjectKey, testExperimentId, updates3).test();

      // Assert
      observer1.assertComplete().assertNoErrors().assertValue(true);
      observer2.assertComplete().assertNoErrors().assertValue(true);
      observer3.assertComplete().assertNoErrors().assertValue(true);

      verify(experimentDAO, times(3)).updatePartial(any(UUID.class), any(UUID.class), anyMap());
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

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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

      Map<String, Object> complexVariantWeights = new HashMap<>();
      for (int i = 0; i < 50; i++) {
        complexVariantWeights.put("variant_" + i, 0.02);
      }
      request.setVariantWeights(complexVariantWeights);

      when(experimentDAO.create(
              any(UUID.class), any(UUID.class), any(CreateExperimentRequest.class)))
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
    request.setAssignmentStrategy(ExperimentStrategy.RANDOM);
    request.setExposure(50);
    request.setThreshold(1000);
    request.setStartTime(System.currentTimeMillis() / 1000);
    request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
    request.setCreatedBy("test@example.com");
    return request;
  }
}
