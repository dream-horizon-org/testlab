package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.TagsDAO;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.service.impl.TagsServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for TagsService.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("TagsService Tests")
class TagsServiceTest {

  @Mock private TagsDAO tagsDAO;
  private TagsService tagsService;
  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    tagsService = new TagsServiceImpl(tagsDAO);
  }

  @Nested
  @DisplayName("Tag Retrieval Success Cases")
  class TagsSuccessTests {
    @Test
    @DisplayName("Should return correct tags for a valid project key")
    void testGetTags_Success() {
      // Arrange
      List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
      when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.just(mockTags));

      // Act
      TestObserver<TagsResponse> testObserver = tagsService.getTags(testProjectKey).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      TagsResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertNotNull(response.tags());
      assertEquals(4, response.tags().size());
      assertTrue(response.tags().contains("A/B-test"));
      assertTrue(response.tags().contains("feature-flag"));
      assertTrue(response.tags().contains("performance"));
      assertTrue(response.tags().contains("ui-test"));
      verify(tagsDAO, times(1)).fetchTags(testProjectKey);
    }
  }

  @Nested
  @DisplayName("Tag Retrieval with No Results")
  class TagsEmptyResultTests {
    @Test
    @DisplayName("Should return empty tags list if project has no tags")
    void testGetTags_EmptyResult() {
      // Arrange
      when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.just(List.of()));

      // Act
      TestObserver<TagsResponse> testObserver = tagsService.getTags(testProjectKey).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      TagsResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertNotNull(response.tags());
      assertTrue(response.tags().isEmpty());
      verify(tagsDAO, times(1)).fetchTags(testProjectKey);
    }
  }

  @Nested
  @DisplayName("Tag Retrieval Error Handling")
  class TagsErrorTests {
    @Test
    @DisplayName("Should throw RestException when tag fetching fails in DAO")
    void testGetTags_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(tagsDAO.fetchTags(testProjectKey)).thenReturn(Single.error(dbException));

      // Act
      Single<TagsResponse> result = tagsService.getTags(testProjectKey);
      TestObserver<TagsResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(tagsDAO, times(1)).fetchTags(testProjectKey);
    }
  }
}
