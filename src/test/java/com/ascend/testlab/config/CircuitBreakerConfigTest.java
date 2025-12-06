package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for CircuitBreakerConfig.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CircuitBreakerConfig Tests")
public class CircuitBreakerConfigTest {

  private CircuitBreakerConfig config;

  @BeforeEach
  void setUp() {
    config = new CircuitBreakerConfig();
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create config with no-arg constructor")
    void testNoArgConstructor() {
      // Act
      CircuitBreakerConfig newConfig = new CircuitBreakerConfig();

      // Assert
      assertNotNull(newConfig);
    }
  }

  @Nested
  @DisplayName("Configuration Scenarios")
  class ConfigurationScenarios {

    @Test
    @DisplayName("Should configure all properties correctly")
    void testFullConfiguration() {
      // Arrange & Act
      config.setFailureRateThreshold(60);
      config.setSlowCallRateThreshold(90);
      config.setWaitDurationInOpenState(120000);
      config.setSlowCallDurationThreshold(10000);
      config.setPermittedNumberOfCallsInHalfOpenState(15);
      config.setMinimumNumberOfCalls(20);
      config.setSlidingWindowSize(200);

      // Assert
      assertEquals(60, config.getFailureRateThreshold());
      assertEquals(90, config.getSlowCallRateThreshold());
      assertEquals(120000, config.getWaitDurationInOpenState());
      assertEquals(10000, config.getSlowCallDurationThreshold());
      assertEquals(15, config.getPermittedNumberOfCallsInHalfOpenState());
      assertEquals(20, config.getMinimumNumberOfCalls());
      assertEquals(200, config.getSlidingWindowSize());
    }

    @Test
    @DisplayName("Should handle minimum threshold values")
    void testMinimumThresholds() {
      // Arrange & Act
      config.setFailureRateThreshold(1);
      config.setSlowCallRateThreshold(1);
      config.setWaitDurationInOpenState(1000);
      config.setSlowCallDurationThreshold(100);
      config.setPermittedNumberOfCallsInHalfOpenState(1);
      config.setMinimumNumberOfCalls(1);
      config.setSlidingWindowSize(10);

      // Assert
      assertEquals(1, config.getFailureRateThreshold());
      assertEquals(1, config.getSlowCallRateThreshold());
      assertEquals(1000, config.getWaitDurationInOpenState());
      assertEquals(100, config.getSlowCallDurationThreshold());
      assertEquals(1, config.getPermittedNumberOfCallsInHalfOpenState());
      assertEquals(1, config.getMinimumNumberOfCalls());
      assertEquals(10, config.getSlidingWindowSize());
    }

    @Test
    @DisplayName("Should handle maximum threshold values")
    void testMaximumThresholds() {
      // Arrange & Act
      config.setFailureRateThreshold(100);
      config.setSlowCallRateThreshold(100);
      config.setWaitDurationInOpenState(Integer.MAX_VALUE);
      config.setSlowCallDurationThreshold(Integer.MAX_VALUE);
      config.setPermittedNumberOfCallsInHalfOpenState(Integer.MAX_VALUE);
      config.setMinimumNumberOfCalls(Integer.MAX_VALUE);
      config.setSlidingWindowSize(Integer.MAX_VALUE);

      // Assert
      assertEquals(100, config.getFailureRateThreshold());
      assertEquals(100, config.getSlowCallRateThreshold());
      assertEquals(Integer.MAX_VALUE, config.getWaitDurationInOpenState());
      assertEquals(Integer.MAX_VALUE, config.getSlowCallDurationThreshold());
      assertEquals(Integer.MAX_VALUE, config.getPermittedNumberOfCallsInHalfOpenState());
      assertEquals(Integer.MAX_VALUE, config.getMinimumNumberOfCalls());
      assertEquals(Integer.MAX_VALUE, config.getSlidingWindowSize());
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create ConfigProvider instance")
    void testProviderCreation() {
      // Act
      ConfigProvider<CircuitBreakerConfig> provider = CircuitBreakerConfig.provider();

      // Assert
      assertNotNull(provider);
      assertEquals("circuit-breaker", provider.getConfigDirectory());
      assertEquals(CircuitBreakerConfig.class, provider.getClazz());
    }

    @Test
    @DisplayName("Should return new provider instance each time")
    void testProviderNewInstance() {
      // Act
      ConfigProvider<CircuitBreakerConfig> provider1 = CircuitBreakerConfig.provider();
      ConfigProvider<CircuitBreakerConfig> provider2 = CircuitBreakerConfig.provider();

      // Assert
      assertNotNull(provider1);
      assertNotNull(provider2);
      assertNotSame(provider1, provider2);
    }
  }

  @Nested
  @DisplayName("Equals and HashCode Tests")
  class EqualsHashCodeTests {

    @Test
    @DisplayName("Should be equal when all fields are same")
    void testEquals() {
      // Arrange
      CircuitBreakerConfig config1 = new CircuitBreakerConfig();
      config1.setFailureRateThreshold(50);
      config1.setSlidingWindowSize(100);

      CircuitBreakerConfig config2 = new CircuitBreakerConfig();
      config2.setFailureRateThreshold(50);
      config2.setSlidingWindowSize(100);

      // Assert
      assertEquals(config1, config2);
    }

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      CircuitBreakerConfig config1 = new CircuitBreakerConfig();
      config1.setFailureRateThreshold(50);

      CircuitBreakerConfig config2 = new CircuitBreakerConfig();
      config2.setFailureRateThreshold(50);

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void testNotEquals() {
      // Arrange
      CircuitBreakerConfig config1 = new CircuitBreakerConfig();
      config1.setFailureRateThreshold(50);

      CircuitBreakerConfig config2 = new CircuitBreakerConfig();
      config2.setFailureRateThreshold(60);

      // Assert
      assertNotEquals(config1, config2);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle zero values")
    void testZeroValues() {
      // Arrange & Act
      config.setFailureRateThreshold(0);
      config.setSlowCallRateThreshold(0);
      config.setWaitDurationInOpenState(0);
      config.setSlowCallDurationThreshold(0);
      config.setPermittedNumberOfCallsInHalfOpenState(0);
      config.setMinimumNumberOfCalls(0);
      config.setSlidingWindowSize(0);

      // Assert
      assertEquals(0, config.getFailureRateThreshold());
      assertEquals(0, config.getSlowCallRateThreshold());
      assertEquals(0, config.getWaitDurationInOpenState());
      assertEquals(0, config.getSlowCallDurationThreshold());
      assertEquals(0, config.getPermittedNumberOfCallsInHalfOpenState());
      assertEquals(0, config.getMinimumNumberOfCalls());
      assertEquals(0, config.getSlidingWindowSize());
    }

    @Test
    @DisplayName("Should handle negative values")
    void testNegativeValues() {
      // Arrange & Act
      config.setFailureRateThreshold(-1);
      config.setSlowCallRateThreshold(-1);
      config.setWaitDurationInOpenState(-1000);
      config.setSlowCallDurationThreshold(-100);

      // Assert
      assertEquals(-1, config.getFailureRateThreshold());
      assertEquals(-1, config.getSlowCallRateThreshold());
      assertEquals(-1000, config.getWaitDurationInOpenState());
      assertEquals(-100, config.getSlowCallDurationThreshold());
    }

    @Test
    @DisplayName("Should handle typical production values")
    void testProductionValues() {
      // Arrange & Act
      config.setFailureRateThreshold(50);
      config.setSlowCallRateThreshold(80);
      config.setWaitDurationInOpenState(60000);
      config.setSlowCallDurationThreshold(5000);
      config.setPermittedNumberOfCallsInHalfOpenState(10);
      config.setMinimumNumberOfCalls(10);
      config.setSlidingWindowSize(100);

      // Assert
      assertEquals(50, config.getFailureRateThreshold());
      assertEquals(80, config.getSlowCallRateThreshold());
      assertEquals(60000, config.getWaitDurationInOpenState());
      assertEquals(5000, config.getSlowCallDurationThreshold());
      assertEquals(10, config.getPermittedNumberOfCallsInHalfOpenState());
      assertEquals(10, config.getMinimumNumberOfCalls());
      assertEquals(100, config.getSlidingWindowSize());
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
      // Arrange
      config.setFailureRateThreshold(50);
      config.setSlidingWindowSize(100);

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("CircuitBreakerConfig"));
    }
  }
}
