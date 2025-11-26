package com.ascend.testlab.allocation.strategy.variantassignment;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VariantAssignmentStrategyFactory Tests")
class VariantAssignmentStrategyFactoryTest {

  @Test
  @DisplayName("Should return RoundRobinVariantAssignment for ROUND_ROBIN strategy")
  void testGetStrategyForRoundRobin() {
    // Act
    VariantAssignmentStrategy strategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.ROUND_ROBIN);

    // Assert
    assertNotNull(strategy);
    assertInstanceOf(RoundRobinVariantAssignment.class, strategy);
  }

  @Test
  @DisplayName("Should return RandomVariantAssignment for RANDOM strategy")
  void testGetStrategyForRandom() {
    // Act
    VariantAssignmentStrategy strategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.RANDOM);

    // Assert
    assertNotNull(strategy);
    assertInstanceOf(RandomVariantAssignment.class, strategy);
  }

  @Test
  @DisplayName("Should return RandomVariantAssignment when distribution strategy is null")
  void testGetStrategyWithNull() {
    // Act
    VariantAssignmentStrategy strategy = VariantAssignmentStrategyFactory.getStrategy(null);

    // Assert
    assertNotNull(strategy);
    assertInstanceOf(RandomVariantAssignment.class, strategy);
  }

  @Test
  @DisplayName("Should return same instance for multiple calls (singleton pattern)")
  void testSingletonBehavior() {
    // Act
    VariantAssignmentStrategy strategy1 =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.ROUND_ROBIN);
    VariantAssignmentStrategy strategy2 =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.ROUND_ROBIN);

    VariantAssignmentStrategy strategy3 =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.RANDOM);
    VariantAssignmentStrategy strategy4 =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.RANDOM);

    // Assert - Same instance should be returned for same strategy type
    assertSame(strategy1, strategy2, "Should return same RoundRobinVariantAssignment instance");
    assertSame(strategy3, strategy4, "Should return same RandomVariantAssignment instance");
  }

  @Test
  @DisplayName("Should return different instances for different strategies")
  void testDifferentStrategiesReturnDifferentInstances() {
    // Act
    VariantAssignmentStrategy roundRobinStrategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.ROUND_ROBIN);
    VariantAssignmentStrategy randomStrategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.RANDOM);

    // Assert
    assertNotSame(roundRobinStrategy, randomStrategy);
    assertNotEquals(roundRobinStrategy.getClass(), randomStrategy.getClass());
  }

  @Test
  @DisplayName("Should handle all enum values")
  void testHandleAllEnumValues() {
    // Act & Assert - Should not throw exceptions for any enum value
    for (DistributionStrategy strategy : DistributionStrategy.values()) {
      VariantAssignmentStrategy result = VariantAssignmentStrategyFactory.getStrategy(strategy);
      assertNotNull(result, "Strategy should not be null for: " + strategy);
    }
  }

  @Test
  @DisplayName("Should return functional strategy instances")
  void testReturnedStrategiesAreFunctional() {
    // Act
    VariantAssignmentStrategy roundRobinStrategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.ROUND_ROBIN);
    VariantAssignmentStrategy randomStrategy =
        VariantAssignmentStrategyFactory.getStrategy(DistributionStrategy.RANDOM);

    // Assert - Strategies should be usable (not null and correct type)
    assertNotNull(roundRobinStrategy);
    assertNotNull(randomStrategy);
    assertTrue(roundRobinStrategy instanceof VariantAssignmentStrategy);
    assertTrue(randomStrategy instanceof VariantAssignmentStrategy);
  }
}
