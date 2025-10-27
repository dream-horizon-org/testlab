package com.ascend.testlab.dao.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperimentHistoryDAOImplTest {

  @Mock private MySQLReaderClient mySQLReaderClient;

  @Mock private RowSet<Row> rowSet;

  @Mock private Row row1;

  @Mock private Row row2;

  private ExperimentHistoryDAOImpl experimentHistoryDAO;

  private final UUID testProjectId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private final UUID testExperimentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

  @BeforeEach
  void setUp() {
    experimentHistoryDAO = new ExperimentHistoryDAOImpl(mySQLReaderClient);
  }

  @Test
  void testFetchExperimentHistory_Success() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    JsonObject previousData = new JsonObject().put("status", "DRAFT");
    JsonObject currentData = new JsonObject().put("status", "LIVE");

    List<ExperimentHistoryEntry> mockEntries =
        Arrays.asList(
            new ExperimentHistoryEntry(
                "user1",
                previousData.encode(),
                currentData.encode(),
                now.toString(),
                now.toString()),
            new ExperimentHistoryEntry(
                "user2",
                null,
                currentData.encode(),
                now.plusHours(1).toString(),
                now.plusHours(1).toString()));

    when(mySQLReaderClient.<ExperimentHistoryEntry>fetchAll(
            eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any()))
        .thenReturn(Single.just(mockEntries));

    // When
    Single<List<ExperimentHistoryEntry>> result =
        experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId);

    // Then
    List<ExperimentHistoryEntry> actualEntries = result.blockingGet();
    assertNotNull(actualEntries);
    assertEquals(2, actualEntries.size());
    assertEquals("user1", actualEntries.get(0).updatedBy());
    assertEquals("user2", actualEntries.get(1).updatedBy());
    verify(mySQLReaderClient, times(1))
        .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any());
  }

  @Test
  void testFetchExperimentHistory_EmptyResult() {
    // Given
    when(mySQLReaderClient.fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any()))
        .thenReturn(Single.just(Arrays.asList()));

    // When
    Single<List<ExperimentHistoryEntry>> result =
        experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId);

    // Then
    List<ExperimentHistoryEntry> actualEntries = result.blockingGet();
    assertNotNull(actualEntries);
    assertTrue(actualEntries.isEmpty());
    verify(mySQLReaderClient, times(1))
        .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any());
  }

  @Test
  void testFetchExperimentHistory_DatabaseError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database connection failed");
    when(mySQLReaderClient.fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any()))
        .thenReturn(Single.error(dbException));

    // When
    Single<List<ExperimentHistoryEntry>> result =
        experimentHistoryDAO.fetchExperimentHistory(testProjectId, testExperimentId);

    // Then
    Exception exception = assertThrows(RuntimeException.class, result::blockingGet);
    assertEquals("Database connection failed", exception.getMessage());
    verify(mySQLReaderClient, times(1))
        .fetchAll(eq(ReadQuery.FETCH_EXPERIMENT_HISTORY), any(), any());
  }
}
