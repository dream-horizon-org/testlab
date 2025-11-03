package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for AerospikeConfig.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("AerospikeConfig Tests")
public class AerospikeConfigTest {

  private AerospikeConfig config;

  @BeforeEach
  void setUp() {
    config = new AerospikeConfig();
  }

  @Nested
  @DisplayName("Constructor and Basic Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create config with no-arg constructor")
    void testNoArgConstructor() {
      // Act
      AerospikeConfig newConfig = new AerospikeConfig();

      // Assert
      assertNotNull(newConfig);
    }

    @Test
    @DisplayName("Should have default port value")
    void testDefaultPort() {
      // Assert
      assertEquals(3000, config.getPort());
    }
  }

  @Nested
  @DisplayName("Configuration Scenarios")
  class ConfigurationScenarios {

    @Test
    @DisplayName("Should configure all properties correctly")
    void testFullConfiguration() {
      // Arrange & Act
      config.setHost("aerospike.example.com");
      config.setPort(3002);
      config.setMaxRetries(10);
      config.setMaxConnsPerNode(500);
      config.setEventLoopSize(16);
      config.setMaxCommandsInProcess(150);
      config.setMaxCommandsInQueue(300);
      config.setConnectRetryIntervalMS(10000L);
      config.setNamespace("production");

      // Assert
      assertEquals("aerospike.example.com", config.getHost());
      assertEquals(3002, config.getPort());
      assertEquals(10, config.getMaxRetries());
      assertEquals(500, config.getMaxConnsPerNode());
      assertEquals(16, config.getEventLoopSize());
      assertEquals(150, config.getMaxCommandsInProcess());
      assertEquals(300, config.getMaxCommandsInQueue());
      assertEquals(10000L, config.getConnectRetryIntervalMS());
      assertEquals("production", config.getNamespace());
    }

    @Test
    @DisplayName("Should handle minimum values")
    void testMinimumValues() {
      // Arrange & Act
      config.setPort(1);
      config.setMaxRetries(0);
      config.setMaxConnsPerNode(1);
      config.setEventLoopSize(1);
      config.setMaxCommandsInProcess(1);
      config.setMaxCommandsInQueue(1);
      config.setConnectRetryIntervalMS(0L);

      // Assert
      assertEquals(1, config.getPort());
      assertEquals(0, config.getMaxRetries());
      assertEquals(1, config.getMaxConnsPerNode());
      assertEquals(1, config.getEventLoopSize());
      assertEquals(1, config.getMaxCommandsInProcess());
      assertEquals(1, config.getMaxCommandsInQueue());
      assertEquals(0L, config.getConnectRetryIntervalMS());
    }

    @Test
    @DisplayName("Should handle maximum values")
    void testMaximumValues() {
      // Arrange & Act
      config.setPort(65535);
      config.setMaxRetries(Integer.MAX_VALUE);
      config.setMaxConnsPerNode(Integer.MAX_VALUE);
      config.setEventLoopSize(Integer.MAX_VALUE);
      config.setMaxCommandsInProcess(Integer.MAX_VALUE);
      config.setMaxCommandsInQueue(Integer.MAX_VALUE);
      config.setConnectRetryIntervalMS(Long.MAX_VALUE);

      // Assert
      assertEquals(65535, config.getPort());
      assertEquals(Integer.MAX_VALUE, config.getMaxRetries());
      assertEquals(Integer.MAX_VALUE, config.getMaxConnsPerNode());
      assertEquals(Integer.MAX_VALUE, config.getEventLoopSize());
      assertEquals(Integer.MAX_VALUE, config.getMaxCommandsInProcess());
      assertEquals(Integer.MAX_VALUE, config.getMaxCommandsInQueue());
      assertEquals(Long.MAX_VALUE, config.getConnectRetryIntervalMS());
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create ConfigProvider instance")
    void testProviderCreation() {
      // Act
      ConfigProvider<AerospikeConfig> provider = AerospikeConfig.provider();

      // Assert
      assertNotNull(provider);
      assertEquals("aerospike", provider.getConfigDirectory());
      assertEquals(AerospikeConfig.class, provider.getClazz());
    }

    @Test
    @DisplayName("Should return new provider instance each time")
    void testProviderNewInstance() {
      // Act
      ConfigProvider<AerospikeConfig> provider1 = AerospikeConfig.provider();
      ConfigProvider<AerospikeConfig> provider2 = AerospikeConfig.provider();

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
      AerospikeConfig config1 = new AerospikeConfig();
      config1.setHost("localhost");
      config1.setPort(3000);
      config1.setNamespace("test");

      AerospikeConfig config2 = new AerospikeConfig();
      config2.setHost("localhost");
      config2.setPort(3000);
      config2.setNamespace("test");

      // Assert
      assertEquals(config1, config2);
    }

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      AerospikeConfig config1 = new AerospikeConfig();
      config1.setHost("localhost");
      config1.setPort(3000);

      AerospikeConfig config2 = new AerospikeConfig();
      config2.setHost("localhost");
      config2.setPort(3000);

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void testNotEquals() {
      // Arrange
      AerospikeConfig config1 = new AerospikeConfig();
      config1.setHost("localhost");

      AerospikeConfig config2 = new AerospikeConfig();
      config2.setHost("remote");

      // Assert
      assertNotEquals(config1, config2);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null host")
    void testNullHost() {
      // Arrange & Act
      config.setHost(null);

      // Assert
      assertNull(config.getHost());
    }

    @Test
    @DisplayName("Should handle empty host")
    void testEmptyHost() {
      // Arrange & Act
      config.setHost("");

      // Assert
      assertEquals("", config.getHost());
    }

    @Test
    @DisplayName("Should handle null namespace")
    void testNullNamespace() {
      // Arrange & Act
      config.setNamespace(null);

      // Assert
      assertNull(config.getNamespace());
    }

    @Test
    @DisplayName("Should handle empty namespace")
    void testEmptyNamespace() {
      // Arrange & Act
      config.setNamespace("");

      // Assert
      assertEquals("", config.getNamespace());
    }

    @Test
    @DisplayName("Should handle negative maxRetries")
    void testNegativeMaxRetries() {
      // Arrange & Act
      config.setMaxRetries(-1);

      // Assert
      assertEquals(-1, config.getMaxRetries());
    }

    @Test
    @DisplayName("Should handle zero values for connection settings")
    void testZeroConnectionSettings() {
      // Arrange & Act
      config.setMaxConnsPerNode(0);
      config.setEventLoopSize(0);
      config.setMaxCommandsInProcess(0);
      config.setMaxCommandsInQueue(0);

      // Assert
      assertEquals(0, config.getMaxConnsPerNode());
      assertEquals(0, config.getEventLoopSize());
      assertEquals(0, config.getMaxCommandsInProcess());
      assertEquals(0, config.getMaxCommandsInQueue());
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
      // Arrange
      config.setHost("localhost");
      config.setPort(3000);
      config.setNamespace("test");

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("AerospikeConfig"));
    }
  }
}
