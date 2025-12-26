package com.ascend.testlab.util;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DbExceptionUtil.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@DisplayName("DbExceptionUtil Tests")
class DbExceptionUtilTest {

  @Nested
  @DisplayName("handleDbError Tests")
  class HandleDbErrorTests {

    @Test
    @DisplayName("Should return partition not found error for 'no partition of relation' message")
    void testHandleDbError_NoPartitionOfRelation_ReturnsPartitionNotFoundError() {
      Exception cause = new RuntimeException("no partition of relation \"experiments\" found");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("PARTITION_NOT_FOUND", result.getErrorCode());
      assertEquals(HttpStatus.SC_BAD_REQUEST, result.getHttpStatusCode());
      assertTrue(result.getErrorMessage().contains("Project key partition does not exist"));
    }

    @Test
    @DisplayName("Should return partition not found error for nested cause with partition message")
    void testHandleDbError_NestedPartitionError_ReturnsPartitionNotFoundError() {
      Exception innerCause = new RuntimeException("no partition of relation found");
      Exception cause = new RuntimeException("Database error", innerCause);
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("PARTITION_NOT_FOUND", result.getErrorCode());
      assertEquals(HttpStatus.SC_BAD_REQUEST, result.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should return duplicate experiment error for unique constraint violation 23505")
    void testHandleDbError_UniqueViolation23505_ReturnsDuplicateError() {
      Exception cause =
          new RuntimeException(
              "ERROR: duplicate key value violates unique constraint (23505) experiment_key_unique_check");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("DUPLICATE_EXPERIMENT", result.getErrorCode());
      assertEquals(HttpStatus.SC_BAD_REQUEST, result.getHttpStatusCode());
      assertTrue(result.getErrorMessage().contains("experiment_key already exists"));
    }

    @Test
    @DisplayName(
        "Should return duplicate experiment error for 'duplicate key value violates unique constraint' message")
    void testHandleDbError_DuplicateKeyViolatesConstraint_ReturnsDuplicateError() {
      Exception cause =
          new RuntimeException(
              "duplicate key value violates unique constraint \"experiment_key_key\"");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("DUPLICATE_EXPERIMENT", result.getErrorCode());
      assertEquals(HttpStatus.SC_BAD_REQUEST, result.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should return appropriate message for name unique check constraint")
    void testHandleDbError_NameUniqueCheck_ReturnsNameDuplicateError() {
      Exception cause =
          new RuntimeException("23505: duplicate key value - name_unique_check violated");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("DUPLICATE_EXPERIMENT", result.getErrorCode());
      assertTrue(result.getErrorMessage().contains("name already exists"));
    }

    @Test
    @DisplayName("Should return default duplicate message for unknown constraint")
    void testHandleDbError_UnknownConstraint_ReturnsDefaultDuplicateMessage() {
      Exception cause =
          new RuntimeException("23505: duplicate key value violates some_unknown_constraint");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("DUPLICATE_EXPERIMENT", result.getErrorCode());
      assertEquals("Duplicate entry detected", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should return default error for non-db related exception")
    void testHandleDbError_NonDbException_ReturnsDefaultError() {
      Exception cause = new RuntimeException("Some random error");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      // Should return an error from ErrorEnum handling
      assertNotNull(result.getErrorCode());
    }

    @Test
    @DisplayName("Should handle null message in exception")
    void testHandleDbError_NullMessage_ReturnsDefaultError() {
      Exception cause = new RuntimeException((String) null);
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle deeply nested exception with partition error")
    void testHandleDbError_DeeplyNestedPartitionError_ReturnsPartitionNotFoundError() {
      Exception level3 = new RuntimeException("no partition of relation found");
      Exception level2 = new RuntimeException("Wrapped error", level3);
      Exception level1 = new RuntimeException("Top level error", level2);

      RestException result =
          DbExceptionUtil.handleDbError(level1, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("PARTITION_NOT_FOUND", result.getErrorCode());
    }

    @Test
    @DisplayName("Should handle check violation with partition keyword")
    void testHandleDbError_CheckViolationWithPartition_ReturnsPartitionNotFoundError() {
      Exception cause = new RuntimeException("23514: check constraint violated for partition");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("PARTITION_NOT_FOUND", result.getErrorCode());
    }

    @Test
    @DisplayName("Should prioritize partition error over unique violation")
    void testHandleDbError_BothPartitionAndUnique_ReturnsPartitionError() {
      Exception cause =
          new RuntimeException(
              "no partition of relation found, also 23505 unique violation happened");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertNotNull(result);
      assertEquals("PARTITION_NOT_FOUND", result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Constraint Message Mapping Tests")
  class ConstraintMessageMappingTests {

    @Test
    @DisplayName("Should map experiment_key_unique_check to correct message")
    void testConstraintMapping_ExperimentKeyUniqueCheck() {
      Exception cause =
          new RuntimeException(
              "duplicate key value violates unique constraint \"experiment_key_unique_check\"");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertTrue(result.getErrorMessage().contains("experiment_key already exists"));
    }

    @Test
    @DisplayName("Should map experiment_key_key to correct message")
    void testConstraintMapping_ExperimentKeyKey() {
      Exception cause =
          new RuntimeException(
              "duplicate key value violates unique constraint \"experiment_key_key\"");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertTrue(result.getErrorMessage().contains("experiment_key already exists"));
    }

    @Test
    @DisplayName("Should map name_unique_check to correct message")
    void testConstraintMapping_NameUniqueCheck() {
      Exception cause =
          new RuntimeException(
              "duplicate key value violates unique constraint \"name_unique_check\"");
      RestException result =
          DbExceptionUtil.handleDbError(cause, ErrorEnum.REST_HEALTH_CHECK_FAILED);

      assertTrue(result.getErrorMessage().contains("name already exists"));
    }
  }
}
