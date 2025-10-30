package com.ascend.testlab.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.dream11.rest.exception.RestException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for ErrorEnum.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ErrorEnum Tests")
public class ErrorEnumTest {

  @Nested
  @DisplayName("Enum Constants Tests")
  class EnumConstantsTests {

    @Test
    @DisplayName("Should have REST_HEALTH_CHECK_FAILED constant")
    void testHealthCheckFailedConstant() {
      // Act
      ErrorEnum error = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertNotNull(error);
      assertEquals("testlab_REST_HEALTH_CHECK_FAILED", error.getErrorCode());
      assertEquals("HealthCheck Failed for testlab service", error.getErrorMessage());
      assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, error.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should return correct HTTP status code")
    void testHttpStatusCode() {
      // Act
      ErrorEnum error = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertEquals(500, error.getHttpStatusCode());
      assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, error.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should have valid error code format")
    void testErrorCodeFormat() {
      // Act
      ErrorEnum error = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertTrue(error.getErrorCode().startsWith("testlab_"));
      assertTrue(error.getErrorCode().contains("REST_HEALTH_CHECK_FAILED"));
    }

    @Test
    @DisplayName("Should have descriptive error message")
    void testErrorMessage() {
      // Act
      ErrorEnum error = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertNotNull(error.getErrorMessage());
      assertFalse(error.getErrorMessage().isEmpty());
      assertTrue(error.getErrorMessage().contains("HealthCheck"));
      assertTrue(error.getErrorMessage().contains("testlab"));
    }
  }

  @Nested
  @DisplayName("Getter Tests")
  class GetterTests {

    @Test
    @DisplayName("Should get error code")
    void testGetErrorCode() {
      // Act
      String errorCode = ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode();

      // Assert
      assertNotNull(errorCode);
      assertEquals("testlab_REST_HEALTH_CHECK_FAILED", errorCode);
    }

    @Test
    @DisplayName("Should get error message")
    void testGetErrorMessage() {
      // Act
      String errorMessage = ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage();

      // Assert
      assertNotNull(errorMessage);
      assertEquals("HealthCheck Failed for testlab service", errorMessage);
    }

    @Test
    @DisplayName("Should get HTTP status code")
    void testGetHttpStatusCode() {
      // Act
      int httpStatusCode = ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode();

      // Assert
      assertEquals(500, httpStatusCode);
    }
  }

  @Nested
  @DisplayName("Exception Handling Tests")
  class ExceptionHandlingTests {

    @Test
    @DisplayName("Should return RestException when throwable is RestException")
    void testHandleRestException() {
      // Arrange
      RestException restException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Test error"));
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(restException, defaultException);

      // Assert
      assertSame(restException, result);
      assertNotSame(defaultException, result);
    }

    @Test
    @DisplayName("Should return default exception when throwable is not RestException")
    void testHandleNonRestException() {
      // Arrange
      RuntimeException runtimeException = new RuntimeException("Runtime error");
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(runtimeException, defaultException);

      // Assert
      assertSame(defaultException, result);
      assertNotSame(runtimeException, result);
    }

    @Test
    @DisplayName("Should handle NullPointerException")
    void testHandleNullPointerException() {
      // Arrange
      NullPointerException npe = new NullPointerException("Null error");
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(npe, defaultException);

      // Assert
      assertSame(defaultException, result);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
      // Arrange
      IllegalArgumentException iae = new IllegalArgumentException("Invalid argument");
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(iae, defaultException);

      // Assert
      assertSame(defaultException, result);
    }

    @Test
    @DisplayName("Should handle generic Throwable")
    void testHandleGenericThrowable() {
      // Arrange
      Throwable throwable = new Throwable("Generic error");
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(throwable, defaultException);

      // Assert
      assertSame(defaultException, result);
    }

    @Test
    @DisplayName("Should handle Error subclass")
    void testHandleError() {
      // Arrange
      Error error = new AssertionError("Assertion failed");
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(error, defaultException);

      // Assert
      assertSame(defaultException, result);
    }

    @Test
    @DisplayName("Should handle nested RestException")
    void testHandleNestedRestException() {
      // Arrange
      RestException innerException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Inner error"));
      RestException outerException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              innerException);
      RestException defaultException =
          new RestException(
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorCode(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getErrorMessage(),
              ErrorEnum.REST_HEALTH_CHECK_FAILED.getHttpStatusCode(),
              new RuntimeException("Default error"));

      // Act
      RestException result = ErrorEnum.handleException(outerException, defaultException);

      // Assert
      assertSame(outerException, result);
      assertNotSame(defaultException, result);
    }
  }

  @Nested
  @DisplayName("Enum Methods Tests")
  class EnumMethodsTests {

    @Test
    @DisplayName("Should support valueOf")
    void testValueOf() {
      // Act
      ErrorEnum error = ErrorEnum.valueOf("REST_HEALTH_CHECK_FAILED");

      // Assert
      assertNotNull(error);
      assertEquals(ErrorEnum.REST_HEALTH_CHECK_FAILED, error);
    }

    @Test
    @DisplayName("Should support values")
    void testValues() {
      // Act
      ErrorEnum[] values = ErrorEnum.values();

      // Assert
      assertNotNull(values);
      assertTrue(values.length > 0);
      assertEquals(ErrorEnum.REST_HEALTH_CHECK_FAILED, values[0]);
    }

    @Test
    @DisplayName("Should support name")
    void testName() {
      // Act
      String name = ErrorEnum.REST_HEALTH_CHECK_FAILED.name();

      // Assert
      assertEquals("REST_HEALTH_CHECK_FAILED", name);
    }

    @Test
    @DisplayName("Should support ordinal")
    void testOrdinal() {
      // Act
      int ordinal = ErrorEnum.REST_HEALTH_CHECK_FAILED.ordinal();

      // Assert
      assertEquals(0, ordinal);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid valueOf")
    void testValueOfInvalid() {
      // Act & Assert
      assertThrows(IllegalArgumentException.class, () -> ErrorEnum.valueOf("INVALID_ERROR"));
    }
  }

  @Nested
  @DisplayName("RestError Interface Tests")
  class RestErrorInterfaceTests {

    @Test
    @DisplayName("Should be usable as RestError")
    void testUsableAsRestError() {
      // Act
      com.dream11.rest.exception.RestError restError = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertNotNull(restError);
      assertEquals("testlab_REST_HEALTH_CHECK_FAILED", restError.getErrorCode());
      assertEquals("HealthCheck Failed for testlab service", restError.getErrorMessage());
      assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, restError.getHttpStatusCode());
    }
  }

  @Nested
  @DisplayName("Enum Comparison Tests")
  class EnumComparisonTests {

    @Test
    @DisplayName("Should support equality comparison")
    void testEquality() {
      // Act
      ErrorEnum error1 = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      ErrorEnum error2 = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertSame(error1, error2);
      assertEquals(error1, error2);
    }

    @Test
    @DisplayName("Should support identity comparison")
    void testIdentity() {
      // Act
      ErrorEnum error1 = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      ErrorEnum error2 = ErrorEnum.valueOf("REST_HEALTH_CHECK_FAILED");

      // Assert
      assertSame(error1, error2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
      // Act
      ErrorEnum error1 = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      ErrorEnum error2 = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Assert
      assertEquals(error1.hashCode(), error2.hashCode());
    }

    @Test
    @DisplayName("Should have consistent toString")
    void testToString() {
      // Act
      String toString = ErrorEnum.REST_HEALTH_CHECK_FAILED.toString();

      // Assert
      assertNotNull(toString);
      assertEquals("REST_HEALTH_CHECK_FAILED", toString);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should create RestException with ErrorEnum")
    void testCreateRestException() {
      // Arrange
      ErrorEnum errorEnum = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Act
      RestException exception =
          new RestException(
              errorEnum.getErrorCode(),
              errorEnum.getErrorMessage(),
              errorEnum.getHttpStatusCode(),
              new RuntimeException("Health check failed"));

      // Assert
      assertNotNull(exception);
      assertEquals(errorEnum.getErrorCode(), exception.getErrorCode());
      assertEquals(errorEnum.getErrorMessage(), exception.getErrorMessage());
      assertEquals(errorEnum.getHttpStatusCode(), exception.getHttpStatusCode());
    }

    @Test
    @DisplayName("Should handle exception chain")
    void testExceptionChain() {
      // Arrange
      Exception originalException = new Exception("Original error");
      ErrorEnum errorEnum = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      RestException restException =
          new RestException(
              errorEnum.getErrorCode(),
              errorEnum.getErrorMessage(),
              errorEnum.getHttpStatusCode(),
              originalException);
      RestException defaultException =
          new RestException(
              errorEnum.getErrorCode(),
              errorEnum.getErrorMessage(),
              errorEnum.getHttpStatusCode(),
              new RuntimeException("Default"));

      // Act
      RestException result1 = ErrorEnum.handleException(restException, defaultException);
      RestException result2 = ErrorEnum.handleException(originalException, defaultException);

      // Assert
      assertSame(restException, result1);
      assertSame(defaultException, result2);
    }

    @Test
    @DisplayName("Should use error enum in typical error handling flow")
    void testTypicalErrorFlow() {
      // Arrange
      ErrorEnum errorEnum = ErrorEnum.REST_HEALTH_CHECK_FAILED;

      // Act
      RestException exception =
          new RestException(
              errorEnum.getErrorCode(),
              errorEnum.getErrorMessage(),
              errorEnum.getHttpStatusCode(),
              new RuntimeException("Service unavailable"));

      // Assert
      assertEquals("testlab_REST_HEALTH_CHECK_FAILED", exception.getErrorCode());
      assertEquals("HealthCheck Failed for testlab service", exception.getErrorMessage());
      assertEquals(500, exception.getHttpStatusCode());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null throwable in handleException")
    void testHandleNullThrowable() {
      // Arrange
      ErrorEnum errorEnum = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      RestException defaultException =
          new RestException(
              errorEnum.getErrorCode(),
              errorEnum.getErrorMessage(),
              errorEnum.getHttpStatusCode(),
              new RuntimeException("Default"));

      // Act
      RestException result = ErrorEnum.handleException(null, defaultException);

      // Assert - handleException returns defaultException when throwable is not a RestException
      assertSame(defaultException, result);
    }

    @Test
    @DisplayName("Should handle null default exception")
    void testHandleNullDefaultException() {
      // Arrange
      RuntimeException throwable = new RuntimeException("Error");

      // Act
      RestException result = ErrorEnum.handleException(throwable, null);

      // Assert
      assertNull(result);
    }

    @Test
    @DisplayName("Should maintain enum singleton property")
    void testEnumSingleton() {
      // Act
      ErrorEnum error1 = ErrorEnum.REST_HEALTH_CHECK_FAILED;
      ErrorEnum error2 = ErrorEnum.values()[0];
      ErrorEnum error3 = ErrorEnum.valueOf("REST_HEALTH_CHECK_FAILED");

      // Assert
      assertSame(error1, error2);
      assertSame(error2, error3);
      assertSame(error1, error3);
    }
  }
}
