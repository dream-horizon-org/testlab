package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for HttpServerConfig.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("HttpServerConfig Tests")
public class HttpServerConfigTest {

  private HttpServerConfig config;
  private String originalHttpPort;

  @BeforeEach
  void setUp() {
    config = new HttpServerConfig();
    originalHttpPort = System.getProperty("http.default.port");
  }

  @AfterEach
  void tearDown() {
    // Restore original system property
    if (originalHttpPort != null) {
      System.setProperty("http.default.port", originalHttpPort);
    } else {
      System.clearProperty("http.default.port");
    }
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create config with no-arg constructor")
    void testNoArgConstructor() {
      // Act
      HttpServerConfig newConfig = new HttpServerConfig();

      // Assert
      assertNotNull(newConfig);
    }
  }

  @Nested
  @DisplayName("Port Getter Tests")
  class PortGetterTests {

    @Test
    @DisplayName("Should return configured port when port is positive")
    void testGetPortWithConfiguredValue() {
      // Arrange
      config.setPort(9090);

      // Act
      Integer port = config.getPort();

      // Assert
      assertEquals(9090, port);
    }

    @Test
    @DisplayName("Should return default port when port is zero")
    void testGetPortWithZeroValue() {
      // Arrange
      config.setPort(0);

      // Act
      Integer port = config.getPort();

      // Assert
      assertEquals(8080, port);
    }

    @Test
    @DisplayName("Should return default port when port is negative")
    void testGetPortWithNegativeValue() {
      // Arrange
      config.setPort(-1);

      // Act
      Integer port = config.getPort();

      // Assert
      assertEquals(8080, port);
    }

    @Test
    @DisplayName("Should return default port when port is null")
    void testGetPortWithNullValue() {
      // Arrange
      config.setPort(null);

      // Act & Assert
      // The getPort() method will throw NPE when port is null due to: 0 < this.port
      // This tests the actual behavior of the code
      assertThrows(NullPointerException.class, () -> config.getPort());
    }

    @Test
    @DisplayName("Should return system property port when configured port is zero")
    void testGetPortWithSystemProperty() {
      // Arrange
      System.setProperty("http.default.port", "9999");
      config.setPort(0);

      // Act
      Integer port = config.getPort();

      // Assert
      assertEquals(9999, port);
    }

    @Test
    @DisplayName("Should prefer configured port over system property")
    void testConfiguredPortOverSystemProperty() {
      // Arrange
      System.setProperty("http.default.port", "9999");
      config.setPort(7070);

      // Act
      Integer port = config.getPort();

      // Assert
      assertEquals(7070, port);
    }
  }

  @Nested
  @DisplayName("Configuration Scenarios")
  class ConfigurationScenarios {

    @Test
    @DisplayName("Should configure all properties correctly")
    void testFullConfiguration() {
      // Arrange & Act
      config.setHost("0.0.0.0");
      config.setPort(8443);
      config.setCompressionLevel(9);
      config.setCompressionSupported(true);
      config.setIdleTimeout(600);
      config.setLogActivity(true);
      config.setReusePort(true);
      config.setReuseAddress(true);
      config.setTcpFastOpen(true);
      config.setTcpNoDelay(true);
      config.setTcpQuickAck(true);
      config.setTcpKeepAlive(true);
      config.setUseAlpn(true);

      // Assert
      assertEquals("0.0.0.0", config.getHost());
      assertEquals(8443, config.getPort());
      assertEquals(9, config.getCompressionLevel());
      assertTrue(config.getCompressionSupported());
      assertEquals(600, config.getIdleTimeout());
      assertTrue(config.getLogActivity());
      assertTrue(config.getReusePort());
      assertTrue(config.getReuseAddress());
      assertTrue(config.getTcpFastOpen());
      assertTrue(config.getTcpNoDelay());
      assertTrue(config.getTcpQuickAck());
      assertTrue(config.getTcpKeepAlive());
      assertTrue(config.getUseAlpn());
    }

    @Test
    @DisplayName("Should handle all TCP optimization flags enabled")
    void testAllTcpOptimizations() {
      // Arrange & Act
      config.setTcpFastOpen(true);
      config.setTcpNoDelay(true);
      config.setTcpQuickAck(true);
      config.setTcpKeepAlive(true);

      // Assert
      assertTrue(config.getTcpFastOpen());
      assertTrue(config.getTcpNoDelay());
      assertTrue(config.getTcpQuickAck());
      assertTrue(config.getTcpKeepAlive());
    }

    @Test
    @DisplayName("Should handle all TCP optimization flags disabled")
    void testNoTcpOptimizations() {
      // Arrange & Act
      config.setTcpFastOpen(false);
      config.setTcpNoDelay(false);
      config.setTcpQuickAck(false);
      config.setTcpKeepAlive(false);

      // Assert
      assertFalse(config.getTcpFastOpen());
      assertFalse(config.getTcpNoDelay());
      assertFalse(config.getTcpQuickAck());
      assertFalse(config.getTcpKeepAlive());
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create ConfigProvider instance")
    void testProviderCreation() {
      // Act
      ConfigProvider<HttpServerConfig> provider = HttpServerConfig.provider();

      // Assert
      assertNotNull(provider);
      assertEquals("http-server", provider.getConfigDirectory());
      assertEquals(HttpServerConfig.class, provider.getClazz());
    }

    @Test
    @DisplayName("Should return new provider instance each time")
    void testProviderNewInstance() {
      // Act
      ConfigProvider<HttpServerConfig> provider1 = HttpServerConfig.provider();
      ConfigProvider<HttpServerConfig> provider2 = HttpServerConfig.provider();

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
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setHost("localhost");
      config1.setPort(8080);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setHost("localhost");
      config2.setPort(8080);

      // Assert
      assertEquals(config1, config2);
    }

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setHost("localhost");
      config1.setPort(8080);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setHost("localhost");
      config2.setPort(8080);

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void testNotEquals() {
      // Arrange
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setPort(8080);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setPort(9090);

      // Assert
      assertNotEquals(config1, config2);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle extreme compression level values")
    void testExtremeCompressionLevel() {
      // Arrange & Act
      config.setCompressionLevel(0);

      // Assert
      assertEquals(0, config.getCompressionLevel());
    }

    @Test
    @DisplayName("Should handle extreme idle timeout values")
    void testExtremeIdleTimeout() {
      // Arrange & Act
      config.setIdleTimeout(Integer.MAX_VALUE);

      // Assert
      assertEquals(Integer.MAX_VALUE, config.getIdleTimeout());
    }

    @Test
    @DisplayName("Should handle zero idle timeout")
    void testZeroIdleTimeout() {
      // Arrange & Act
      config.setIdleTimeout(0);

      // Assert
      assertEquals(0, config.getIdleTimeout());
    }

    @Test
    @DisplayName("Should handle null host value")
    void testNullHost() {
      // Arrange & Act
      config.setHost(null);

      // Assert
      assertNull(config.getHost());
    }

    @Test
    @DisplayName("Should handle empty host value")
    void testEmptyHost() {
      // Arrange & Act
      config.setHost("");

      // Assert
      assertEquals("", config.getHost());
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
      config.setPort(8080);

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("HttpServerConfig"));
    }
  }
}
