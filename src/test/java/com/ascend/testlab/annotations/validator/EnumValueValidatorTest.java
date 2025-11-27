package com.ascend.testlab.annotations.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("EnumValueValidator Tests")
@ExtendWith(MockitoExtension.class)
class EnumValueValidatorTest {

  private EnumValueValidator validator;

  @Mock private ConstraintValidatorContext context;

  @Mock private ValidEnumValue annotation;

  @BeforeEach
  void setUp() {
    validator = new EnumValueValidator();
  }

  @Nested
  @DisplayName("Initialization Tests")
  class InitializationTests {

    @Test
    @DisplayName("Should initialize with enum class and method name")
    void testInitialize() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      // Verify that initialization doesn't throw exception
      assertDoesNotThrow(() -> validator.isValid("LIVE", context));
    }

    @Test
    @DisplayName("Should initialize with custom method name")
    void testInitialize_CustomMethod() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("toString");

      validator.initialize(annotation);

      assertDoesNotThrow(() -> validator.isValid("LIVE", context));
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should return true for null value")
    void testIsValid_NullValue() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Should return true for valid enum value")
    void testIsValid_ValidValue() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertTrue(validator.isValid("LIVE", context));
      assertTrue(validator.isValid("DRAFT", context));
      assertTrue(validator.isValid("PAUSED", context));
    }

    @Test
    @DisplayName("Should return false for invalid enum value")
    void testIsValid_InvalidValue() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertFalse(validator.isValid("INVALID_STATUS", context));
      assertFalse(validator.isValid("UNKNOWN", context));
    }

    @Test
    @DisplayName("Should handle case sensitivity")
    void testIsValid_CaseSensitive() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertTrue(validator.isValid("LIVE", context));
      assertFalse(validator.isValid("live", context));
      assertFalse(validator.isValid("Live", context));
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should throw RuntimeException for invalid method name")
    void testIsValid_InvalidMethod() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("nonExistentMethod");

      validator.initialize(annotation);

      assertThrows(RuntimeException.class, () -> validator.isValid("LIVE", context));
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle empty string")
    void testIsValid_EmptyString() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("Should handle numeric value")
    void testIsValid_NumericValue() {
      when(annotation.enumClass()).thenReturn((Class) ExperimentStatus.class);
      when(annotation.method()).thenReturn("name");

      validator.initialize(annotation);

      assertFalse(validator.isValid(123, context));
    }
  }
}
