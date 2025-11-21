package com.ascend.testlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.AdminDAO;
import com.ascend.testlab.dto.entity.ExperimentHistoryEntry;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.ExperimentHistoryResponse;
import com.ascend.testlab.dto.response.ExperimentKeyAvailabilityResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.impl.AdminServiceImpl;
import com.dream11.rest.exception.RestException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import java.time.Instant;
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
public class AdminServiceTest {

  @Mock private AdminDAO adminDAO;
  private AdminService adminService;
  private static final String PROJECT_KEY = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_KEY = "test_experiment_key";
  private static final String EXPERIMENT_ID = "123e4567-e89b-12d3-a456-426614174001";

  @BeforeEach
  void setUp() {
    adminService = new AdminServiceImpl(adminDAO);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create service with valid DAO")
    void testConstructorWithValidDAO() {
      // Act
      AdminServiceImpl service = new AdminServiceImpl(adminDAO);

      // Assert
      assertNotNull(service);
    }

    @Test
    @DisplayName("Should create service with null DAO")
    void testConstructorWithNullDAO() {
      // Act
      AdminServiceImpl service = new AdminServiceImpl(null);

      // Assert
      assertNotNull(service);
    }
  }

  @Nested
  @DisplayName("Tag Retrieval Tests")
  class TagsTests {
    @Test
    @DisplayName("Should return correct tags for a valid project key")
    void testGetTags_Success() {
      // Arrange
      List<String> mockTags = Arrays.asList("A/B-test", "feature-flag", "performance", "ui-test");
      when(adminDAO.fetchTags(PROJECT_KEY)).thenReturn(Single.just(mockTags));

      // Act
      TestObserver<TagsResponse> testObserver = adminService.getTags(PROJECT_KEY).test();

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
      verify(adminDAO, times(1)).fetchTags(PROJECT_KEY);
    }

    @Test
    @DisplayName("Should return empty tags list if project has no tags")
    void testGetTags_EmptyResult() {
      // Arrange
      when(adminDAO.fetchTags(PROJECT_KEY)).thenReturn(Single.just(List.of()));

      // Act
      TestObserver<TagsResponse> testObserver = adminService.getTags(PROJECT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      TagsResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertNotNull(response.tags());
      assertTrue(response.tags().isEmpty());
      verify(adminDAO, times(1)).fetchTags(PROJECT_KEY);
    }

    @Test
    @DisplayName("Should throw RestException when tag fetching fails in DAO")
    void testGetTags_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(adminDAO.fetchTags(PROJECT_KEY)).thenReturn(Single.error(dbException));

      // Act
      Single<TagsResponse> result = adminService.getTags(PROJECT_KEY);
      TestObserver<TagsResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).fetchTags(PROJECT_KEY);
    }
  }

  @Nested
  @DisplayName("Key Availability Tests")
  class KeyAvailabilityTests {
    @Test
    @DisplayName("Should return available=true when experiment key does not exist")
    void testIsExperimentKeyAvailable_Available() {
      // Arrange
      when(adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY))
          .thenReturn(Single.just(true));

      // Act
      TestObserver<ExperimentKeyAvailabilityResponse> testObserver =
          adminService.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentKeyAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertTrue(response.isAvailable());
      verify(adminDAO, times(1)).isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
    }

    @Test
    @DisplayName("Should return available=false when experiment key already exists")
    void testIsExperimentKeyAvailable_NotAvailable() {
      // Arrange
      when(adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY))
          .thenReturn(Single.just(false));

      // Act
      TestObserver<ExperimentKeyAvailabilityResponse> testObserver =
          adminService.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentKeyAvailabilityResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertFalse(response.isAvailable());
      verify(adminDAO, times(1)).isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
    }

    @Test
    @DisplayName("Should throw RestException when key availability check fails in DAO")
    void testIsExperimentKeyAvailable_DAOError() {
      // Arrange
      RuntimeException dbException = new RuntimeException("Database error");
      when(adminDAO.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY))
          .thenReturn(Single.error(dbException));

      // Act
      Single<ExperimentKeyAvailabilityResponse> result =
          adminService.isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
      TestObserver<ExperimentKeyAvailabilityResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).isExperimentKeyAvailable(PROJECT_KEY, EXPERIMENT_KEY);
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
              .previousData(new JsonObject().put("status", "DRAFT"))
              .currentData(new JsonObject().put("status", "LIVE"))
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();
      ExperimentHistoryEntry entry2 =
          ExperimentHistoryEntry.builder()
              .updatedBy("user2")
              .previousData(new JsonObject().put("status", "LIVE"))
              .currentData(new JsonObject().put("status", "PAUSED"))
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();
      List<ExperimentHistoryEntry> mockHistory = Arrays.asList(entry1, entry2);
      ExperimentHistoryResponse mockResult =
          ExperimentHistoryResponse.builder()
              .experimentId(EXPERIMENT_ID)
              .history(mockHistory)
              .pagination(
                  PaginationMeta.builder()
                      .currentPage(page)
                      .pageSize(mockHistory.size())
                      .totalCount(2)
                      .build())
              .build();
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      when(adminDAO.fetchExperimentHistory(request)).thenReturn(Single.just(mockResult));

      // Act
      TestObserver<ExperimentHistoryResponse> testObserver =
          adminService.getExperimentHistory(request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testObserver.assertValueCount(1);
      ExperimentHistoryResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(EXPERIMENT_ID, response.experimentId());
      assertNotNull(response.history());
      assertEquals(2, response.history().size());
      assertNotNull(response.pagination());
      assertEquals(page, response.pagination().currentPage());
      assertEquals(response.history().size(), response.pagination().pageSize());
      assertEquals(2, response.pagination().totalCount());
      assertEquals("user1", response.history().get(0).updatedBy());
      assertEquals("user2", response.history().get(1).updatedBy());
      verify(adminDAO, times(1)).fetchExperimentHistory(request);
    }

    @Test
    @DisplayName("Should return paginated results with correct pagination metadata")
    void testGetExperimentHistory_Pagination() {
      // Arrange
      int limit = 10;
      int page = 2;
      int totalCount = 25;
      List<ExperimentHistoryEntry> mockHistory =
          List.of(
              ExperimentHistoryEntry.builder()
                  .updatedBy("user11")
                  .previousData(new JsonObject().put("status", "DRAFT"))
                  .currentData(new JsonObject().put("status", "LIVE"))
                  .createdAt(Instant.now())
                  .updatedAt(Instant.now())
                  .build());
      ExperimentHistoryResponse mockResult =
          ExperimentHistoryResponse.builder()
              .experimentId(EXPERIMENT_ID)
              .history(mockHistory)
              .pagination(
                  PaginationMeta.builder()
                      .currentPage(page)
                      .pageSize(mockHistory.size())
                      .totalCount(totalCount)
                      .build())
              .build();
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      when(adminDAO.fetchExperimentHistory(request)).thenReturn(Single.just(mockResult));

      // Act
      TestObserver<ExperimentHistoryResponse> testObserver =
          adminService.getExperimentHistory(request).test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      ExperimentHistoryResponse response = testObserver.values().get(0);
      assertNotNull(response);
      assertEquals(page, response.pagination().currentPage());
      assertEquals(response.history().size(), response.pagination().pageSize());
      assertEquals(totalCount, response.pagination().totalCount());
      verify(adminDAO, times(1)).fetchExperimentHistory(request);
    }

    @Test
    @DisplayName("Should throw RestException with EXPERIMENT_NOT_FOUND when history is empty")
    void testGetExperimentHistory_EmptyResult() {
      // Arrange
      int limit = 20;
      int page = 1;
      ExperimentHistoryResponse mockResult =
          ExperimentHistoryResponse.builder()
              .experimentId(EXPERIMENT_ID)
              .history(List.of())
              .pagination(
                  PaginationMeta.builder().currentPage(page).pageSize(0).totalCount(0).build())
              .build();
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      when(adminDAO.fetchExperimentHistory(request)).thenReturn(Single.just(mockResult));

      // Act
      Single<ExperimentHistoryResponse> result = adminService.getExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      testObserver.assertError(
          error ->
              error instanceof RestException
                  && ((RestException) error)
                      .getErrorCode()
                      .equals(ErrorEnum.EXPERIMENT_NOT_FOUND.getErrorCode()));
      verify(adminDAO, times(1)).fetchExperimentHistory(request);
    }

    @Test
    @DisplayName("Should throw RestException when experiment history fetching fails in DAO")
    void testGetExperimentHistory_DAOError() {
      // Arrange
      int limit = 20;
      int page = 1;
      RuntimeException dbException = new RuntimeException("Database error");
      ExperimentHistoryRequest request =
          ExperimentHistoryRequest.builder()
              .projectKey(PROJECT_KEY)
              .experimentId(EXPERIMENT_ID)
              .limit(limit)
              .page(page)
              .build();
      when(adminDAO.fetchExperimentHistory(request)).thenReturn(Single.error(dbException));

      // Act
      Single<ExperimentHistoryResponse> result = adminService.getExperimentHistory(request);
      TestObserver<ExperimentHistoryResponse> testObserver = result.test();

      // Assert
      testObserver.assertError(RestException.class);
      verify(adminDAO, times(1)).fetchExperimentHistory(request);
    }
  }
}
