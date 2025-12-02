package com.ascend.testlab.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConditionValueValidator.
 *
 * <p>Note: Currently, only basic validations exist for Condition fields (@NotBlank, @ValidEnumValue
 * for operand/operator/operandDataType). Type-specific value validation is not yet implemented.
 * These tests verify the current validation behavior.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ConditionValueValidator Tests")
class ConditionValueValidatorTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Nested
  @DisplayName("Boolean Validation Tests")
  class BooleanValidationTests {

    @Test
    @DisplayName("Should accept 'true' for BOOL type")
    void testBooleanTrue() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("BOOL")
              .operator("=")
              .value("true")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept 'true' for BOOL type");
    }

    @Test
    @DisplayName("Should accept 'false' for BOOL type")
    void testBooleanFalse() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("BOOL")
              .operator("=")
              .value("false")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept 'false' for BOOL type");
    }

    @Test
    @DisplayName("Should accept case-insensitive boolean values")
    void testBooleanCaseInsensitive() {
      RuleAttributes.Condition condition1 =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("BOOL")
              .operator("=")
              .value("TRUE")
              .build();

      RuleAttributes.Condition condition2 =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("BOOL")
              .operator("=")
              .value("False")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations1 =
          validator.validate(condition1);
      Set<ConstraintViolation<RuleAttributes.Condition>> violations2 =
          validator.validate(condition2);

      assertTrue(violations1.isEmpty(), "Should accept 'TRUE' for BOOL type");
      assertTrue(violations2.isEmpty(), "Should accept 'False' for BOOL type");
    }

    @Test
    @DisplayName(
        "Should accept any non-blank value for BOOL type - no type-specific validation yet")
    void testInvalidBoolean() {
      // Currently, no type-specific validation exists for BOOL
      // Any non-blank value is accepted
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("BOOL")
              .operator("=")
              .value("yes")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value (type-specific validation not implemented)
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for BOOL type");
    }
  }

  @Nested
  @DisplayName("Number Validation Tests")
  class NumberValidationTests {

    @Test
    @DisplayName("Should accept valid integer for NUMBER type")
    void testValidInteger() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("NUMBER")
              .operator(">")
              .value("25")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept valid integer");
    }

    @Test
    @DisplayName("Should accept negative integer for NUMBER type")
    void testNegativeInteger() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("NUMBER")
              .operator("<")
              .value("-10")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept negative integer");
    }

    @Test
    @DisplayName("Should accept decimal for NUMBER type - no type-specific validation yet")
    void testDecimalForNumber() {
      // Currently, no type-specific validation exists
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("NUMBER")
              .operator(">")
              .value("25.5")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for NUMBER type");
    }

    @Test
    @DisplayName(
        "Should accept non-numeric string for NUMBER type - no type-specific validation yet")
    void testInvalidString() {
      // Currently, no type-specific validation exists
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("NUMBER")
              .operator(">")
              .value("abc")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for NUMBER type");
    }
  }

  @Nested
  @DisplayName("Decimal Validation Tests")
  class DecimalValidationTests {

    @Test
    @DisplayName("Should accept valid decimal for DECIMAL type")
    void testValidDecimal() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("DECIMAL")
              .operator(">=")
              .value("99.99")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept valid decimal");
    }

    @Test
    @DisplayName("Should accept integer for DECIMAL type")
    void testIntegerForDecimal() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("DECIMAL")
              .operator(">=")
              .value("100")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept integer for DECIMAL type");
    }

    @Test
    @DisplayName(
        "Should accept non-numeric string for DECIMAL type - no type-specific validation yet")
    void testInvalidDecimal() {
      // Currently, no type-specific validation exists
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("build_number")
              .operandDataType("DECIMAL")
              .operator(">=")
              .value("abc")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for DECIMAL type");
    }
  }

  @Nested
  @DisplayName("String Validation Tests")
  class StringValidationTests {

    @Test
    @DisplayName("Should accept any non-empty string for STRING type")
    void testValidString() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("STRING")
              .operator("=")
              .value("android")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept any non-empty string");
    }

    @Test
    @DisplayName("Should reject empty string for STRING type")
    void testEmptyString() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("STRING")
              .operator("=")
              .value("   ")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertFalse(violations.isEmpty(), "Should reject whitespace-only string");
    }
  }

  @Nested
  @DisplayName("SemVer Validation Tests")
  class SemVerValidationTests {

    @Test
    @DisplayName("Should accept valid semantic version")
    void testValidSemVer() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("app_version")
              .operandDataType("SEMVER_STRING")
              .operator(">=")
              .value("1.2.3")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept valid semantic version");
    }

    @Test
    @DisplayName("Should accept semantic version with pre-release")
    void testSemVerWithPreRelease() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("app_version")
              .operandDataType("SEMVER_STRING")
              .operator(">=")
              .value("2.0.0-beta.1")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept semantic version with pre-release");
    }

    @Test
    @DisplayName("Should accept invalid semver - no type-specific validation yet")
    void testInvalidSemVer() {
      // Currently, no type-specific validation exists for SEMVER_STRING
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("app_version")
              .operandDataType("SEMVER_STRING")
              .operator(">=")
              .value("1.2")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(
          violations.isEmpty(), "Currently accepts any non-blank value for SEMVER_STRING type");
    }
  }

  @Nested
  @DisplayName("Object Validation Tests")
  class ObjectValidationTests {

    @Test
    @DisplayName("Should accept valid JSON object")
    void testValidObject() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("OBJECT")
              .operator("=")
              .value("{\"key\": \"value\"}")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept valid JSON object");
    }

    @Test
    @DisplayName("Should accept invalid JSON object - basic validator only checks non-blank")
    void testInvalidObject() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("OBJECT")
              .operator("=")
              .value("{key: value}")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Basic validator accepts any non-blank value
      assertTrue(violations.isEmpty(), "Basic validator accepts balanced braces");
    }

    @Test
    @DisplayName("Should accept non-object string - no type-specific validation yet")
    void testNonObject() {
      // Currently, no type-specific validation exists for OBJECT
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("OBJECT")
              .operator("=")
              .value("not an object")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for OBJECT type");
    }
  }

  @Nested
  @DisplayName("List Validation Tests")
  class ListValidationTests {

    @Test
    @DisplayName("Should accept valid JSON array")
    void testValidList() {
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("LIST")
              .operator("contains")
              .value("[\"tag1\", \"tag2\"]")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      assertTrue(violations.isEmpty(), "Should accept valid JSON array");
    }

    @Test
    @DisplayName("Should accept non-array string - no type-specific validation yet")
    void testNonList() {
      // Currently, no type-specific validation exists for LIST
      RuleAttributes.Condition condition =
          RuleAttributes.Condition.builder()
              .operand("platform")
              .operandDataType("LIST")
              .operator("contains")
              .value("not a list")
              .build();

      Set<ConstraintViolation<RuleAttributes.Condition>> violations = validator.validate(condition);
      // Currently accepts any non-blank value
      assertTrue(violations.isEmpty(), "Currently accepts any non-blank value for LIST type");
    }
  }
}
