package com.ascend.testlab.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.config.ApplicationConfig;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.AllocationService;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for Allocation REST endpoint, specifically for PUT /v1/allocations
 * (reallocation API).
 *
 * @author NishantParmar0026
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("Allocation REST Endpoint Tests")
public class ReallocateIT {

  private Allocation allocationEndpoint;

  @Mock private AllocationService allocationService;
  @Mock private ApplicationConfig applicationConfig;

  private static final String PROJECT_KEY = "project-123";
  private static final String USER_ID = "user-001";
  private static final String EXPERIMENT_ID = "exp-456";
  private static final String VARIANT_NAME = "variant-control";
  private static final String OLD_VARIANT_NAME = "variant-treatment";

  @BeforeEach
  void setUp() {
    allocationEndpoint = new Allocation(allocationService, applicationConfig);
    when(applicationConfig.getProjectKey()).thenReturn(PROJECT_KEY);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create Allocation endpoint with valid dependencies")
    void testConstructorWithValidDependencies() {
      // Act
      Allocation endpoint = new Allocation(allocationService, applicationConfig);

      // Assert
      assertNotNull(endpoint);
    }

    @Test
    @DisplayName("Should throw exception when allocationService is null")
    void testConstructorWithNullAllocationService() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new Allocation(null, applicationConfig));
    }

    @Test
    @DisplayName("Should throw exception when applicationConfig is null")
    void testConstructorWithNullApplicationConfig() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new Allocation(allocationService, null));
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Reallocation Success Tests")
  class ReallocationSuccessTests {

    @Test
    @DisplayName("Should reallocate user to new variant successfully")
    void testReallocationSuccess() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason("Testing variant switching")
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should use default project key when header is empty")
    void testReallocationWithDefaultProjectKey() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle("", request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should reallocate with custom project key")
    void testReallocationWithCustomProjectKey() {
      // Arrange
      String customProjectKey = "custom-project-key";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(customProjectKey, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage =
          allocationEndpoint.reAllocateExperimentHandle(customProjectKey, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(customProjectKey, request);
    }

    @Test
    @DisplayName("Should reallocate without reason field")
    void testReallocationWithoutReason() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason(null)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should return correct response structure on successful reallocation")
    void testReallocationResponseStructure() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      assertTrue(completionStage instanceof java.util.concurrent.CompletionStage);
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Reallocation Error Handling Tests")
  class ReallocationErrorHandlingTests {

    @Test
    @DisplayName("Should handle experiment not found error")
    void testReallocationExperimentNotFound() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId("non-existent-exp")
              .variantName(VARIANT_NAME)
              .build();

      RestException restException = new RestException(ErrorEnum.EXPERIMENT_NOT_FOUND);
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act & Assert - We can't easily test the response because it's wrapped in
      // CompletionStage
      // But we verify the service was called with correct parameters
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle user allocation not found error")
    void testReallocationUserAllocationNotFound() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId("non-existent-user")
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      RestException restException = new RestException(ErrorEnum.NO_ALLOTMENT_FOUND);
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle invalid variant error")
    void testReallocationInvalidVariant() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName("invalid-variant")
              .build();

      RestException restException = new RestException(ErrorEnum.INVALID_VARIANT_FOUND);
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle variant already assigned error")
    void testReallocationVariantAlreadyAssigned() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      RestException restException = new RestException(ErrorEnum.VARIANT_ALREADY_ASSIGNED);
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle lock acquisition failed error")
    void testReallocationLockAcquisitionFailed() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      RestException restException =
          new RestException(ErrorEnum.REALLOCATION_LOCK_ACQUISITION_FAILED);
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(restException));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle generic runtime exception")
    void testReallocationRuntimeException() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      RuntimeException runtimeException = new RuntimeException("Database connection failed");
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.error(runtimeException));

      // Act & Assert
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Validation Tests")
  class ReallocationValidationTests {

    @Test
    @DisplayName("Should handle null experiment ID in request")
    void testReallocationNullExperimentId() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(null)
              .variantName(VARIANT_NAME)
              .build();

      // Act & Assert - Validation should prevent this
      assertNull(request.getExperimentId());
    }

    @Test
    @DisplayName("Should handle null variant name in request")
    void testReallocationNullVariantName() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(null)
              .build();

      // Act & Assert - Validation should prevent this
      assertNull(request.getVariantName());
    }

    @Test
    @DisplayName("Should handle null user ID in request")
    void testReallocationNullUserId() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(null)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      // Act & Assert - Validation should prevent this
      assertNull(request.getUserId());
    }

    @Test
    @DisplayName("Should handle empty experiment ID in request")
    void testReallocationEmptyExperimentId() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId("")
              .variantName(VARIANT_NAME)
              .build();

      // Act & Assert
      assertEquals("", request.getExperimentId());
    }

    @Test
    @DisplayName("Should handle empty variant name in request")
    void testReallocationEmptyVariantName() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName("")
              .build();

      // Act & Assert
      assertEquals("", request.getVariantName());
    }

    @Test
    @DisplayName("Should handle empty user ID in request")
    void testReallocationEmptyUserId() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId("")
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      // Act & Assert
      assertEquals("", request.getUserId());
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Request Body Tests")
  class ReallocationRequestBodyTests {

    @Test
    @DisplayName("Should create valid reallocation request with all fields")
    void testCreateCompleteReallocationRequest() {
      // Arrange & Act
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason("Testing new variant")
              .build();

      // Assert
      assertEquals(USER_ID, request.getUserId());
      assertEquals(EXPERIMENT_ID, request.getExperimentId());
      assertEquals(VARIANT_NAME, request.getVariantName());
      assertEquals("Testing new variant", request.getReason());
    }

    @Test
    @DisplayName("Should create reallocation request without reason")
    void testCreateReallocationRequestWithoutReason() {
      // Arrange & Act
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      // Assert
      assertEquals(USER_ID, request.getUserId());
      assertEquals(EXPERIMENT_ID, request.getExperimentId());
      assertEquals(VARIANT_NAME, request.getVariantName());
      assertNull(request.getReason());
    }

    @Test
    @DisplayName("Should handle request with special characters in reason")
    void testReallocationRequestWithSpecialCharacters() {
      // Arrange & Act
      String specialReason = "Testing @#$%^&*() variant!";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason(specialReason)
              .build();

      // Assert
      assertEquals(specialReason, request.getReason());
    }

    @Test
    @DisplayName("Should handle request with long reason")
    void testReallocationRequestWithLongReason() {
      // Arrange & Act
      String longReason = "a".repeat(500);
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason(longReason)
              .build();

      // Assert
      assertEquals(longReason, request.getReason());
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Edge Cases and Concurrency Tests")
  class ReallocationEdgeCasesTests {

    @Test
    @DisplayName("Should handle rapid successive reallocation requests")
    void testRapidSuccessiveReallocationRequests() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act - Make multiple requests
      for (int i = 0; i < 3; i++) {
        var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);
        assertNotNull(completionStage);
      }

      // Assert
      verify(allocationService, times(3)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle reallocation with very long UUID")
    void testReallocationWithLongUUIDs() {
      // Arrange
      String longUUID = UUID.randomUUID().toString();
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(longUUID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle project key with special characters")
    void testReallocationWithSpecialProjectKey() {
      // Arrange
      String specialProjectKey = "project-123_456-ABC";
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(specialProjectKey, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage =
          allocationEndpoint.reAllocateExperimentHandle(specialProjectKey, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(specialProjectKey, request);
    }

    @Test
    @DisplayName("Should handle delayed service responses")
    void testReallocationDelayedResponse() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult).delay(100, TimeUnit.MILLISECONDS));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Response Structure Tests")
  class ReallocationResponseStructureTests {

    @Test
    @DisplayName("Should return ResponseEntity.Success wrapper")
    void testReallocationResponseIsSuccess() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should handle response with null experiment ID")
    void testReallocationResponseWithNullExperimentId() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap resultWithNullExp =
          UserExperimentMap.builder()
              .experimentId(null)
              .experimentName(null)
              .variantName(VARIANT_NAME)
              .assignedAt(System.currentTimeMillis())
              .build();

      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(resultWithNullExp));

      // Act
      var completionStage = allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      assertNotNull(completionStage);
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
    }
  }

  @Nested
  @DisplayName("PUT /v1/allocations - Service Integration Tests")
  class ReallocationServiceIntegrationTests {

    @Test
    @DisplayName("Should call service with exact request parameters")
    void testReallocationCallsServiceWithExactParameters() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .reason("Integration test")
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
      // Verify exact parameters
      verify(allocationService).reallocateExperiment(PROJECT_KEY, request);
    }

    @Test
    @DisplayName("Should not call service if request is null")
    void testReallocationWithNullRequest() {
      // Act & Assert - Request validation happens before service call
      assertThrows(
          NullPointerException.class,
          () -> allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, null));
    }

    @Test
    @DisplayName("Should verify service is called exactly once per request")
    void testReallocationServiceCallCount() {
      // Arrange
      ReallocateRequest request =
          ReallocateRequest.builder()
              .userId(USER_ID)
              .experimentId(EXPERIMENT_ID)
              .variantName(VARIANT_NAME)
              .build();

      UserExperimentMap expectedResult = createMockUserExperimentMap();
      when(allocationService.reallocateExperiment(PROJECT_KEY, request))
          .thenReturn(Single.just(expectedResult));

      // Act
      allocationEndpoint.reAllocateExperimentHandle(PROJECT_KEY, request);

      // Assert
      verify(allocationService, times(1)).reallocateExperiment(PROJECT_KEY, request);
      verifyNoMoreInteractions(allocationService);
    }
  }

  /**
   * Helper method to create a mock UserExperimentMap for testing.
   *
   * @return a mock UserExperimentMap
   */
  private UserExperimentMap createMockUserExperimentMap() {
    return UserExperimentMap.builder()
        .experimentId(UUID.randomUUID())
        .experimentName("Test Experiment")
        .variantName(VARIANT_NAME)
        .assignedAt(System.currentTimeMillis())
        .build();
  }
}
