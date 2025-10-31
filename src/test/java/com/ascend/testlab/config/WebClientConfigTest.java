package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for WebClientConfig.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("WebClientConfig Tests")
public class WebClientConfigTest {

  private WebClientConfig config;

  @BeforeEach
  void setUp() {
    config = new WebClientConfig();
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create config with no-arg constructor")
    void testNoArgConstructor() {
      // Act
      WebClientConfig newConfig = new WebClientConfig();

      // Assert
      assertNotNull(newConfig);
    }

    @Test
    @DisplayName("Should have default values")
    void testDefaultValues() {
      // Act
      WebClientConfig newConfig = new WebClientConfig();

      // Assert
      assertEquals(1000, newConfig.getConnectTimeout());
      assertEquals(32, newConfig.getMaxPoolSize());
      assertFalse(newConfig.isLogActivity());
      assertTrue(newConfig.isKeepAlive());
      assertEquals(10, newConfig.getKeepAliveTimeout());
      assertEquals(100, newConfig.getMaxWaitQueueSize());
      assertFalse(newConfig.isPipelining());
      assertEquals(8, newConfig.getPipeliningLimit());
    }
  }

  @Nested
  @DisplayName("Configuration Scenarios")
  class ConfigurationScenarios {

    @Test
    @DisplayName("Should configure all properties correctly")
    void testFullConfiguration() {
      // Arrange & Act
      config.setConnectTimeout(3000);
      config.setMaxPoolSize(128);
      config.setLogActivity(true);
      config.setKeepAlive(true);
      config.setKeepAliveTimeout(30);
      config.setMaxWaitQueueSize(300);
      config.setPipelining(true);
      config.setPipeliningLimit(24);

      // Assert
      assertEquals(3000, config.getConnectTimeout());
      assertEquals(128, config.getMaxPoolSize());
      assertTrue(config.isLogActivity());
      assertTrue(config.isKeepAlive());
      assertEquals(30, config.getKeepAliveTimeout());
      assertEquals(300, config.getMaxWaitQueueSize());
      assertTrue(config.isPipelining());
      assertEquals(24, config.getPipeliningLimit());
    }

    @Test
    @DisplayName("Should handle minimum values")
    void testMinimumValues() {
      // Arrange & Act
      config.setConnectTimeout(1);
      config.setMaxPoolSize(1);
      config.setKeepAliveTimeout(1);
      config.setMaxWaitQueueSize(1);
      config.setPipeliningLimit(1);

      // Assert
      assertEquals(1, config.getConnectTimeout());
      assertEquals(1, config.getMaxPoolSize());
      assertEquals(1, config.getKeepAliveTimeout());
      assertEquals(1, config.getMaxWaitQueueSize());
      assertEquals(1, config.getPipeliningLimit());
    }

    @Test
    @DisplayName("Should handle maximum values")
    void testMaximumValues() {
      // Arrange & Act
      config.setConnectTimeout(Integer.MAX_VALUE);
      config.setMaxPoolSize(Integer.MAX_VALUE);
      config.setKeepAliveTimeout(Integer.MAX_VALUE);
      config.setMaxWaitQueueSize(Integer.MAX_VALUE);
      config.setPipeliningLimit(Integer.MAX_VALUE);

      // Assert
      assertEquals(Integer.MAX_VALUE, config.getConnectTimeout());
      assertEquals(Integer.MAX_VALUE, config.getMaxPoolSize());
      assertEquals(Integer.MAX_VALUE, config.getKeepAliveTimeout());
      assertEquals(Integer.MAX_VALUE, config.getMaxWaitQueueSize());
      assertEquals(Integer.MAX_VALUE, config.getPipeliningLimit());
    }

    @Test
    @DisplayName("Should configure for high throughput")
    void testHighThroughputConfiguration() {
      // Arrange & Act
      config.setConnectTimeout(500);
      config.setMaxPoolSize(256);
      config.setLogActivity(false);
      config.setKeepAlive(true);
      config.setKeepAliveTimeout(60);
      config.setMaxWaitQueueSize(500);
      config.setPipelining(true);
      config.setPipeliningLimit(32);

      // Assert
      assertEquals(500, config.getConnectTimeout());
      assertEquals(256, config.getMaxPoolSize());
      assertFalse(config.isLogActivity());
      assertTrue(config.isKeepAlive());
      assertEquals(60, config.getKeepAliveTimeout());
      assertEquals(500, config.getMaxWaitQueueSize());
      assertTrue(config.isPipelining());
      assertEquals(32, config.getPipeliningLimit());
    }

    @Test
    @DisplayName("Should configure for debugging")
    void testDebugConfiguration() {
      // Arrange & Act
      config.setConnectTimeout(10000);
      config.setMaxPoolSize(5);
      config.setLogActivity(true);
      config.setKeepAlive(true);
      config.setKeepAliveTimeout(5);
      config.setMaxWaitQueueSize(10);
      config.setPipelining(false);

      // Assert
      assertEquals(10000, config.getConnectTimeout());
      assertEquals(5, config.getMaxPoolSize());
      assertTrue(config.isLogActivity());
      assertTrue(config.isKeepAlive());
      assertEquals(5, config.getKeepAliveTimeout());
      assertEquals(10, config.getMaxWaitQueueSize());
      assertFalse(config.isPipelining());
    }

    @Test
    @DisplayName("Should configure pipelining with keep alive")
    void testPipeliningWithKeepAlive() {
      // Arrange & Act
      config.setPipelining(true);
      config.setKeepAlive(true);
      config.setPipeliningLimit(16);

      // Assert
      assertTrue(config.isPipelining());
      assertTrue(config.isKeepAlive());
      assertEquals(16, config.getPipeliningLimit());
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create ConfigProvider instance")
    void testProviderCreation() {
      // Act
      ConfigProvider<WebClientConfig> provider = WebClientConfig.provider();

      // Assert
      assertNotNull(provider);
      assertEquals("webclient", provider.getConfigDirectory());
      assertEquals(WebClientConfig.class, provider.getClazz());
    }

    @Test
    @DisplayName("Should return new provider instance each time")
    void testProviderNewInstance() {
      // Act
      ConfigProvider<WebClientConfig> provider1 = WebClientConfig.provider();
      ConfigProvider<WebClientConfig> provider2 = WebClientConfig.provider();

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
      WebClientConfig config1 = new WebClientConfig();
      config1.setConnectTimeout(5000);
      config1.setMaxPoolSize(64);

      WebClientConfig config2 = new WebClientConfig();
      config2.setConnectTimeout(5000);
      config2.setMaxPoolSize(64);

      // Assert
      assertEquals(config1, config2);
    }

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      WebClientConfig config1 = new WebClientConfig();
      config1.setConnectTimeout(5000);
      config1.setMaxPoolSize(64);

      WebClientConfig config2 = new WebClientConfig();
      config2.setConnectTimeout(5000);
      config2.setMaxPoolSize(64);

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void testNotEquals() {
      // Arrange
      WebClientConfig config1 = new WebClientConfig();
      config1.setConnectTimeout(5000);

      WebClientConfig config2 = new WebClientConfig();
      config2.setConnectTimeout(3000);

      // Assert
      assertNotEquals(config1, config2);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle zero timeout values")
    void testZeroTimeouts() {
      // Arrange & Act
      config.setConnectTimeout(0);
      config.setKeepAliveTimeout(0);

      // Assert
      assertEquals(0, config.getConnectTimeout());
      assertEquals(0, config.getKeepAliveTimeout());
    }

    @Test
    @DisplayName("Should handle negative values")
    void testNegativeValues() {
      // Arrange & Act
      config.setConnectTimeout(-1);
      config.setMaxPoolSize(-1);
      config.setKeepAliveTimeout(-1);

      // Assert
      assertEquals(-1, config.getConnectTimeout());
      assertEquals(-1, config.getMaxPoolSize());
      assertEquals(-1, config.getKeepAliveTimeout());
    }

    @Test
    @DisplayName("Should handle zero pool sizes")
    void testZeroPoolSize() {
      // Arrange & Act
      config.setMaxPoolSize(0);
      config.setMaxWaitQueueSize(0);

      // Assert
      assertEquals(0, config.getMaxPoolSize());
      assertEquals(0, config.getMaxWaitQueueSize());
    }

    @Test
    @DisplayName("Should toggle boolean flags independently")
    void testBooleanFlags() {
      // Arrange & Act
      config.setLogActivity(true);
      config.setKeepAlive(false);
      config.setPipelining(true);

      // Assert
      assertTrue(config.isLogActivity());
      assertFalse(config.isKeepAlive());
      assertTrue(config.isPipelining());
    }

    @Test
    @DisplayName("Should handle pipelining without keep alive")
    void testPipeliningWithoutKeepAlive() {
      // Arrange & Act
      config.setPipelining(true);
      config.setKeepAlive(false);

      // Assert
      assertTrue(config.isPipelining());
      assertFalse(config.isKeepAlive());
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
      // Arrange
      config.setConnectTimeout(5000);
      config.setMaxPoolSize(64);

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("WebClientConfig"));
    }
  }
}
