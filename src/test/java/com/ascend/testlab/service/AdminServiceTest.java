package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.service.impl.AdminServiceImpl;
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
 * Comprehensive unit tests for AdminService.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("AdminService Tests")
class AdminServiceTest {

  @Mock private AdminDAO adminDAO;
  private AdminService adminService;
  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";
  private final String testExperimentName = "test-experiment";
  private final String testExperimentId = "123e4567-e89b-12d3-a456-426614174001";

  @BeforeEach
  void setUp() {
    adminService = new AdminServiceImpl(adminDAO);
  }

  @Nested
  @DisplayName("Tag Retrieval Tests")
  class TagsTests {
    @Test
    @DisplayName("Should return correct tags for a valid project key")
    void testGetTags_Success() {
      // Arrange
      List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
      when(adminDAO.fetchTags(testProjectKey)).thenReturn(Single.just(mockTags));

      // Act
      TestObserver<TagsResponse> testObserver = adminService.getTags(testProjectKey).test();

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
      verify(adminDAO, times(1)).fetchTags(testProjectKey);
    }

    @Test
    @DisplayName("Should return empty tags list if project has no tags")
    void testGetTags_EmptyResult() {
      // Arrange
      when(adminDAO.fetchTags(testProjectKey)).thenReturn(Single.just(List.of()));

      // Act
      TestObserver<TagsResponse> testObserver = adminService.getTags(testProjectKey).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      TagsResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertNotNull(response.tags());
      assertTrue(response.tags().isEmpty());
      verify(adminDAO, times(1)).fetchTags(testProjectKey);
    }

    @Test
    @DisplayName("Should throw RestException when tag fetching fails in DAO")
    void testGetTags_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(adminDAO.fetchTags(testProjectKey)).thenReturn(Single.error(dbException));

      // Act
      Single<TagsResponse> result = adminService.getTags(testProjectKey);
      TestObserver<TagsResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).fetchTags(testProjectKey);
    }
  }

  @Nested
  @DisplayName("Name Availability Tests")
  class NameAvailabilityTests {
    @Test
    @DisplayName("Should return available=true when experiment name does not exist")
    void testIsExperimentNameAvailable_Available() {
      // Arrange
      when(adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<NameAvailabilityResponse> testObserver =
          adminService.isExperimentNameAvailable(testProjectKey, testExperimentName).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      NameAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertTrue(response.isAvailable());
      assertNotNull(response.message());
      assertTrue(
          response.message().contains(testExperimentName)
              && response.message().contains(testProjectKey)
              && response.message().contains("available"));
      verify(adminDAO, times(1)).isExperimentNameAvailable(testProjectKey, testExperimentName);
    }

    @Test
    @DisplayName("Should return available=false when experiment name already exists")
    void testIsExperimentNameAvailable_NotAvailable() {
      // Arrange
      when(adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<NameAvailabilityResponse> testObserver =
          adminService.isExperimentNameAvailable(testProjectKey, testExperimentName).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      NameAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertFalse(response.isAvailable());
      assertNotNull(response.message());
      assertTrue(
          response.message().contains(testExperimentName)
              && response.message().contains(testProjectKey)
              && response.message().contains("already exists"));
      verify(adminDAO, times(1)).isExperimentNameAvailable(testProjectKey, testExperimentName);
    }

    @Test
    @DisplayName("Should throw RestException when name availability check fails in DAO")
    void testIsExperimentNameAvailable_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(adminDAO.isExperimentNameAvailable(testProjectKey, testExperimentName))
          .thenReturn(Single.error(dbException));

      // Act
      Single<NameAvailabilityResponse> result =
          adminService.isExperimentNameAvailable(testProjectKey, testExperimentName);
      TestObserver<NameAvailabilityResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).isExperimentNameAvailable(testProjectKey, testExperimentName);
    }
  }

  @Nested
  @DisplayName("Experiment History Tests")
  class ExperimentHistoryTests {
    @Test
    @DisplayName(
        "Should return correct experiment history for a valid project key and experiment id")
    void testGetExperimentHistory_Success() {
      // Arrange
      int limit = 20;
      int page = 1;
      ExperimentHistoryEntry entry1 =
          ExperimentHistoryEntry.builder()
              .updatedBy("user1")
              .previousData("{\"status\":\"DRAFT\"}")
              .currentData("{\"status\":\"LIVE\"}")
              .createdAt(System.currentTimeMillis())
              .updatedAt(System.currentTimeMillis())
              .build();
      ExperimentHistoryEntry entry2 =
          ExperimentHistoryEntry.builder()
              .updatedBy("user2")
              .previousData("{\"status\":\"LIVE\"}")
              .currentData("{\"status\":\"PAUSED\"}")
              .createdAt(System.currentTimeMillis())
              .updatedAt(System.currentTimeMillis())
              .build();
      List<ExperimentHistoryEntry> mockHistory = Arrays.asList(entry1, entry2);
      AdminDAO.ExperimentHistoryResult mockResult =
          new AdminDAO.ExperimentHistoryResult(mockHistory, 2);
      when(adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0))
          .thenReturn(Single.just(mockResult));

      // Act
      TestObserver<GetExperimentHistoryResponse> testObserver =
          adminService.getExperimentHistory(testProjectKey, testExperimentId, limit, page).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      GetExperimentHistoryResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(testExperimentId, response.experimentId());
      assertNotNull(response.history());
      assertEquals(2, response.history().size());
      assertNotNull(response.pagination());
      assertEquals(page, response.pagination().currentPage());
      assertEquals(limit, response.pagination().pageSize());
      assertEquals(2, response.pagination().totalCount());
      assertEquals("user1", response.history().get(0).updatedBy());
      assertEquals("user2", response.history().get(1).updatedBy());
      verify(adminDAO, times(1)).fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0);
    }

    @Test
    @DisplayName("Should return paginated results with correct pagination metadata")
    void testGetExperimentHistory_Pagination() {
      // Arrange
      int limit = 10;
      int page = 2;
      int totalCount = 25;
      List<ExperimentHistoryEntry> mockHistory =
          Arrays.asList(
              ExperimentHistoryEntry.builder()
                  .updatedBy("user11")
                  .previousData("{\"status\":\"DRAFT\"}")
                  .currentData("{\"status\":\"LIVE\"}")
                  .createdAt(System.currentTimeMillis())
                  .updatedAt(System.currentTimeMillis())
                  .build());
      AdminDAO.ExperimentHistoryResult mockResult =
          new AdminDAO.ExperimentHistoryResult(mockHistory, totalCount);
      when(adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId, limit, 10))
          .thenReturn(Single.just(mockResult));

      // Act
      TestObserver<GetExperimentHistoryResponse> testObserver =
          adminService.getExperimentHistory(testProjectKey, testExperimentId, limit, page).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      GetExperimentHistoryResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(page, response.pagination().currentPage());
      assertEquals(limit, response.pagination().pageSize());
      assertEquals(totalCount, response.pagination().totalCount());
      verify(adminDAO, times(1))
          .fetchExperimentHistory(testProjectKey, testExperimentId, limit, 10);
    }

    @Test
    @DisplayName("Should return empty history list if experiment has no history")
    void testGetExperimentHistory_EmptyResult() {
      // Arrange
      int limit = 20;
      int page = 1;
      AdminDAO.ExperimentHistoryResult mockResult =
          new AdminDAO.ExperimentHistoryResult(List.of(), 0);
      when(adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0))
          .thenReturn(Single.just(mockResult));

      // Act
      TestObserver<GetExperimentHistoryResponse> testObserver =
          adminService.getExperimentHistory(testProjectKey, testExperimentId, limit, page).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      GetExperimentHistoryResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(testExperimentId, response.experimentId());
      assertNotNull(response.history());
      assertTrue(response.history().isEmpty());
      assertNotNull(response.pagination());
      assertEquals(0, response.pagination().totalCount());
      assertEquals(page, response.pagination().currentPage());
      assertEquals(limit, response.pagination().pageSize());
      verify(adminDAO, times(1)).fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0);
    }

    @Test
    @DisplayName("Should throw RestException when experiment history fetching fails in DAO")
    void testGetExperimentHistory_DAOError() {
      // Arrange
      int limit = 20;
      int page = 1;
      RuntimeException dbException = new RuntimeException("Database error");
      when(adminDAO.fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0))
          .thenReturn(Single.error(dbException));

      // Act
      Single<GetExperimentHistoryResponse> result =
          adminService.getExperimentHistory(testProjectKey, testExperimentId, limit, page);
      TestObserver<GetExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).fetchExperimentHistory(testProjectKey, testExperimentId, limit, 0);
    }
  }
}
