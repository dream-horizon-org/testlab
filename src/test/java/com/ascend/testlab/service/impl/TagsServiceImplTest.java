package com.ascend.testlab.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.TagsDAO;
import com.ascend.testlab.dto.response.TagsResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagsServiceImplTest {

  @Mock private TagsDAO tagsDAO;

  private TagsServiceImpl tagsService;

  private final UUID testProjectId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @BeforeEach
  void setUp() {
    tagsService = new TagsServiceImpl(tagsDAO);
  }

  @Test
  void testGetTags_Success() {
    // Given
    List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
    when(tagsDAO.fetchTags(testProjectId)).thenReturn(Single.just(mockTags));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectId);

    // Then
    TagsResponse response = result.blockingGet();
    assertNotNull(response);
    assertNotNull(response.tags());
    assertEquals(4, response.tags().size());
    assertTrue(response.tags().contains("A/B-test"));
    assertTrue(response.tags().contains("feature-flag"));
    assertTrue(response.tags().contains("performance"));
    assertTrue(response.tags().contains("ui-test"));
    verify(tagsDAO, times(1)).fetchTags(testProjectId);
  }

  @Test
  void testGetTags_EmptyResult() {
    // Given
    when(tagsDAO.fetchTags(testProjectId)).thenReturn(Single.just(Arrays.asList()));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectId);

    // Then
    TagsResponse response = result.blockingGet();
    assertNotNull(response);
    assertNotNull(response.tags());
    assertTrue(response.tags().isEmpty());
    verify(tagsDAO, times(1)).fetchTags(testProjectId);
  }

  @Test
  void testGetTags_DAOError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database error");
    when(tagsDAO.fetchTags(testProjectId)).thenReturn(Single.error(dbException));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectId);

    // Then
    Exception exception = assertThrows(Exception.class, result::blockingGet);
    assertTrue(exception.getMessage().contains("Tags Listing failed"));
    verify(tagsDAO, times(1)).fetchTags(testProjectId);
  }
}
