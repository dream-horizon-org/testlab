package com.ascend.testlab.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.ValidateExperimentNameResponse;
import com.ascend.testlab.service.ExperimentNameValidationService;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateExperimentNameTest {

  @Mock private ExperimentNameValidationService experimentNameValidationService;

  private ValidateExperimentName validateExperimentName;

  private final String validProjectId = "123e4567-e89b-12d3-a456-426614174000";
  private final String validExperimentName = "my-test-experiment";

  @BeforeEach
  void setUp() {
    validateExperimentName = new ValidateExperimentName(experimentNameValidationService);
  }

  @Test
  void testHandle_NameAvailable() throws Exception {
    // Given
    ValidateExperimentNameResponse response =
        new ValidateExperimentNameResponse(
            true, "Experiment name 'my-test-experiment' is available");
    when(experimentNameValidationService.validateExperimentName(
            any(UUID.class), eq(validExperimentName)))
        .thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<ValidateExperimentNameResponse>> result =
        validateExperimentName.handle(validProjectId, validExperimentName);
    ResponseEntity.Success<ValidateExperimentNameResponse> responseEntity =
        result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertTrue(responseEntity.data().isAvailable());
    assertEquals(
        "Experiment name 'my-test-experiment' is available", responseEntity.data().message());
    verify(experimentNameValidationService, times(1))
        .validateExperimentName(any(UUID.class), eq(validExperimentName));
  }

  @Test
  void testHandle_NameNotAvailable() throws Exception {
    // Given
    ValidateExperimentNameResponse response =
        new ValidateExperimentNameResponse(
            false, "Experiment name 'my-test-experiment' already exists in this project");
    when(experimentNameValidationService.validateExperimentName(
            any(UUID.class), eq(validExperimentName)))
        .thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<ValidateExperimentNameResponse>> result =
        validateExperimentName.handle(validProjectId, validExperimentName);
    ResponseEntity.Success<ValidateExperimentNameResponse> responseEntity =
        result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertFalse(responseEntity.data().isAvailable());
    assertTrue(responseEntity.data().message().contains("already exists"));
    verify(experimentNameValidationService, times(1))
        .validateExperimentName(any(UUID.class), eq(validExperimentName));
  }

  @Test
  void testHandle_InvalidProjectId() {
    // Given
    String invalidProjectId = "invalid-uuid";

    // When & Then
    assertThrows(
        Exception.class,
        () -> validateExperimentName.handle(invalidProjectId, validExperimentName));
    verify(experimentNameValidationService, never())
        .validateExperimentName(any(UUID.class), anyString());
  }

  @Test
  void testHandle_EmptyName() {
    // Given
    String emptyName = "";

    // When & Then
    assertThrows(Exception.class, () -> validateExperimentName.handle(validProjectId, emptyName));
    verify(experimentNameValidationService, never())
        .validateExperimentName(any(UUID.class), anyString());
  }

  @Test
  void testHandle_NameTooLong() {
    // Given
    String longName = "a".repeat(65); // 65 characters, exceeds 64 limit

    // When & Then
    assertThrows(Exception.class, () -> validateExperimentName.handle(validProjectId, longName));
    verify(experimentNameValidationService, never())
        .validateExperimentName(any(UUID.class), anyString());
  }
}
