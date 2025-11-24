package com.ascend.testlab.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for ResponseEntity.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ResponseEntity Tests")
public class ResponseEntityTest {

  @Nested
  @DisplayName("Success Tests")
  class SuccessTests {

    @Test
    @DisplayName("Should create Success with string data")
    void testSuccessWithString() {
      // Arrange
      String data = "Success message";

      // Act
      ResponseEntity.Success<String> success = new ResponseEntity.Success<>(data);

      // Assert
      assertNotNull(success);
      assertEquals(data, success.data());
    }

    @Test
    @DisplayName("Should create Success with integer data")
    void testSuccessWithInteger() {
      // Arrange
      Integer data = 42;

      // Act
      ResponseEntity.Success<Integer> success = new ResponseEntity.Success<>(data);

      // Assert
      assertNotNull(success);
      assertEquals(data, success.data());
    }

    @Test
    @DisplayName("Should create Success with custom object")
    void testSuccessWithCustomObject() {
      // Arrange
      TestData data = new TestData("test", 123);

      // Act
      ResponseEntity.Success<TestData> success = new ResponseEntity.Success<>(data);

      // Assert
      assertNotNull(success);
      assertEquals(data, success.data());
      assertEquals("test", success.data().name);
      assertEquals(123, success.data().value);
    }

    @Test
    @DisplayName("Should create Success with null data")
    void testSuccessWithNull() {
      // Act
      ResponseEntity.Success<String> success = new ResponseEntity.Success<>(null);

      // Assert
      assertNotNull(success);
      assertNull(success.data());
    }

    @Test
    @DisplayName("Should support equality for Success records")
    void testSuccessEquality() {
      // Arrange
      ResponseEntity.Success<String> success1 = new ResponseEntity.Success<>("data");
      ResponseEntity.Success<String> success2 = new ResponseEntity.Success<>("data");
      ResponseEntity.Success<String> success3 = new ResponseEntity.Success<>("different");

      // Assert
      assertEquals(success1, success2);
      assertNotEquals(success1, success3);
    }

    @Test
    @DisplayName("Should support hashCode for Success records")
    void testSuccessHashCode() {
      // Arrange
      ResponseEntity.Success<String> success1 = new ResponseEntity.Success<>("data");
      ResponseEntity.Success<String> success2 = new ResponseEntity.Success<>("data");

      // Assert
      assertEquals(success1.hashCode(), success2.hashCode());
    }

    @Test
    @DisplayName("Should support toString for Success records")
    void testSuccessToString() {
      // Arrange
      ResponseEntity.Success<String> success = new ResponseEntity.Success<>("test data");

      // Act
      String toString = success.toString();

      // Assert
      assertNotNull(toString);
      assertTrue(toString.contains("test data"));
    }

    @Test
    @DisplayName("Should create Success with array data")
    void testSuccessWithArray() {
      // Arrange
      String[] data = {"item1", "item2", "item3"};

      // Act
      ResponseEntity.Success<String[]> success = new ResponseEntity.Success<>(data);

      // Assert
      assertNotNull(success);
      assertArrayEquals(data, success.data());
    }

    @Test
    @DisplayName("Should create Success with nested Success")
    void testSuccessWithNestedSuccess() {
      // Arrange
      ResponseEntity.Success<String> innerSuccess = new ResponseEntity.Success<>("inner");
      ResponseEntity.Success<ResponseEntity.Success<String>> outerSuccess =
          new ResponseEntity.Success<>(innerSuccess);

      // Assert
      assertNotNull(outerSuccess);
      assertEquals(innerSuccess, outerSuccess.data());
      assertEquals("inner", outerSuccess.data().data());
    }
  }

  @Nested
  @DisplayName("Failure Tests")
  class FailureTests {

    @Test
    @DisplayName("Should create Failure with all fields")
    void testFailureWithAllFields() {
      // Arrange
      String code = "ERROR_001";
      String message = "An error occurred";
      String cause = "Invalid input";

      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure(code, message, cause);

      // Assert
      assertNotNull(failure);
      assertNotNull(failure.getError());
      assertEquals(code, failure.getError().code());
      assertEquals(message, failure.getError().message());
      assertEquals(cause, failure.getError().cause());
    }

    @Test
    @DisplayName("Should create Failure with null code")
    void testFailureWithNullCode() {
      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure(null, "message", "cause");

      // Assert
      assertNotNull(failure);
      assertNull(failure.getError().code());
      assertEquals("message", failure.getError().message());
      assertEquals("cause", failure.getError().cause());
    }

    @Test
    @DisplayName("Should create Failure with null message")
    void testFailureWithNullMessage() {
      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure("CODE", null, "cause");

      // Assert
      assertNotNull(failure);
      assertEquals("CODE", failure.getError().code());
      assertNull(failure.getError().message());
      assertEquals("cause", failure.getError().cause());
    }

    @Test
    @DisplayName("Should create Failure with null cause")
    void testFailureWithNullCause() {
      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure("CODE", "message", null);

      // Assert
      assertNotNull(failure);
      assertEquals("CODE", failure.getError().code());
      assertEquals("message", failure.getError().message());
      assertNull(failure.getError().cause());
    }

    @Test
    @DisplayName("Should create Failure with all nulls")
    void testFailureWithAllNulls() {
      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure(null, null, null);

      // Assert
      assertNotNull(failure);
      assertNotNull(failure.getError());
      assertNull(failure.getError().code());
      assertNull(failure.getError().message());
      assertNull(failure.getError().cause());
    }

    @Test
    @DisplayName("Should have getter for error entity")
    void testFailureGetError() {
      // Arrange
      ResponseEntity.Failure failure =
          new ResponseEntity.Failure("ERR_001", "Error message", "Error cause");

      // Act
      ResponseEntity.Failure.ErrorEntity error = failure.getError();

      // Assert
      assertNotNull(error);
      assertEquals("ERR_001", error.code());
      assertEquals("Error message", error.message());
      assertEquals("Error cause", error.cause());
    }

    @Test
    @DisplayName("Should create Failure with empty strings")
    void testFailureWithEmptyStrings() {
      // Act
      ResponseEntity.Failure failure = new ResponseEntity.Failure("", "", "");

      // Assert
      assertNotNull(failure);
      assertEquals("", failure.getError().code());
      assertEquals("", failure.getError().message());
      assertEquals("", failure.getError().cause());
    }

    @Test
    @DisplayName("Should create Failure with long strings")
    void testFailureWithLongStrings() {
      // Arrange
      String longString = "a".repeat(1000);

      // Act
      ResponseEntity.Failure failure =
          new ResponseEntity.Failure(longString, longString, longString);

      // Assert
      assertNotNull(failure);
      assertEquals(1000, failure.getError().code().length());
      assertEquals(1000, failure.getError().message().length());
      assertEquals(1000, failure.getError().cause().length());
    }
  }

  @Nested
  @DisplayName("ErrorEntity Tests")
  class ErrorEntityTests {

    @Test
    @DisplayName("Should create ErrorEntity with all fields")
    void testErrorEntityCreation() {
      // Arrange
      String code = "ERR_001";
      String message = "Error occurred";
      String cause = "Invalid state";

      // Act
      ResponseEntity.Failure.ErrorEntity error =
          new ResponseEntity.Failure.ErrorEntity(code, message, cause);

      // Assert
      assertNotNull(error);
      assertEquals(code, error.code());
      assertEquals(message, error.message());
      assertEquals(cause, error.cause());
    }

    @Test
    @DisplayName("Should support equality for ErrorEntity records")
    void testErrorEntityEquality() {
      // Arrange
      ResponseEntity.Failure.ErrorEntity error1 =
          new ResponseEntity.Failure.ErrorEntity("CODE", "message", "cause");
      ResponseEntity.Failure.ErrorEntity error2 =
          new ResponseEntity.Failure.ErrorEntity("CODE", "message", "cause");
      ResponseEntity.Failure.ErrorEntity error3 =
          new ResponseEntity.Failure.ErrorEntity("DIFF", "message", "cause");

      // Assert
      assertEquals(error1, error2);
      assertNotEquals(error1, error3);
    }

    @Test
    @DisplayName("Should support hashCode for ErrorEntity records")
    void testErrorEntityHashCode() {
      // Arrange
      ResponseEntity.Failure.ErrorEntity error1 =
          new ResponseEntity.Failure.ErrorEntity("CODE", "message", "cause");
      ResponseEntity.Failure.ErrorEntity error2 =
          new ResponseEntity.Failure.ErrorEntity("CODE", "message", "cause");

      // Assert
      assertEquals(error1.hashCode(), error2.hashCode());
    }

    @Test
    @DisplayName("Should support toString for ErrorEntity records")
    void testErrorEntityToString() {
      // Arrange
      ResponseEntity.Failure.ErrorEntity error =
          new ResponseEntity.Failure.ErrorEntity("ERR_001", "Error message", "Cause");

      // Act
      String toString = error.toString();

      // Assert
      assertNotNull(toString);
      assertTrue(toString.contains("ERR_001"));
      assertTrue(toString.contains("Error message"));
      assertTrue(toString.contains("Cause"));
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should create multiple Success instances")
    void testMultipleSuccessInstances() {
      // Act
      ResponseEntity.Success<String> success1 = new ResponseEntity.Success<>("data1");
      ResponseEntity.Success<Integer> success2 = new ResponseEntity.Success<>(123);
      ResponseEntity.Success<Boolean> success3 = new ResponseEntity.Success<>(true);

      // Assert
      assertEquals("data1", success1.data());
      assertEquals(123, success2.data());
      assertTrue(success3.data());
    }

    @Test
    @DisplayName("Should create multiple Failure instances")
    void testMultipleFailureInstances() {
      // Act
      ResponseEntity.Failure failure1 = new ResponseEntity.Failure("ERR1", "msg1", "cause1");
      ResponseEntity.Failure failure2 = new ResponseEntity.Failure("ERR2", "msg2", "cause2");

      // Assert
      assertEquals("ERR1", failure1.getError().code());
      assertEquals("ERR2", failure2.getError().code());
      assertNotEquals(failure1.getError(), failure2.getError());
    }

    @Test
    @DisplayName("Should handle typical REST API success response")
    void testTypicalSuccessResponse() {
      // Arrange
      TestData responseData = new TestData("user123", 200);

      // Act
      ResponseEntity.Success<TestData> success = new ResponseEntity.Success<>(responseData);

      // Assert
      assertNotNull(success);
      assertNotNull(success.data());
      assertEquals("user123", success.data().name);
      assertEquals(200, success.data().value);
    }

    @Test
    @DisplayName("Should handle typical REST API error response")
    void testTypicalErrorResponse() {
      // Act
      ResponseEntity.Failure failure =
          new ResponseEntity.Failure("AUTH_FAILED", "Authentication failed", "Invalid credentials");

      // Assert
      assertNotNull(failure);
      assertNotNull(failure.getError());
      assertEquals("AUTH_FAILED", failure.getError().code());
      assertEquals("Authentication failed", failure.getError().message());
      assertEquals("Invalid credentials", failure.getError().cause());
    }
  }

  @Nested
  @DisplayName("Type Safety Tests")
  class TypeSafetyTests {

    @Test
    @DisplayName("Should support generic collections in Success")
    void testSuccessWithCollections() {
      // Arrange
      List<String> list = Arrays.asList("a", "b", "c");

      // Act
      ResponseEntity.Success<List<String>> success = new ResponseEntity.Success<>(list);

      // Assert
      assertNotNull(success);
      assertEquals(3, success.data().size());
      assertTrue(success.data().contains("a"));
    }

    @Test
    @DisplayName("Should support generic maps in Success")
    void testSuccessWithMaps() {
      // Arrange
      Map<String, Integer> map = new HashMap<>();
      map.put("key1", 1);
      map.put("key2", 2);

      // Act
      ResponseEntity.Success<Map<String, Integer>> success = new ResponseEntity.Success<>(map);

      // Assert
      assertNotNull(success);
      assertEquals(2, success.data().size());
      assertEquals(1, success.data().get("key1"));
    }
  }

  // Helper test class
  private static class TestData {
    String name;
    int value;

    TestData(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
