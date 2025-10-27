package com.ascend.testlab.dao.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperimentNameValidationDAOImplTest {

  @Mock private MySQLReaderClient mySQLReaderClient;

  @Mock private RowSet<Row> rowSet;

  @Mock private Row row;

  private ExperimentNameValidationDAOImpl experimentNameValidationDAO;

  private final UUID testProjectId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private final String testExperimentName = "my-test-experiment";

  @BeforeEach
  void setUp() {
    experimentNameValidationDAO = new ExperimentNameValidationDAOImpl(mySQLReaderClient);
  }

  @Test
  void testIsExperimentNameExists_Exists() {
    // Given
    when(mySQLReaderClient.fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any()))
        .thenReturn(Single.just(true));

    // When
    Single<Boolean> result =
        experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName);

    // Then
    Boolean exists = result.blockingGet();
    assertTrue(exists);
    verify(mySQLReaderClient, times(1))
        .fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any());
  }

  @Test
  void testIsExperimentNameExists_NotExists() {
    // Given
    when(mySQLReaderClient.fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any()))
        .thenReturn(Single.just(false));

    // When
    Single<Boolean> result =
        experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName);

    // Then
    Boolean exists = result.blockingGet();
    assertFalse(exists);
    verify(mySQLReaderClient, times(1))
        .fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any());
  }

  @Test
  void testIsExperimentNameExists_DatabaseError() {
    // Given
    RuntimeException dbException = new RuntimeException("Database connection failed");
    when(mySQLReaderClient.fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any()))
        .thenReturn(Single.error(dbException));

    // When
    Single<Boolean> result =
        experimentNameValidationDAO.isExperimentNameExists(testProjectId, testExperimentName);

    // Then
    Exception exception = assertThrows(RuntimeException.class, result::blockingGet);
    assertEquals("Database connection failed", exception.getMessage());
    verify(mySQLReaderClient, times(1))
        .fetchOne(eq(ReadQuery.CHECK_EXPERIMENT_NAME_EXISTS), any(), any());
  }
}
