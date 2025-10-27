package com.ascend.testlab.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.service.TagsService;
import io.reactivex.rxjava3.core.Single;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTagsTest {

  @Mock private TagsService tagsService;

  private GetTags getTags;

  private final String validProjectId = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    getTags = new GetTags(tagsService);
  }

  @Test
  void testHandle_Success() throws Exception {
    // Given
    List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
    TagsResponse response = new TagsResponse(mockTags);
    when(tagsService.getTags(any(UUID.class))).thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<TagsResponse>> result = getTags.handle(validProjectId);
    ResponseEntity.Success<TagsResponse> responseEntity = result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertEquals(4, responseEntity.data().tags().size());
    assertTrue(responseEntity.data().tags().contains("A/B-test"));
    assertTrue(responseEntity.data().tags().contains("feature-flag"));
    verify(tagsService, times(1)).getTags(any(UUID.class));
  }

  @Test
  void testHandle_EmptyResult() throws Exception {
    // Given
    TagsResponse response = new TagsResponse(Arrays.asList());
    when(tagsService.getTags(any(UUID.class))).thenReturn(Single.just(response));

    // When
    CompletionStage<ResponseEntity.Success<TagsResponse>> result = getTags.handle(validProjectId);
    ResponseEntity.Success<TagsResponse> responseEntity = result.toCompletableFuture().get();

    // Then
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.data());
    assertTrue(responseEntity.data().tags().isEmpty());
    verify(tagsService, times(1)).getTags(any(UUID.class));
  }

  @Test
  void testHandle_InvalidProjectId() {
    // Given
    String invalidProjectId = "invalid-uuid";

    // When & Then
    assertThrows(Exception.class, () -> getTags.handle(invalidProjectId));
    verify(tagsService, never()).getTags(any(UUID.class));
  }
}
