package com.ascend.testlab.allocation.strategy.concludedexperiment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConcludedStrategyFactory.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ConcludedStrategyFactory Tests")
class ConcludedStrategyFactoryTest {

  @Nested
  @DisplayName("getStrategy Tests")
  class GetStrategyTests {

    @Test
    @DisplayName("Should return AssignedOnlyStrategy for ASSIGNED_ONLY type")
    void testGetStrategy_AssignedOnly_ReturnsAssignedOnlyStrategy() {
      ConcludedStrategy strategy =
          ConcludedStrategyFactory.getStrategy(ConcludedStrategyFactory.StrategyType.ASSIGNED_ONLY);

      assertNotNull(strategy);
      assertInstanceOf(AssignedOnlyStrategy.class, strategy);
      assertEquals("ASSIGNED_ONLY", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return UnassignedOnlyStrategy for UNASSIGNED_ONLY type")
    void testGetStrategy_UnassignedOnly_ReturnsUnassignedOnlyStrategy() {
      ConcludedStrategy strategy =
          ConcludedStrategyFactory.getStrategy(
              ConcludedStrategyFactory.StrategyType.UNASSIGNED_ONLY);

      assertNotNull(strategy);
      assertInstanceOf(UnassignedOnlyStrategy.class, strategy);
      assertEquals("UNASSIGNED_ONLY", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return DefaultStrategy for DEFAULT type")
    void testGetStrategy_Default_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy =
          ConcludedStrategyFactory.getStrategy(ConcludedStrategyFactory.StrategyType.DEFAULT);

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
      assertEquals("BOTH", strategy.getStrategyName());
    }
  }

  @Nested
  @DisplayName("getStrategyByName Tests")
  class GetStrategyByNameTests {

    @Test
    @DisplayName("Should return AssignedOnlyStrategy for 'ASSIGNED_ONLY' name")
    void testGetStrategyByName_AssignedOnly_ReturnsAssignedOnlyStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("ASSIGNED_ONLY");

      assertNotNull(strategy);
      assertInstanceOf(AssignedOnlyStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return UnassignedOnlyStrategy for 'UNASSIGNED_ONLY' name")
    void testGetStrategyByName_UnassignedOnly_ReturnsUnassignedOnlyStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("UNASSIGNED_ONLY");

      assertNotNull(strategy);
      assertInstanceOf(UnassignedOnlyStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return DefaultStrategy for 'DEFAULT' name")
    void testGetStrategyByName_Default_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("DEFAULT");

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should handle lowercase name and return correct strategy")
    void testGetStrategyByName_Lowercase_ReturnsCorrectStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("assigned_only");

      assertNotNull(strategy);
      assertInstanceOf(AssignedOnlyStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should handle mixed case name and return correct strategy")
    void testGetStrategyByName_MixedCase_ReturnsCorrectStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("Assigned_Only");

      assertNotNull(strategy);
      assertInstanceOf(AssignedOnlyStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return DefaultStrategy for invalid name")
    void testGetStrategyByName_InvalidName_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("INVALID_STRATEGY");

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return DefaultStrategy for empty name")
    void testGetStrategyByName_EmptyName_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getStrategyByName("");

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return DefaultStrategy for random string")
    void testGetStrategyByName_RandomString_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy =
          ConcludedStrategyFactory.getStrategyByName("some_random_strategy_name");

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
    }
  }

  @Nested
  @DisplayName("getDefaultStrategy Tests")
  class GetDefaultStrategyTests {

    @Test
    @DisplayName("Should return DefaultStrategy")
    void testGetDefaultStrategy_ReturnsDefaultStrategy() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getDefaultStrategy();

      assertNotNull(strategy);
      assertInstanceOf(DefaultStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return strategy with 'BOTH' name")
    void testGetDefaultStrategy_ReturnsStrategyWithBothName() {
      ConcludedStrategy strategy = ConcludedStrategyFactory.getDefaultStrategy();

      assertEquals("BOTH", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return new instance each time")
    void testGetDefaultStrategy_ReturnsNewInstanceEachTime() {
      ConcludedStrategy strategy1 = ConcludedStrategyFactory.getDefaultStrategy();
      ConcludedStrategy strategy2 = ConcludedStrategyFactory.getDefaultStrategy();

      assertNotSame(strategy1, strategy2);
    }
  }

  @Nested
  @DisplayName("StrategyType Enum Tests")
  class StrategyTypeEnumTests {

    @Test
    @DisplayName("Should have all expected strategy types")
    void testStrategyType_HasAllExpectedValues() {
      ConcludedStrategyFactory.StrategyType[] types =
          ConcludedStrategyFactory.StrategyType.values();

      assertEquals(3, types.length);
      assertEquals(
          ConcludedStrategyFactory.StrategyType.ASSIGNED_ONLY,
          ConcludedStrategyFactory.StrategyType.valueOf("ASSIGNED_ONLY"));
      assertEquals(
          ConcludedStrategyFactory.StrategyType.UNASSIGNED_ONLY,
          ConcludedStrategyFactory.StrategyType.valueOf("UNASSIGNED_ONLY"));
      assertEquals(
          ConcludedStrategyFactory.StrategyType.DEFAULT,
          ConcludedStrategyFactory.StrategyType.valueOf("DEFAULT"));
    }
  }
}
