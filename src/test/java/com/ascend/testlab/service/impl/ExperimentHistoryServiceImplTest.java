package com.ascend.testlab.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.dao.ExperimentHistoryDAO;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
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
class ExperimentHistoryServiceImplTest {

  @Mock private ExperimentHistoryDAO experimentHistoryDAO;

  private ExperimentHistoryServiceImpl experimentHistoryService;

  private final UUID testProjectId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private final UUID testExperimentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

  @BeforeEach
  void setUp() {
    experimentHistoryService = new ExperimentHistoryServiceImpl(experimentHistoryDAO);
  }

  @Test
  void testGetExperimentHistory_Success() {
    // Given
    List<ExperimentHistoryEntry> mockEntries =
        Arrays.asList(
            new ExperimentHistoryEntry(
                "user1",
                "{\"status\":\"DRAFT\"}",
                "{\"status\":\"LIVE\"}",
                "2024-01-01T10:00:00",
                "2024-01-01T10:00:00"),
            new ExperimentHistoryEntry(
                "user2",
                null,
                "{\"status\":\"PAUSED\"}",
                "2024-01-01T11:00:00",
                "2024-01-01T11:00:00"));
    when(experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId))
        .thenReturn(Single.just(mockEntries));

    // When
    Single<GetExperimentHistoryResponse> result =
        experimentHistoryService.getExperimentHistory(testProjectId, testExperimentId);

    // Then
    GetExperimentHistoryResponse response = result.blockingGet();
    assertNotNull(response);
    assertEquals(testExperimentId.toString(), response.getExperimentId());
    assertEquals(2, response.getTotalCount());
    assertEquals(2, response.getHistory().size());
    assertEquals("user1", response.getHistory().get(0).updatedBy());
    assertEquals("user2", response.getHistory().get(1).updatedBy());
    verify(experimentHistoryDAO, times(1)).fetchExperimentHistory(testProjectId, testExperimentId);
  }

  @Test
  void testGetExperimentHistory_EmptyResult() {
    // Given
    when(experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId))
        .thenReturn(Single.just(Arrays.asList()));

    // When
    Single<GetExperimentHistoryResponse> result =
        experimentHistoryService.getExperimentHistory(testProjectId, testExperimentId);

    // Then
    GetExperimentHistoryResponse response = result.blockingGet();
    assertNotNull(response);
    assertEquals(testExperimentId.toString(), response.getExperimentId());
    assertEquals(0, response.getTotalCount());
    assertTrue(response.getHistory().isEmpty());
    verify(experimentHistoryDAO, times(1)).fetchExperimentHistory(testProjectId, testExperimentId);
  }

  @Test
  void testGetExperimentHistory_DAOError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database error");
    when(experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId))
        .thenReturn(Single.error(dbException));

    // When
    Single<GetExperimentHistoryResponse> result =
        experimentHistoryService.getExperimentHistory(testProjectId, testExperimentId);

    // Then
    Exception exception = assertThrows(Exception.class, result::blockingGet);
    assertTrue(exception.getMessage().contains("Failed to fetch experiment history"));
    verify(experimentHistoryDAO, times(1)).fetchExperimentHistory(testProjectId, testExperimentId);
  }
}
