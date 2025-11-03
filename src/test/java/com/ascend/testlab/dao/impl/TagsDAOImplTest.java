package com.ascend.testlab.dao.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagsDAOImplTest {

  @Mock private PgReaderClient pgReaderClient;

  @Mock private RowSet<Row> rowSet;

  @Mock private Row row1;

  @Mock private Row row2;

  private TagsDAOImpl tagsDAO;

  private final String testProjectKey = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeEach
  void setUp() {
    tagsDAO = new TagsDAOImpl(pgReaderClient);
  }

  @Test
  void testFetchTags_Success() {
    // Given
    when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any()))
        .thenReturn(Single.just(Arrays.asList("A/B-test", "feature-flag")));

    // When
    Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);

    // Then
    List<String> actualTags = result.blockingGet();
    assertNotNull(actualTags);
    assertEquals(2, actualTags.size());
    assertTrue(actualTags.contains("A/B-test"));
    assertTrue(actualTags.contains("feature-flag"));
    verify(pgReaderClient, times(1)).fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any());
  }

  @Test
  void testFetchTags_EmptyResult() {
    // Given
    when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any()))
        .thenReturn(Single.just(Arrays.asList()));

    // When
    Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);

    // Then
    List<String> actualTags = result.blockingGet();
    assertNotNull(actualTags);
    assertTrue(actualTags.isEmpty());
    verify(pgReaderClient, times(1)).fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any());
  }

  @Test
  void testFetchTags_DatabaseError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database connection failed");
    when(pgReaderClient.fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any()))
        .thenReturn(Single.error(dbException));

    // When
    Single<List<String>> result = tagsDAO.fetchTags(testProjectKey);

    // Then
    Exception exception = assertThrows(RuntimeException.class, result::blockingGet);
    assertEquals("Database connection failed", exception.getMessage());
    verify(pgReaderClient, times(1)).fetchAll(eq(ReadQuery.FETCH_TAGS), any(), any());
  }
}
