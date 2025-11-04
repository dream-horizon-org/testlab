package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.TagsDAO;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.impl.TagsServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagsServiceTest {

  @Mock private TagsDAO tagsDAO;

  private TagsService tagsService;

  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    tagsService = new TagsServiceImpl(tagsDAO);
  }

  @Test
  void testGetTags_Success() {
    // Given
    List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
    when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.just(mockTags));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectKey);

    // Then
    TagsResponse response = result.blockingGet();
    assertNotNull(response);
    assertNotNull(response.tags());
    assertEquals(4, response.tags().size());
    assertTrue(response.tags().contains("A/B-test"));
    assertTrue(response.tags().contains("feature-flag"));
    assertTrue(response.tags().contains("performance"));
    assertTrue(response.tags().contains("ui-test"));
    verify(tagsDAO, times(1)).fetchTags(testProjectKey);
  }

  @Test
  void testGetTags_EmptyResult() {
    // Given
    when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.just(List.of()));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectKey);

    // Then
    TagsResponse response = result.blockingGet();
    assertNotNull(response);
    assertNotNull(response.tags());
    assertTrue(response.tags().isEmpty());
    verify(tagsDAO, times(1)).fetchTags(testProjectKey);
  }

  @Test
  void testGetTags_DAOError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database error");
    when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.error(dbException));

    // When
    Single<TagsResponse> result = tagsService.getTags(testProjectKey);

    // Then
    RestException exception = assertThrows(RestException.class, result::blockingGet);
    assertEquals(ErrorEnum.REST_FETCH_TAGS_FAILED.getErrorCode(), exception.getErrorCode());
    assertEquals(
        ErrorEnum.REST_FETCH_TAGS_FAILED.getHttpStatusCode(), exception.getHttpStatusCode());
    assertTrue(exception.getMessage().contains("Tags Listing failed"));
    verify(tagsDAO, times(1)).fetchTags(testProjectKey);
  }
}
