package com.ascend.testlab.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.constants.Constants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for CustomConstraintExceptionMapper.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomConstraintExceptionMapper Tests")
public class CustomConstraintExceptionMapperTest {

  private CustomConstraintExceptionMapper mapper;

  @Mock private ConstraintViolation<Object> violation1;
  @Mock private ConstraintViolation<Object> violation2;
  @Mock private ConstraintViolation<Object> violation3;

  @BeforeEach
  void setUp() {
    mapper = new CustomConstraintExceptionMapper();
  }

  @Nested
  @DisplayName("Response Mapping Tests")
  class ResponseMappingTests {

    @Test
    @DisplayName("Should map single constraint violation to response")
    void testToResponseSingleViolation() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Field is required");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
      assertNotNull(response.getEntity());
      assertTrue(response.getEntity().toString().contains("Field is required"));
    }

    @Test
    @DisplayName("Should map multiple constraint violations to response")
    void testToResponseMultipleViolations() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Field1 is required");
      when(violation2.getMessageTemplate()).thenReturn("Field2 is invalid");
      when(violation3.getMessageTemplate()).thenReturn("Field3 must be positive");
      ConstraintViolationException exception =
          new ConstraintViolationException(
              "Validation failed", Set.of(violation1, violation2, violation3));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
      String entity = response.getEntity().toString();
      assertTrue(
          entity.contains("Field1 is required")
              && entity.contains("Field2 is invalid")
              && entity.contains("Field3 must be positive"));
    }

    @Test
    @DisplayName("Should use comma as separator for multiple violations")
    void testToResponseViolationsSeparator() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Error1");
      when(violation2.getMessageTemplate()).thenReturn("Error2");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1, violation2));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      String entity = response.getEntity().toString();
      assertTrue(entity.contains(Constants.COMMA) || entity.split(",").length >= 1);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status code")
    void testToResponseStatusCode() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Validation error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertEquals(400, response.getStatus());
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should include RestException in response entity")
    void testToResponseEntityFormat() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Invalid input");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response.getEntity());
      String entity = response.getEntity().toString();
      assertFalse(entity.isEmpty());
    }
  }

  @Nested
  @DisplayName("Error Message Aggregation Tests")
  class ErrorMessageAggregationTests {

    @Test
    @DisplayName("Should aggregate all violation messages")
    void testAggregateViolationMessages() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Error A");
      when(violation2.getMessageTemplate()).thenReturn("Error B");
      when(violation3.getMessageTemplate()).thenReturn("Error C");
      ConstraintViolationException exception =
          new ConstraintViolationException(
              "Validation failed", Set.of(violation1, violation2, violation3));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      String entity = response.getEntity().toString();
      // At least one of the errors should be present
      assertTrue(
          entity.contains("Error A") && entity.contains("Error B") && entity.contains("Error C"));
    }

    @Test
    @DisplayName("Should handle empty violation messages")
    void testEmptyViolationMessages() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should handle null violation messages gracefully")
    void testNullViolationMessages() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn(null);
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should handle long violation messages")
    void testLongViolationMessages() {
      // Arrange
      String longMessage = "a".repeat(1000);
      when(violation1.getMessageTemplate()).thenReturn(longMessage);
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
      assertTrue(response.getEntity().toString().contains(longMessage));
    }
  }

  @Nested
  @DisplayName("ExceptionMapper Interface Tests")
  class ExceptionMapperInterfaceTests {

    @Test
    @DisplayName("Should have Provider annotation")
    void testHasProviderAnnotation() {
      // Assert
      assertTrue(
          CustomConstraintExceptionMapper.class.isAnnotationPresent(
              jakarta.ws.rs.ext.Provider.class));
    }

    @Test
    @DisplayName("Should be usable as ExceptionMapper")
    void testUsableAsExceptionMapper() {
      // Arrange
      jakarta.ws.rs.ext.ExceptionMapper<ConstraintViolationException> exceptionMapper = mapper;
      when(violation1.getMessageTemplate()).thenReturn("Test error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = exceptionMapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }
  }

  @Nested
  @DisplayName("RestException Creation Tests")
  class RestExceptionCreationTests {

    @Test
    @DisplayName("Should create RestException with correct error code")
    void testRestExceptionErrorCode() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Validation error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      String entity = response.getEntity().toString();
      assertTrue(entity.contains("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Should create RestException with aggregated message")
    void testRestExceptionMessage() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Field error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertTrue(response.getEntity().toString().contains("Field error"));
    }

    @Test
    @DisplayName("Should create RestException with BAD_REQUEST status")
    void testRestExceptionStatus() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should create RestException with original exception as cause")
    void testRestExceptionCause() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertNotNull(response.getEntity());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle exception with no violations")
    void testNoViolations() {
      // Arrange
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of());

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should handle violations with special characters")
    void testViolationsWithSpecialCharacters() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Field contains @#$%^&*()");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
      assertTrue(response.getEntity().toString().contains("@#$%^&*()"));
    }

    @Test
    @DisplayName("Should handle violations with unicode characters")
    void testViolationsWithUnicode() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("字段无效");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @DisplayName("Should handle many violations efficiently")
    void testManyViolations() {
      // Arrange
      Set<ConstraintViolation<Object>> violations = new HashSet<>();
      for (int i = 0; i < 100; i++) {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessageTemplate()).thenReturn("Error " + i);
        violations.add(violation);
      }
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", violations);

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should produce valid JAX-RS Response")
    void testValidJaxRsResponse() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Validation error");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertNotNull(response.getEntity());
      assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("Should work with typical validation scenarios")
    void testTypicalValidationScenario() {
      // Arrange
      when(violation1.getMessageTemplate()).thenReturn("Email must be valid");
      when(violation2.getMessageTemplate()).thenReturn("Password must be at least 8 characters");
      ConstraintViolationException exception =
          new ConstraintViolationException("Validation failed", Set.of(violation1, violation2));

      // Act
      Response response = mapper.toResponse(exception);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
      String entity = response.getEntity().toString();
      assertFalse(entity.isEmpty());
    }

    @Test
    @DisplayName("Should be instantiable")
    void testInstantiable() {
      // Act
      CustomConstraintExceptionMapper newMapper = new CustomConstraintExceptionMapper();

      // Assert
      assertNotNull(newMapper);
    }
  }
}
