package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.dto.response.GetAllocationsResponse;
import com.ascend.testlab.service.impl.AllocationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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
      TestObserver<GetAllocationsResponse> testObserver =
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
      TestObserver<GetAllocationsResponse> testObserver =
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
      TestObserver<GetAllocationsResponse> testObserver =
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
      TestObserver<GetAllocationsResponse> testObserver1 =
          allocationService.getAllocations(userId1, PROJECT_KEY).test();
      TestObserver<GetAllocationsResponse> testObserver2 =
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
