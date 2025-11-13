package com.ascend.testlab.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentStrategy;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.impl.ExperimentDAOImpl;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.sqlclient.Tuple;
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
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("ExperimentDAO Tests")
public class ExperimentDAOTest {

  @Mock private PgWriterClient pgWriterClient;
  @Mock private PgReaderClient pgReaderClient;

  private ExperimentDAO experimentDAO;

  private UUID testTenantId;
  private UUID testProjectKey;
  private UUID testExperimentId;

  @BeforeEach
  void setUp() {
    experimentDAO = new ExperimentDAOImpl(pgWriterClient, pgReaderClient);
    testTenantId = UUID.randomUUID();
    testProjectKey = UUID.randomUUID();
    testExperimentId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("Create Experiment Tests")
  class CreateExperimentTests {

    @Test
    @DisplayName("Should successfully create experiment with all fields")
    void testCreateExperimentSuccess() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(1L);
      verify(pgWriterClient, times(1)).execute(anyString(), any(Tuple.class));
    }

    @Test
    @DisplayName("Should create experiment with minimal required fields")
    void testCreateExperimentMinimalFields() {
      // Arrange
      CreateExperimentRequest request = new CreateExperimentRequest();
      request.setProjectKey(testProjectKey);
      request.setExperimentId(testExperimentId);
      request.setName("minimal_experiment");
      request.setDescription("Minimal description");
      request.setHypothesis("Minimal hypothesis");
      request.setExposure(50);
      request.setThreshold(1000);
      request.setStartTime(System.currentTimeMillis() / 1000);
      request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
      request.setCreatedBy("test@example.com");

      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(1L);
    }

    @Test
    @DisplayName("Should handle database insert failure")
    void testCreateExperimentInsertFailure() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(false));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(0L);
    }

    @Test
    @DisplayName("Should handle database error during insert")
    void testCreateExperimentDatabaseError() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      RuntimeException exception = new RuntimeException("Database connection failed");
      when(pgWriterClient.execute(anyString(), any(Tuple.class)))
          .thenReturn(Single.error(exception));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(0L);
    }

    @Test
    @DisplayName("Should handle experiment with cohorts array")
    void testCreateExperimentWithCohorts() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      request.setCohorts(Arrays.asList("premium_users", "mobile_users", "web_users"));
      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(1L);
    }

    @Test
    @DisplayName("Should handle experiment with JSONB fields")
    void testCreateExperimentWithJsonbFields() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();

      // Note: variantWeights is now VariantWeights type, not Map
      // Skipping variant weights for this test

      request.setOverrides("user1@example.com,user2@example.com");

      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(1L);
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void testCreateExperimentWithNullFields() {
      // Arrange
      CreateExperimentRequest request = new CreateExperimentRequest();
      request.setProjectKey(testProjectKey);
      request.setExperimentId(testExperimentId);
      request.setName("test_experiment");
      request.setDescription("Test description");
      request.setHypothesis("Test hypothesis");
      request.setStatus(null);
      request.setType(null);
      request.setGuardrailHealthStatus(null);
      request.setCohorts(null);
      request.setVariantWeights(null);
      request.setAssignmentStrategy(null);
      request.setExposure(50);
      request.setThreshold(1000);
      request.setStartTime(System.currentTimeMillis() / 1000);
      request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
      request.setCreatedBy("test@example.com");

      when(pgWriterClient.execute(anyString(), any(Tuple.class))).thenReturn(Single.just(true));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(1L);
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
      updates.put("assignment_strategy", "RANDOM");
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
      // Create an object that can't be serialized to JSON (e.g., a Thread object)
      updates.put("variant_weights", new Thread());

      // Act
      TestObserver<Boolean> testObserver =
          experimentDAO.updatePartial(testProjectKey, testExperimentId, updates).test();

      // Assert - Should handle serialization error gracefully
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(false);
    }

    @Test
    @DisplayName("Should handle exception in create method")
    void testCreateExceptionHandling() {
      // Arrange
      CreateExperimentRequest request = createValidRequest();
      when(pgWriterClient.execute(anyString(), any(Tuple.class)))
          .thenThrow(new RuntimeException("Unexpected error"));

      // Act
      TestObserver<Long> testObserver =
          experimentDAO.create(testTenantId, testProjectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(0L);
    }
  }

  // Helper method to create a valid request
  private CreateExperimentRequest createValidRequest() {
    CreateExperimentRequest request = new CreateExperimentRequest();
    request.setProjectKey(testProjectKey);
    request.setExperimentId(testExperimentId);
    request.setName("test_experiment");
    request.setDescription("Test description");
    request.setHypothesis("Test hypothesis");
    request.setStatus(ExperimentStatus.DRAFT);
    request.setType(ExperimentType.A_B);
    request.setGuardrailHealthStatus(ExperimentHealth.PASSING);
    request.setCohorts(Arrays.asList("test_cohort"));
    request.setAssignmentStrategy(ExperimentStrategy.RANDOM);
    request.setExposure(50);
    request.setThreshold(1000);
    request.setStartTime(System.currentTimeMillis() / 1000);
    request.setEndTime(System.currentTimeMillis() / 1000 + 86400);
    request.setCreatedBy("test@example.com");
    return request;
  }
}
