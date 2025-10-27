package com.ascend.testlab.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.service.ExperimentHistoryService;
import io.reactivex.rxjava3.core.Single;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetExperimentHistoryTest {

  @Mock private ExperimentHistoryService experimentHistoryService;

  private GetExperimentHistory getExperimentHistory;

  private final String validProjectId = "123e4567-e89b-12d3-a456-426614174000";
  private final String validExperimentId = "550e8400-e29b-41d4-a716-446655440000";

  @BeforeEach
  void setUp() {
    getExperimentHistory = new GetExperimentHistory(experimentHistoryService);
  }

  @Test
  void testHandle_Success() throws Exception {
    // Given
    GetExperimentHistoryResponse response =
        GetExperimentHistoryResponse.builder()
            .experimentId(validExperimentId)
            .history(Arrays.asList())
            .totalCount(0)
            .build();
    when(experimentHistoryService.getExperimentHistory(any(UUID.class), any(UUID.class)))
        .thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<GetExperimentHistoryResponse>> result =
        getExperimentHistory.handle(validProjectId, validExperimentId);
    ResponseEntity.Success<GetExperimentHistoryResponse> responseEntity =
        result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertEquals(validExperimentId, responseEntity.data().getExperimentId());
    assertEquals(0, responseEntity.data().getTotalCount());
    verify(experimentHistoryService, times(1))
        .getExperimentHistory(any(UUID.class), any(UUID.class));
  }

  @Test
  void testHandle_WithHistoryData() throws Exception {
    // Given
    GetExperimentHistoryResponse response =
        GetExperimentHistoryResponse.builder()
            .experimentId(validExperimentId)
            .history(Arrays.asList())
            .totalCount(2)
            .build();
    when(experimentHistoryService.getExperimentHistory(any(UUID.class), any(UUID.class)))
        .thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<GetExperimentHistoryResponse>> result =
        getExperimentHistory.handle(validProjectId, validExperimentId);
    ResponseEntity.Success<GetExperimentHistoryResponse> responseEntity =
        result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertEquals(validExperimentId, responseEntity.data().getExperimentId());
    assertEquals(2, responseEntity.data().getTotalCount());
    verify(experimentHistoryService, times(1))
        .getExperimentHistory(any(UUID.class), any(UUID.class));
  }

  @Test
  void testHandle_InvalidProjectId() {
    // Given
    String invalidProjectId = "invalid-uuid";

    // When & Then
    assertThrows(
        Exception.class, () -> getExperimentHistory.handle(invalidProjectId, validExperimentId));
    verify(experimentHistoryService, never())
        .getExperimentHistory(any(UUID.class), any(UUID.class));
  }

  @Test
  void testHandle_InvalidExperimentId() {
    // Given
    String invalidExperimentId = "invalid-uuid";

    // When & Then
    assertThrows(
        Exception.class, () -> getExperimentHistory.handle(validProjectId, invalidExperimentId));
    verify(experimentHistoryService, never())
        .getExperimentHistory(any(UUID.class), any(UUID.class));
  }
}
