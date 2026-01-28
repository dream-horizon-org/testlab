package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.experiment.WinningVariant;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.dto.response.AllocationsResponse;
import com.ascend.testlab.service.impl.AllocationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Maybe;
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

@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("AllocationService Reallocate Tests")
class AllocationServiceTest {

  private AllocationService allocationService;

  @Mock private AllocationDAO allocationDAO;
  @Mock private CohortService cohortService;

  private static final String PROJECT_KEY = "550e8400-e29b-41d4-a716-446655440001";
  private static final UUID EXPERIMENT_ID = UUID.fromString("7d0a4cca-b882-4092-bf8c-201a69a20f1b");
  private static final String USER_ID = "user-001";
  private static final String CONTROL_VARIANT = "control";
  private static final String TREATMENT_VARIANT = "treatment";

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    allocationService = new AllocationServiceImpl(allocationDAO, cohortService, objectMapper);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid dependencies")
    void testConstructorWithValidDependencies() {
      // Act
      AllocationServiceImpl service =
          new AllocationServiceImpl(allocationDAO, cohortService, new ObjectMapper());

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should handle null ObjectMapper")
    void testConstructorWithNullObjectMapper() {
      // Act
      AllocationServiceImpl service = new AllocationServiceImpl(allocationDAO, cohortService, null);

      // Assert
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Reallocation Success Tests")
  class ReallocationSuccessTests {

    @Test
    @DisplayName("Should successfully reallocate user to treatment variant")
    void testSuccessfulReallocationToTreatment() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("VIP upgrade")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(EXPERIMENT_ID.toString())))
          .thenReturn(Single.just(createMockExperiment()));
      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), eq(CONTROL_VARIANT), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      testObserver.assertValue(result -> result.getVariantName().equals(TREATMENT_VARIANT));
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(PROJECT_KEY), eq(CONTROL_VARIANT), any(UserExperimentMap.class), eq(request));
    }

    @Test
    @DisplayName("Should reallocate with reason captured for audit")
    void testReallocationWithReasonCaptured() {
      // Arrange
      String auditReason = "A/B test - statistically significant";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason(auditReason)
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(PROJECT_KEY),
              anyString(),
              any(UserExperimentMap.class),
              argThat(req -> req.getReason() != null && req.getReason().equals(auditReason)));
    }

    @Test
    @DisplayName("Should reallocate with variant count updates")
    void testReallocationWithVariantCountUpdates() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), eq(CONTROL_VARIANT), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          result ->
              result.getVariantName().equals(TREATMENT_VARIANT)
                  && result.getStatus().equals("REALLOCATED"));
    }

    @Test
    @DisplayName("Should maintain experiment ID consistency after reallocation")
    void testExperimentIdConsistencyAfterReallocation() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.getExperimentId().equals(EXPERIMENT_ID));
    }
  }

  @Nested
  @DisplayName("Reallocation Error Handling Tests")
  class ReallocationErrorHandlingTests {

    @Test
    @DisplayName("Should throw error when experiment not found")
    void testExperimentNotFoundError() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(UUID.randomUUID().toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      lenient()
          .when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(request.getExperimentId())))
          .thenReturn(Single.error(new RuntimeException("Experiment not found")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should throw error when variant not found")
    void testVariantNotFoundError() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName("non-existent")
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      // Variant won't exist in the mock experiment's variants map

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should handle invalid experiment ID format")
    void testInvalidExperimentIdFormat() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId("invalid-uuid")
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      lenient()
          .when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      lenient()
          .when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), anyString()))
          .thenReturn(Single.just(createMockExperiment()));
      // UUID parsing error will occur before/during performReallocation

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
      testObserver.assertNotComplete();
    }

    @Test
    @DisplayName("Should handle database errors during reallocation")
    void testDatabaseErrorDuringReallocation() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.error(new RuntimeException("Database connection failed")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
      testObserver.assertValueCount(0);
    }

    @Test
    @DisplayName("Should handle timeout errors during reallocation")
    void testTimeoutErrorDuringReallocation() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.error(new RuntimeException("Request timed out")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      testObserver.assertNotComplete();
    }
  }

  @Nested
  @DisplayName("Request Parameter Validation Tests")
  class RequestParameterValidationTests {

    @Test
    @DisplayName("Should accept valid reallocation request")
    void testValidReallocationRequest() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Valid request")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
    }

    @Test
    @DisplayName("Should accept request with null reason")
    void testRequestWithNullReason() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason(null)
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should accept request with empty reason string")
    void testRequestWithEmptyReason() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle long reason strings")
    void testRequestWithLongReason() {
      // Arrange
      String longReason = "A".repeat(1000);
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason(longReason)
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }
  }

  @Nested
  @DisplayName("Project Key Handling Tests")
  class ProjectKeyHandlingTests {

    @Test
    @DisplayName("Should correctly use project key in reallocation")
    void testProjectKeyUsedInReallocation() {
      // Arrange
      String projectKey = "test-project-key";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(projectKey)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(projectKey)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(projectKey), eq(request.getExperimentId())))
          .thenReturn(Single.just(createMockExperiment()));
      when(allocationDAO.getAllocations(eq(request.getUserId()), eq(projectKey)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));
      when(allocationDAO.reallocateUserVariant(
              eq(projectKey), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(projectKey, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(projectKey), anyString(), any(UserExperimentMap.class), eq(request));
    }

    @Test
    @DisplayName("Should handle different project keys")
    void testDifferentProjectKeys() {
      // Arrange
      String projectKey1 = "project-1";
      String projectKey2 = "project-2";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      // Setup mocks for projectKey1
      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(projectKey1)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(projectKey1)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(projectKey1), eq(request.getExperimentId())))
          .thenReturn(Single.just(createMockExperiment()));
      when(allocationDAO.getAllocations(eq(request.getUserId()), eq(projectKey1)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));

      // Setup mocks for projectKey2
      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(projectKey2)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(projectKey2)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(projectKey2), eq(request.getExperimentId())))
          .thenReturn(Single.just(createMockExperiment()));
      when(allocationDAO.getAllocations(eq(request.getUserId()), eq(projectKey2)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));

      when(allocationDAO.reallocateUserVariant(
              anyString(), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver1 =
          allocationService.reallocateExperiment(projectKey1, request).test();
      TestObserver<UserExperimentMap> testObserver2 =
          allocationService.reallocateExperiment(projectKey2, request).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(projectKey1), anyString(), any(UserExperimentMap.class), eq(request));
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(projectKey2), anyString(), any(UserExperimentMap.class), eq(request));
    }
  }

  @Nested
  @DisplayName("Concurrent Reallocation Tests")
  class ConcurrentReallocationTests {

    @Test
    @DisplayName("Should handle concurrent reallocation requests for same user")
    void testConcurrentReallocationsSameUser() {
      // Arrange
      String variant1 = "variant1";
      String variant2 = "variant2";

      ReallocateRequest request1 =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(variant1)
              .userId(USER_ID)
              .reason("First")
              .build();

      ReallocateRequest request2 =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(variant2)
              .userId(USER_ID)
              .reason("Second")
              .build();

      UserExperimentMap response1 =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(variant1)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      UserExperimentMap response2 =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(variant2)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      // Create experiment with variant1 and variant2
      Experiment mockExperiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .experimentKey("test_experiment")
              .status(ExperimentStatus.LIVE)
              .type(ExperimentType.A_B)
              .variants(
                  Map.of(
                      CONTROL_VARIANT,
                      Variant.builder().displayName(CONTROL_VARIANT).build(),
                      variant1,
                      Variant.builder().displayName(variant1).build(),
                      variant2,
                      Variant.builder().displayName(variant2).build()))
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(EXPERIMENT_ID.toString())))
          .thenReturn(Single.just(mockExperiment));
      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request1)))
          .thenReturn(Single.just(response1));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request2)))
          .thenReturn(Single.just(response2));

      // Act
      TestObserver<UserExperimentMap> testObserver1 =
          allocationService.reallocateExperiment(PROJECT_KEY, request1).test();
      TestObserver<UserExperimentMap> testObserver2 =
          allocationService.reallocateExperiment(PROJECT_KEY, request2).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testObserver1.assertNoErrors();
      testObserver2.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle concurrent reallocations for different users")
    void testConcurrentReallocationsDifferentUsers() {
      // Arrange
      String userId1 = "user-1";
      String userId2 = "user-2";

      ReallocateRequest request1 =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(userId1)
              .reason("First user")
              .build();

      ReallocateRequest request2 =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(userId2)
              .reason("Second user")
              .build();

      UserExperimentMap response =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      // Setup mocks for user1
      when(allocationDAO.acquireUserLock(eq(userId1), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(userId1), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.getAllocations(eq(userId1), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));

      // Setup mocks for user2
      when(allocationDAO.acquireUserLock(eq(userId2), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(userId2), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.getAllocations(eq(userId2), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));

      // Shared stub for experiment (same for both requests)
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(EXPERIMENT_ID.toString())))
          .thenReturn(Single.just(createMockExperiment()));

      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY),
              anyString(),
              any(UserExperimentMap.class),
              any(ReallocateRequest.class)))
          .thenReturn(Single.just(response));

      // Act
      TestObserver<UserExperimentMap> testObserver1 =
          allocationService.reallocateExperiment(PROJECT_KEY, request1).test();
      TestObserver<UserExperimentMap> testObserver2 =
          allocationService.reallocateExperiment(PROJECT_KEY, request2).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
    }
  }

  @Nested
  @DisplayName("Response Structure Tests")
  class ResponseStructureTests {

    @Test
    @DisplayName("Should return UserExperimentMap with correct structure")
    void testResponseStructure() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          result -> {
            assertNotNull(result.getExperimentId());
            assertNotNull(result.getVariantName());
            assertNotNull(result.getStatus());
            assertNotNull(result.getAssignedAt());
            return true;
          });
    }

    @Test
    @DisplayName("Should preserve variant name in response")
    void testPreserveVariantNameInResponse() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.getVariantName().equals(TREATMENT_VARIANT));
    }

    @Test
    @DisplayName("Should set status to REALLOCATED")
    void testStatusSetToReallocated() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.getStatus().equals("REALLOCATED"));
    }
  }

  @Nested
  @DisplayName("Audit Trail Tests")
  class AuditTrailTests {

    @Test
    @DisplayName("Should pass audit reason to DAO")
    void testAuditReasonPassedToDAO() {
      // Arrange
      String reason = "Manual reassignment - premium tier";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason(reason)
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      verify(allocationDAO, times(1))
          .reallocateUserVariant(
              eq(PROJECT_KEY),
              anyString(),
              any(UserExperimentMap.class),
              argThat(req -> req.getReason().equals(reason)));
    }

    @Test
    @DisplayName("Should handle special characters in audit reason")
    void testSpecialCharactersInAuditReason() {
      // Arrange
      String reason = "Special chars: @#$%^&*()_+-={}[]|:;<>?,./";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason(reason)
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }
  }

  @Nested
  @DisplayName("Edge Cases Tests")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle reallocating to control variant")
    void testReallocationToControlVariant() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(CONTROL_VARIANT)
              .userId(USER_ID)
              .reason("Return to control")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(CONTROL_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, TREATMENT_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(result -> result.getVariantName().equals(CONTROL_VARIANT));
    }

    @Test
    @DisplayName("Should handle reallocation with hyphenated variant names")
    void testHyphenatedVariantNames() {
      // Arrange
      String hyphenatedVariant = "variant-with-hyphen";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(hyphenatedVariant)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(hyphenatedVariant)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      // Create experiment with hyphenated variant
      Experiment mockExperiment =
          Experiment.builder()
              .experimentId(EXPERIMENT_ID)
              .projectKey(PROJECT_KEY)
              .name("Test Experiment")
              .experimentKey("test_experiment")
              .status(ExperimentStatus.LIVE)
              .type(ExperimentType.A_B)
              .variants(
                  Map.of(
                      CONTROL_VARIANT,
                      Variant.builder().displayName(CONTROL_VARIANT).build(),
                      hyphenatedVariant,
                      Variant.builder().displayName(hyphenatedVariant).build()))
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(request.getExperimentId())))
          .thenReturn(Single.just(mockExperiment));
      when(allocationDAO.getAllocations(eq(request.getUserId()), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of(createCurrentAssignment(CONTROL_VARIANT))));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should handle reallocation with numeric user IDs")
    void testNumericUserIds() {
      // Arrange
      String numericUserId = "12345";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(numericUserId)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }
  }

  @Nested
  @DisplayName("GetAllocations Service Tests")
  class GetAllocationsServiceTests {

    @Test
    @DisplayName("Should get allocations for user successfully")
    void testGetAllocationsSuccess() {
      // Arrange
      List<UserExperimentMap> allocations =
          List.of(
              createCurrentAssignment(CONTROL_VARIANT), createCurrentAssignment(TREATMENT_VARIANT));

      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(allocations));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.getAllocations(USER_ID, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(response -> response.experimentMap().size() == 2);
      verify(allocationDAO, times(1)).getAllocations(eq(USER_ID), eq(PROJECT_KEY));
    }

    @Test
    @DisplayName("Should get empty allocations when user has none")
    void testGetAllocationsEmpty() {
      // Arrange
      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of()));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.getAllocations(USER_ID, PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(response -> response.experimentMap().isEmpty());
    }

    @Test
    @DisplayName("Should handle error when fetching allocations")
    void testGetAllocationsError() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Database error");
      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.error(expectedException));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.getAllocations(USER_ID, PROJECT_KEY).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle different user IDs correctly")
    void testGetAllocationsDifferentUsers() {
      // Arrange
      String userId1 = "user-1";
      String userId2 = "user-2";

      List<UserExperimentMap> allocations1 = List.of(createCurrentAssignment(CONTROL_VARIANT));
      List<UserExperimentMap> allocations2 = List.of(createCurrentAssignment(TREATMENT_VARIANT));

      when(allocationDAO.getAllocations(eq(userId1), eq(PROJECT_KEY)))
          .thenReturn(Single.just(allocations1));
      when(allocationDAO.getAllocations(eq(userId2), eq(PROJECT_KEY)))
          .thenReturn(Single.just(allocations2));

      // Act
      TestObserver<AllocationsResponse> testObserver1 =
          allocationService.getAllocations(userId1, PROJECT_KEY).test();
      TestObserver<AllocationsResponse> testObserver2 =
          allocationService.getAllocations(userId2, PROJECT_KEY).test();

      // Assert
      testObserver1.assertComplete();
      testObserver2.assertComplete();
      testObserver1.assertValue(response -> response.experimentMap().size() == 1);
      testObserver2.assertValue(response -> response.experimentMap().size() == 1);
    }
  }

  @Nested
  @DisplayName("Lock Acquisition Tests")
  class LockAcquisitionTests {

    @Test
    @DisplayName("Should fail reallocation when lock cannot be acquired")
    void testLockAcquisitionFailure() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
      testObserver.assertNotComplete();
    }

    @Test
    @DisplayName("Should handle lock acquisition error")
    void testLockAcquisitionError() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.error(new RuntimeException("Lock service unavailable")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should release lock after successful reallocation")
    void testLockReleaseAfterSuccess() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      UserExperimentMap expectedResponse =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.just(expectedResponse));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      verify(allocationDAO, times(1)).releaseUserLock(eq(USER_ID), eq(PROJECT_KEY));
    }

    @Test
    @DisplayName("Should release lock after reallocation failure")
    void testLockReleaseAfterFailure() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY), anyString(), any(UserExperimentMap.class), eq(request)))
          .thenReturn(Single.error(new RuntimeException("Reallocation failed")));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(RuntimeException.class);
      verify(allocationDAO, times(1)).releaseUserLock(eq(USER_ID), eq(PROJECT_KEY));
    }
  }

  @Nested
  @DisplayName("Variant Assignment Validation Tests")
  class VariantAssignmentValidationTests {

    @Test
    @DisplayName("Should reject reallocation to same variant")
    void testReallocationToSameVariant() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(CONTROL_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
    }

    @Test
    @DisplayName("Should reject reallocation when user has no current assignment")
    void testReallocationWithNoCurrentAssignment() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(TREATMENT_VARIANT)
              .userId(USER_ID)
              .reason("Test")
              .build();

      when(allocationDAO.acquireUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.releaseUserLock(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(true));
      when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(request.getExperimentId())))
          .thenReturn(Single.just(createMockExperiment()));
      when(allocationDAO.getAllocations(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Single.just(List.of()));

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
    }

    @Test
    @DisplayName("Should handle case-sensitive variant names")
    void testVariantNameCaseSensitivity() {
      // Arrange
      String variantUpperCase = "TREATMENT";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .experimentId(EXPERIMENT_ID.toString())
              .variantName(variantUpperCase)
              .userId(USER_ID)
              .reason("Test")
              .build();

      setupCommonMocks(request, CONTROL_VARIANT);

      // Act
      TestObserver<UserExperimentMap> testObserver =
          allocationService.reallocateExperiment(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertError(Exception.class);
    }
  }

  @Nested
  @DisplayName("Apply Overrides Tests")
  class ApplyOverridesTests {

    @Test
    @DisplayName("Should apply overrides for new users without existing allocations")
    void testApplyOverridesNewUsers() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = createMockOverrides();

      UserExperimentMap newAllocation =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(CONTROL_VARIANT)
              .status("ASSIGNED")
              .assignedAt(System.currentTimeMillis())
              .build();

      // No existing allocations
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(new HashMap<>()));
      when(allocationDAO.insertAllocationsAndIncrementCounts(
              anyString(), eq(PROJECT_KEY), anyList(), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Should reallocate existing users to new variant")
    void testApplyOverridesExistingUsersReallocation() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(TREATMENT_VARIANT, List.of(USER_ID)); // Move user to treatment
      overrides.setOverrideIds(overrideIds);

      // User currently in control
      UserExperimentMap existingAllocation = createCurrentAssignment(CONTROL_VARIANT);
      Map<String, List<UserExperimentMap>> existingAllocations = new HashMap<>();
      existingAllocations.put(USER_ID, List.of(existingAllocation));

      UserExperimentMap reallocatedAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(existingAllocations));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY),
              eq(CONTROL_VARIANT),
              any(UserExperimentMap.class),
              any(ReallocateRequest.class)))
          .thenReturn(Single.just(reallocatedAssignment));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> !results.isEmpty());
    }

    @Test
    @DisplayName("Should skip users already in target variant")
    void testApplyOverridesSkipSameVariant() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(CONTROL_VARIANT, List.of(USER_ID)); // User already in control
      overrides.setOverrideIds(overrideIds);

      // User already in control
      UserExperimentMap existingAllocation = createCurrentAssignment(CONTROL_VARIANT);
      Map<String, List<UserExperimentMap>> existingAllocations = new HashMap<>();
      existingAllocations.put(USER_ID, List.of(existingAllocation));

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(existingAllocations));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      // Should return existing allocation, not reallocate
      testObserver.assertValue(results -> !results.isEmpty());
      // No reallocation should be called
      verify(allocationDAO, never())
          .reallocateUserVariant(
              anyString(), anyString(), any(UserExperimentMap.class), any(ReallocateRequest.class));
      // No new allocation should be created
      verify(allocationDAO, never())
          .insertAllocationsAndIncrementCounts(anyString(), anyString(), anyList(), anyMap());
    }

    @Test
    @DisplayName("Should return empty list when overrides is null")
    void testApplyOverridesNullOverrides() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, null).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.isEmpty());
      verify(allocationDAO, never()).getAllocations(anyList(), anyString());
    }

    @Test
    @DisplayName("Should return empty list when override IDs is null")
    void testApplyOverridesNullOverrideIds() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      overrides.setOverrideIds(null);

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.isEmpty());
      verify(allocationDAO, never()).getAllocations(anyList(), anyString());
    }

    @Test
    @DisplayName("Should return empty list when override IDs is empty")
    void testApplyOverridesEmptyOverrideIds() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      overrides.setOverrideIds(new HashMap<>());

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.isEmpty());
      verify(allocationDAO, never()).getAllocations(anyList(), anyString());
    }

    @Test
    @DisplayName("Should skip invalid variant names in overrides")
    void testApplyOverridesInvalidVariant() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put("non_existent_variant", List.of(USER_ID));
      overrides.setOverrideIds(overrideIds);

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.isEmpty());
      // No allocation operations should be performed for invalid variant
      verify(allocationDAO, never())
          .insertAllocationsAndIncrementCounts(anyString(), anyString(), anyList(), anyMap());
    }

    @Test
    @DisplayName("Should skip null or blank user IDs in overrides")
    void testApplyOverridesNullOrBlankUserIds() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(CONTROL_VARIANT, Arrays.asList(null, "", "  "));
      overrides.setOverrideIds(overrideIds);

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(new HashMap<>()));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.isEmpty());
    }

    @Test
    @DisplayName("Should apply overrides for multiple variants")
    void testApplyOverridesMultipleVariants() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(CONTROL_VARIANT, List.of("user1", "user2"));
      overrideIds.put(TREATMENT_VARIANT, List.of("user3", "user4"));
      overrides.setOverrideIds(overrideIds);

      UserExperimentMap newAllocationControl =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(CONTROL_VARIANT)
              .status("ASSIGNED")
              .assignedAt(System.currentTimeMillis())
              .build();

      UserExperimentMap newAllocationTreatment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("ASSIGNED")
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(new HashMap<>()));
      when(allocationDAO.insertAllocationsAndIncrementCounts(
              anyString(), eq(PROJECT_KEY), anyList(), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.size() == 4);
    }

    @Test
    @DisplayName("Should handle mixed new and existing users")
    void testApplyOverridesMixedUsers() {
      // Arrange
      Experiment experiment = createMockExperimentWithVariants();
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(TREATMENT_VARIANT, List.of("existing_user", "new_user"));
      overrides.setOverrideIds(overrideIds);

      // existing_user is currently in CONTROL
      UserExperimentMap existingAllocation =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(CONTROL_VARIANT)
              .status("ASSIGNED")
              .assignedAt(System.currentTimeMillis())
              .build();

      Map<String, List<UserExperimentMap>> existingAllocations = new HashMap<>();
      existingAllocations.put("existing_user", List.of(existingAllocation));
      // new_user has no allocations

      UserExperimentMap reallocatedAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("REALLOCATED")
              .assignedAt(System.currentTimeMillis())
              .build();

      UserExperimentMap newAssignment =
          UserExperimentMap.builder()
              .experimentId(EXPERIMENT_ID)
              .variantName(TREATMENT_VARIANT)
              .status("ASSIGNED")
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(existingAllocations));
      when(allocationDAO.reallocateUserVariant(
              eq(PROJECT_KEY),
              eq(CONTROL_VARIANT),
              any(UserExperimentMap.class),
              any(ReallocateRequest.class)))
          .thenReturn(Single.just(reallocatedAssignment));
      when(allocationDAO.insertAllocationsAndIncrementCounts(
              anyString(), eq(PROJECT_KEY), anyList(), anyMap()))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<List<UserExperimentMap>> testObserver =
          allocationService.applyOverrides(PROJECT_KEY, experiment, overrides).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(results -> results.size() == 2);
    }

    private Experiment createMockExperimentWithVariants() {
      return Experiment.builder()
          .experimentId(EXPERIMENT_ID)
          .projectKey(PROJECT_KEY)
          .name("Test Experiment")
          .experimentKey("test_experiment")
          .status(ExperimentStatus.LIVE)
          .type(ExperimentType.A_B)
          .variants(
              Map.of(
                  CONTROL_VARIANT,
                  Variant.builder().displayName(CONTROL_VARIANT).build(),
                  TREATMENT_VARIANT,
                  Variant.builder().displayName(TREATMENT_VARIANT).build()))
          .build();
    }

    private Overrides createMockOverrides() {
      Overrides overrides = new Overrides();
      Map<String, List<String>> overrideIds = new HashMap<>();
      overrideIds.put(CONTROL_VARIANT, List.of("user1", "user2"));
      overrideIds.put(TREATMENT_VARIANT, List.of("user3"));
      overrides.setOverrideIds(overrideIds);
      return overrides;
    }
  }

  @Nested
  @DisplayName("AllotExperiments Test Allocations Tests")
  class AllotExperimentsTestAllocationsTests {

    private static final UUID TEST_EXPERIMENT_ID =
        UUID.fromString("8e1b5ccb-c993-5193-c09d-312b7ab31f2c");
    private static final UUID LIVE_EXPERIMENT_ID =
        UUID.fromString("9f2c6ddc-d0a4-6204-d00e-423c8bc42a3d");
    private static final UUID CONCLUDED_EXPERIMENT_ID =
        UUID.fromString("0a3d7eed-e1b5-7315-e11f-534d9cd53a4e");

    @Test
    @DisplayName("Should return test allocations for user with test experiment allocations")
    void testAllotExperimentsReturnsTestAllocations() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("live_experiment", "test_experiment"))
              .build();

      Experiment liveExperiment =
          createExperimentWithStatus(LIVE_EXPERIMENT_ID, "live_experiment", ExperimentStatus.LIVE);
      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);

      UserExperimentMap liveAllocation =
          createAllocationForExperiment(LIVE_EXPERIMENT_ID, CONTROL_VARIANT);
      UserExperimentMap testAllocation =
          createAllocationForExperiment(TEST_EXPERIMENT_ID, TREATMENT_VARIANT);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, List.of(liveAllocation, testAllocation));

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(liveExperiment, testExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            List<UserExperimentMap> allocations = response.experimentMap();
            // Should contain both live and test allocations
            boolean hasLiveAllocation =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(LIVE_EXPERIMENT_ID));
            boolean hasTestAllocation =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(TEST_EXPERIMENT_ID));
            return hasLiveAllocation && hasTestAllocation;
          });
    }

    @Test
    @DisplayName("Should return test allocations when no live experiments pass filters")
    void testAllotExperimentsReturnsTestAllocationsWhenNoLiveExperimentsPassFilters() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("test_experiment"))
              .build();

      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);
      UserExperimentMap testAllocation =
          createAllocationForExperiment(TEST_EXPERIMENT_ID, TREATMENT_VARIANT);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, List.of(testAllocation));

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(testExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            List<UserExperimentMap> allocations = response.experimentMap();
            // Should contain test allocation even though no live experiments
            return allocations.size() == 1
                && allocations.get(0).getExperimentId().equals(TEST_EXPERIMENT_ID);
          });
    }

    @Test
    @DisplayName("Should not create new allocations for test experiments")
    void testAllotExperimentsDoesNotCreateNewAllocationsForTestExperiments() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("test_experiment"))
              .build();

      // Test experiment exists but user has no allocation for it
      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, Collections.emptyList()); // No existing allocations

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(testExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            // Should return empty - no new allocations created for TEST experiments
            return response.experimentMap().isEmpty();
          });

      // Verify no new allocations were inserted
      verify(allocationDAO, never())
          .insertAllocationsAndIncrementCounts(anyString(), anyString(), anyList(), anyMap());
    }

    @Test
    @DisplayName("Should return test, live, and concluded allocations together")
    void testAllotExperimentsReturnsMixedAllocations() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("live_experiment", "test_experiment", "concluded_experiment"))
              .build();

      Experiment liveExperiment =
          createExperimentWithStatus(LIVE_EXPERIMENT_ID, "live_experiment", ExperimentStatus.LIVE);
      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);
      Experiment concludedExperiment =
          createExperimentWithStatusAndWinner(
              CONCLUDED_EXPERIMENT_ID,
              "concluded_experiment",
              ExperimentStatus.CONCLUDED,
              CONTROL_VARIANT);

      UserExperimentMap liveAllocation =
          createAllocationForExperiment(LIVE_EXPERIMENT_ID, CONTROL_VARIANT);
      UserExperimentMap testAllocation =
          createAllocationForExperiment(TEST_EXPERIMENT_ID, TREATMENT_VARIANT);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, List.of(liveAllocation, testAllocation));

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(liveExperiment, testExperiment, concludedExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            List<UserExperimentMap> allocations = response.experimentMap();
            // Should contain live, test, and concluded allocations
            boolean hasLive =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(LIVE_EXPERIMENT_ID));
            boolean hasTest =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(TEST_EXPERIMENT_ID));
            boolean hasConcluded =
                allocations.stream()
                    .anyMatch(a -> a.getExperimentId().equals(CONCLUDED_EXPERIMENT_ID));
            return hasLive && hasTest && hasConcluded;
          });
    }

    @Test
    @DisplayName("Should include test allocations alongside existing live allocations")
    void testAllotExperimentsIncludesTestAllocationsWithExistingLiveAllocations() {
      // Arrange - User already has both live and test allocations
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("live_experiment", "test_experiment"))
              .build();

      Experiment liveExperiment =
          createExperimentWithStatus(LIVE_EXPERIMENT_ID, "live_experiment", ExperimentStatus.LIVE);
      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);

      // User has both live and test allocations already
      UserExperimentMap liveAllocation =
          createAllocationForExperiment(LIVE_EXPERIMENT_ID, CONTROL_VARIANT);
      UserExperimentMap testAllocation =
          createAllocationForExperiment(TEST_EXPERIMENT_ID, TREATMENT_VARIANT);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, List.of(liveAllocation, testAllocation));

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(liveExperiment, testExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            List<UserExperimentMap> allocations = response.experimentMap();
            // Should contain both existing live allocation and test allocation
            boolean hasLive =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(LIVE_EXPERIMENT_ID));
            boolean hasTest =
                allocations.stream().anyMatch(a -> a.getExperimentId().equals(TEST_EXPERIMENT_ID));
            return hasLive && hasTest && allocations.size() == 2;
          });
    }

    @Test
    @DisplayName("Should return empty when no allocations exist and only test experiments")
    void testAllotExperimentsEmptyWhenNoAllocationsAndOnlyTestExperiments() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("test_experiment_1", "test_experiment_2"))
              .build();

      Experiment testExperiment1 =
          createExperimentWithStatus(
              TEST_EXPERIMENT_ID, "test_experiment_1", ExperimentStatus.TEST);
      UUID testExperiment2Id = UUID.fromString("1b4e8ffc-f2c6-8426-0000-645e0de64a5f");
      Experiment testExperiment2 =
          createExperimentWithStatus(testExperiment2Id, "test_experiment_2", ExperimentStatus.TEST);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, Collections.emptyList());

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(testExperiment1, testExperiment2)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(response -> response.experimentMap().isEmpty());
    }

    @Test
    @DisplayName("Should handle user with only test allocations correctly")
    void testAllotExperimentsUserWithOnlyTestAllocations() {
      // Arrange
      AllocationRequest request =
          AllocationRequest.builder()
              .userId(USER_ID)
              .experimentKeys(List.of("test_experiment"))
              .build();

      Experiment testExperiment =
          createExperimentWithStatus(TEST_EXPERIMENT_ID, "test_experiment", ExperimentStatus.TEST);
      UserExperimentMap testAllocation =
          createAllocationForExperiment(TEST_EXPERIMENT_ID, CONTROL_VARIANT);

      Map<String, List<UserExperimentMap>> userAllocationsMap = new HashMap<>();
      userAllocationsMap.put(USER_ID, List.of(testAllocation));

      when(cohortService.getUserCohorts(eq(USER_ID), eq(PROJECT_KEY)))
          .thenReturn(Maybe.just(Collections.emptyList()));
      when(allocationDAO.fetchActiveExperiments(eq(PROJECT_KEY), anyList()))
          .thenReturn(Single.just(List.of(testExperiment)));
      when(allocationDAO.getAllocations(anyList(), eq(PROJECT_KEY)))
          .thenReturn(Single.just(userAllocationsMap));

      // Act
      TestObserver<AllocationsResponse> testObserver =
          allocationService.allotExperiments(PROJECT_KEY, request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValue(
          response -> {
            List<UserExperimentMap> allocations = response.experimentMap();
            return allocations.size() == 1
                && allocations.get(0).getExperimentId().equals(TEST_EXPERIMENT_ID)
                && allocations.get(0).getVariantName().equals(CONTROL_VARIANT);
          });
    }

    // Helper methods for test experiments tests
    private Experiment createExperimentWithStatus(
        UUID experimentId, String key, ExperimentStatus status) {
      return Experiment.builder()
          .experimentId(experimentId)
          .projectKey(PROJECT_KEY)
          .name("Experiment " + key)
          .experimentKey(key)
          .status(status)
          .type(ExperimentType.A_B)
          .variants(
              Map.of(
                  CONTROL_VARIANT,
                  Variant.builder().displayName(CONTROL_VARIANT).build(),
                  TREATMENT_VARIANT,
                  Variant.builder().displayName(TREATMENT_VARIANT).build()))
          .build();
    }

    private Experiment createExperimentWithStatusAndWinner(
        UUID experimentId, String key, ExperimentStatus status, String winningVariantName) {
      WinningVariant winningVariant = new WinningVariant();
      winningVariant.setVariantName(winningVariantName);
      return Experiment.builder()
          .experimentId(experimentId)
          .projectKey(PROJECT_KEY)
          .name("Experiment " + key)
          .experimentKey(key)
          .status(status)
          .type(ExperimentType.A_B)
          .winningVariant(winningVariant)
          .variants(
              Map.of(
                  CONTROL_VARIANT,
                  Variant.builder().displayName(CONTROL_VARIANT).build(),
                  TREATMENT_VARIANT,
                  Variant.builder().displayName(TREATMENT_VARIANT).build()))
          .build();
    }

    private UserExperimentMap createAllocationForExperiment(UUID experimentId, String variantName) {
      return UserExperimentMap.builder()
          .experimentId(experimentId)
          .variantName(variantName)
          .status("ASSIGNED")
          .assignedAt(System.currentTimeMillis())
          .build();
    }
  }

  // Helper methods
  private Experiment createMockExperiment() {
    return Experiment.builder()
        .experimentId(EXPERIMENT_ID)
        .projectKey(PROJECT_KEY)
        .name("Test Experiment")
        .experimentKey("test_experiment")
        .status(ExperimentStatus.LIVE)
        .type(ExperimentType.A_B)
        .variants(
            Map.of(
                CONTROL_VARIANT,
                Variant.builder().displayName(CONTROL_VARIANT).build(),
                TREATMENT_VARIANT,
                Variant.builder().displayName(TREATMENT_VARIANT).build()))
        .build();
  }

  private UserExperimentMap createCurrentAssignment(String variantName) {
    return UserExperimentMap.builder()
        .experimentId(EXPERIMENT_ID)
        .variantName(variantName)
        .status("ASSIGNED")
        .assignedAt(System.currentTimeMillis())
        .build();
  }

  private void setupCommonMocks(ReallocateRequest request, String currentVariant) {
    when(allocationDAO.acquireUserLock(eq(request.getUserId()), eq(PROJECT_KEY)))
        .thenReturn(Single.just(true));
    when(allocationDAO.releaseUserLock(eq(request.getUserId()), eq(PROJECT_KEY)))
        .thenReturn(Single.just(true));
    when(allocationDAO.fetchActiveExperiment(eq(PROJECT_KEY), eq(request.getExperimentId())))
        .thenReturn(Single.just(createMockExperiment()));
    when(allocationDAO.getAllocations(eq(request.getUserId()), eq(PROJECT_KEY)))
        .thenReturn(Single.just(List.of(createCurrentAssignment(currentVariant))));
  }
}
