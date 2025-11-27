package com.ascend.testlab.constants.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ConditionalOperator Tests")
class ConditionalOperatorTest {

  @Nested
  @DisplayName("Enum Values Tests")
  class EnumValuesTests {

    @Test
    @DisplayName("Should have AND operator with correct properties")
    void testAndOperator() {
      ConditionalOperator operator = ConditionalOperator.AND;
      assertEquals("AND", operator.getName());
      assertEquals(Boolean.TRUE, operator.getIdentity());
      assertNotNull(operator.getAccumulator());

      // Test accumulator functionality
      Boolean result = operator.getAccumulator().apply(true, true);
      assertTrue(result);

      result = operator.getAccumulator().apply(true, false);
      assertFalse(result);
    }

    @Test
    @DisplayName("Should have OR operator with correct properties")
    void testOrOperator() {
      ConditionalOperator operator = ConditionalOperator.OR;
      assertEquals("OR", operator.getName());
      assertEquals(Boolean.FALSE, operator.getIdentity());
      assertNotNull(operator.getAccumulator());

      // Test accumulator functionality
      Boolean result = operator.getAccumulator().apply(false, false);
      assertFalse(result);

      result = operator.getAccumulator().apply(true, false);
      assertTrue(result);
    }
  }

  @Nested
  @DisplayName("getOperator Tests")
  class GetOperatorTests {

    @Test
    @DisplayName("Should return AND for 'AND' string")
    void testGetOperator_And() {
      ConditionalOperator operator = ConditionalOperator.getOperator("AND");
      assertEquals(ConditionalOperator.AND, operator);
    }

    @Test
    @DisplayName("Should return OR for 'OR' string")
    void testGetOperator_Or() {
      ConditionalOperator operator = ConditionalOperator.getOperator("OR");
      assertEquals(ConditionalOperator.OR, operator);
    }

    @Test
    @DisplayName("Should throw exception for invalid operator")
    void testGetOperator_Invalid() {
      assertThrows(
          IllegalArgumentException.class, () -> ConditionalOperator.getOperator("INVALID"));
    }
  }
}
