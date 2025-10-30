package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PostgreSQLConfig}.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("PostgreSQLConfig Tests")
class PostgreSQLConfigTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create PostgreSQLConfig with no-args constructor")
    void testNoArgsConstructor() {
      // Act
      PostgreSQLConfig config = new PostgreSQLConfig();

      // Assert
      assertNotNull(config);
    }

    @Test
    @DisplayName("Should create BaseConfig with no-args constructor")
    void testBaseConfigNoArgsConstructor() {
      // Act
      PostgreSQLConfig.BaseConfig baseConfig = new PostgreSQLConfig.BaseConfig();

      // Assert
      assertNotNull(baseConfig);
    }

    @Test
    @DisplayName("Should create ConnectOptions with no-args constructor")
    void testConnectOptionsNoArgsConstructor() {
      // Act
      PostgreSQLConfig.ConnectOptions connectOptions = new PostgreSQLConfig.ConnectOptions();

      // Assert
      assertNotNull(connectOptions);
    }

    @Test
    @DisplayName("Should create PoolOptions with no-args constructor")
    void testPoolOptionsNoArgsConstructor() {
      // Act
      PostgreSQLConfig.PoolOptions poolOptions = new PostgreSQLConfig.PoolOptions();

      // Assert
      assertNotNull(poolOptions);
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create provider with correct config name")
    void testProvider() {
      // Act
      ConfigProvider<PostgreSQLConfig> provider = PostgreSQLConfig.provider();

      // Assert
      assertNotNull(provider);
    }

    @Test
    @DisplayName("Should create provider that can load config")
    void testProviderGet() {
      // Arrange
      System.setProperty("POSTGRES_USER", "test_user");
      System.setProperty("POSTGRES_PASSWORD", "test_password");

      try {
        // Act
        ConfigProvider<PostgreSQLConfig> provider = PostgreSQLConfig.provider();
        PostgreSQLConfig config = provider.get();

        // Assert
        assertNotNull(config);
        assertNotNull(config.getReaderConfig());
        assertNotNull(config.getWriterConfig());
      } finally {
        // Clean up
        System.clearProperty("POSTGRES_USER");
        System.clearProperty("POSTGRES_PASSWORD");
      }
    }

    @Test
    @DisplayName("Should create multiple independent providers")
    void testMultipleProviders() {
      // Act
      ConfigProvider<PostgreSQLConfig> provider1 = PostgreSQLConfig.provider();
      ConfigProvider<PostgreSQLConfig> provider2 = PostgreSQLConfig.provider();

      // Assert
      assertNotNull(provider1);
      assertNotNull(provider2);
      assertNotSame(provider1, provider2);
    }
  }

  @Nested
  @DisplayName("Complex Configuration Tests")
  class ComplexConfigurationTests {

    @Test
    @DisplayName("Should set complete configuration hierarchy")
    void testCompleteConfiguration() {
      // Arrange
      PostgreSQLConfig config = getPostgreSQLConfig();

      // Assert
      assertNotNull(config.getReaderConfig());
      assertNotNull(config.getWriterConfig());

      // Reader assertions
      assertEquals("reader-host", config.getReaderConfig().getConnectOptions().getHost());
      assertEquals(5432, config.getReaderConfig().getConnectOptions().getPort());
      assertEquals("reader_user", config.getReaderConfig().getConnectOptions().getUser());
      assertEquals("reader_pass", config.getReaderConfig().getConnectOptions().getPassword());
      assertEquals("reader_db", config.getReaderConfig().getConnectOptions().getDatabase());
      assertEquals(3000, config.getReaderConfig().getConnectOptions().getConnectTimeout());
      assertTrue(config.getReaderConfig().getConnectOptions().getCachePreparedStatements());
      assertEquals(20, config.getReaderConfig().getPoolOptions().getMaxSize());
      assertEquals(100, config.getReaderConfig().getPoolOptions().getMaxWaitQueueSize());
      assertEquals(3, config.getReaderConfig().getRetryCount());

      // Writer assertions
      assertEquals("writer-host", config.getWriterConfig().getConnectOptions().getHost());
      assertEquals(5433, config.getWriterConfig().getConnectOptions().getPort());
      assertEquals("writer_user", config.getWriterConfig().getConnectOptions().getUser());
      assertEquals("writer_pass", config.getWriterConfig().getConnectOptions().getPassword());
      assertEquals("writer_db", config.getWriterConfig().getConnectOptions().getDatabase());
      assertEquals(2000, config.getWriterConfig().getConnectOptions().getConnectTimeout());
      assertFalse(config.getWriterConfig().getConnectOptions().getCachePreparedStatements());
      assertEquals(30, config.getWriterConfig().getPoolOptions().getMaxSize());
      assertEquals(150, config.getWriterConfig().getPoolOptions().getMaxWaitQueueSize());
      assertEquals(5, config.getWriterConfig().getRetryCount());
    }

    private static PostgreSQLConfig getPostgreSQLConfig() {
      PostgreSQLConfig config = new PostgreSQLConfig();

      PostgreSQLConfig.BaseConfig readerConfig = new PostgreSQLConfig.BaseConfig();
      PostgreSQLConfig.ConnectOptions readerConnectOptions = new PostgreSQLConfig.ConnectOptions();
      readerConnectOptions.setHost("reader-host");
      readerConnectOptions.setPort(5432);
      readerConnectOptions.setUser("reader_user");
      readerConnectOptions.setPassword("reader_pass");
      readerConnectOptions.setDatabase("reader_db");
      readerConnectOptions.setConnectTimeout(3000);
      readerConnectOptions.setCachePreparedStatements(true);

      PostgreSQLConfig.PoolOptions readerPoolOptions = new PostgreSQLConfig.PoolOptions();
      readerPoolOptions.setMaxSize(20);
      readerPoolOptions.setMaxWaitQueueSize(100);

      readerConfig.setConnectOptions(readerConnectOptions);
      readerConfig.setPoolOptions(readerPoolOptions);
      readerConfig.setRetryCount(3);

      PostgreSQLConfig.BaseConfig writerConfig = new PostgreSQLConfig.BaseConfig();
      PostgreSQLConfig.ConnectOptions writerConnectOptions = new PostgreSQLConfig.ConnectOptions();
      writerConnectOptions.setHost("writer-host");
      writerConnectOptions.setPort(5433);
      writerConnectOptions.setUser("writer_user");
      writerConnectOptions.setPassword("writer_pass");
      writerConnectOptions.setDatabase("writer_db");
      writerConnectOptions.setConnectTimeout(2000);
      writerConnectOptions.setCachePreparedStatements(false);

      PostgreSQLConfig.PoolOptions writerPoolOptions = new PostgreSQLConfig.PoolOptions();
      writerPoolOptions.setMaxSize(30);
      writerPoolOptions.setMaxWaitQueueSize(150);

      writerConfig.setConnectOptions(writerConnectOptions);
      writerConfig.setPoolOptions(writerPoolOptions);
      writerConfig.setRetryCount(5);

      config.setReaderConfig(readerConfig);
      config.setWriterConfig(writerConfig);
      return config;
    }

    @Test
    @DisplayName("Should handle separate reader and writer configurations")
    void testSeparateReaderWriter() {
      // Arrange
      PostgreSQLConfig config = new PostgreSQLConfig();
      PostgreSQLConfig.BaseConfig readerConfig = new PostgreSQLConfig.BaseConfig();
      PostgreSQLConfig.BaseConfig writerConfig = new PostgreSQLConfig.BaseConfig();

      // Act
      config.setReaderConfig(readerConfig);
      config.setWriterConfig(writerConfig);

      // Assert
      assertNotSame(config.getReaderConfig(), config.getWriterConfig());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null values in config")
    void testNullValues() {
      // Arrange
      PostgreSQLConfig config = new PostgreSQLConfig();

      // Act
      config.setReaderConfig(null);
      config.setWriterConfig(null);

      // Assert
      assertNull(config.getReaderConfig());
      assertNull(config.getWriterConfig());
    }

    @Test
    @DisplayName("Should handle null values in BaseConfig")
    void testNullValuesInBaseConfig() {
      // Arrange
      PostgreSQLConfig.BaseConfig baseConfig = new PostgreSQLConfig.BaseConfig();

      // Act
      baseConfig.setConnectOptions(null);
      baseConfig.setPoolOptions(null);
      baseConfig.setRetryCount(null);

      // Assert
      assertNull(baseConfig.getConnectOptions());
      assertNull(baseConfig.getPoolOptions());
      assertNull(baseConfig.getRetryCount());
    }

    @Test
    @DisplayName("Should handle extreme pool size values")
    void testExtremePoolSizeValues() {
      // Arrange
      PostgreSQLConfig.PoolOptions poolOptions = new PostgreSQLConfig.PoolOptions();

      // Act
      poolOptions.setMaxSize(Integer.MAX_VALUE);
      poolOptions.setMaxWaitQueueSize(Integer.MAX_VALUE);

      // Assert
      assertEquals(Integer.MAX_VALUE, poolOptions.getMaxSize());
      assertEquals(Integer.MAX_VALUE, poolOptions.getMaxWaitQueueSize());
    }

    @Test
    @DisplayName("Should handle zero retry count")
    void testZeroRetryCount() {
      // Arrange
      PostgreSQLConfig.BaseConfig baseConfig = new PostgreSQLConfig.BaseConfig();

      // Act
      baseConfig.setRetryCount(0);

      // Assert
      assertEquals(0, baseConfig.getRetryCount());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStringValues() {
      // Arrange
      PostgreSQLConfig.ConnectOptions options = new PostgreSQLConfig.ConnectOptions();

      // Act
      options.setHost("");
      options.setUser("");
      options.setPassword("");
      options.setDatabase("");

      // Assert
      assertEquals("", options.getHost());
      assertEquals("", options.getUser());
      assertEquals("", options.getPassword());
      assertEquals("", options.getDatabase());
    }

    @Test
    @DisplayName("Should handle negative values")
    void testNegativeValues() {
      // Arrange
      PostgreSQLConfig.BaseConfig baseConfig = new PostgreSQLConfig.BaseConfig();
      PostgreSQLConfig.ConnectOptions connectOptions = new PostgreSQLConfig.ConnectOptions();
      PostgreSQLConfig.PoolOptions poolOptions = new PostgreSQLConfig.PoolOptions();

      // Act
      baseConfig.setRetryCount(-1);
      connectOptions.setPort(-1);
      connectOptions.setConnectTimeout(-1);
      poolOptions.setMaxSize(-1);
      poolOptions.setMaxWaitQueueSize(-1);

      // Assert
      assertEquals(-1, baseConfig.getRetryCount());
      assertEquals(-1, connectOptions.getPort());
      assertEquals(-1, connectOptions.getConnectTimeout());
      assertEquals(-1, poolOptions.getMaxSize());
      assertEquals(-1, poolOptions.getMaxWaitQueueSize());
    }

    @Test
    @DisplayName("Should handle minimum port value")
    void testMinimumPortValue() {
      // Arrange
      PostgreSQLConfig.ConnectOptions options = new PostgreSQLConfig.ConnectOptions();

      // Act
      options.setPort(1);

      // Assert
      assertEquals(1, options.getPort());
    }

    @Test
    @DisplayName("Should handle maximum port value")
    void testMaximumPortValue() {
      // Arrange
      PostgreSQLConfig.ConnectOptions options = new PostgreSQLConfig.ConnectOptions();

      // Act
      options.setPort(65535);

      // Assert
      assertEquals(65535, options.getPort());
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should generate toString for PostgreSQLConfig")
    void testPostgreSQLConfigToString() {
      // Arrange
      PostgreSQLConfig config = new PostgreSQLConfig();
      config.setReaderConfig(new PostgreSQLConfig.BaseConfig());

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("PostgreSQLConfig"));
    }

    @Test
    @DisplayName("Should generate toString for BaseConfig")
    void testBaseConfigToString() {
      // Arrange
      PostgreSQLConfig.BaseConfig baseConfig = new PostgreSQLConfig.BaseConfig();
      baseConfig.setRetryCount(3);

      // Act
      String result = baseConfig.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("BaseConfig"));
    }

    @Test
    @DisplayName("Should generate toString for ConnectOptions")
    void testConnectOptionsToString() {
      // Arrange
      PostgreSQLConfig.ConnectOptions options = new PostgreSQLConfig.ConnectOptions();
      options.setHost("localhost");
      options.setPort(5432);

      // Act
      String result = options.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("ConnectOptions"));
    }

    @Test
    @DisplayName("Should generate toString for PoolOptions")
    void testPoolOptionsToString() {
      // Arrange
      PostgreSQLConfig.PoolOptions options = new PostgreSQLConfig.PoolOptions();
      options.setMaxSize(20);

      // Act
      String result = options.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("PoolOptions"));
    }
  }

  @Nested
  @DisplayName("Equals and HashCode Tests")
  class EqualsHashCodeTests {

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      PostgreSQLConfig config1 = new PostgreSQLConfig();

      PostgreSQLConfig config2 = new PostgreSQLConfig();

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when fields differ")
    void testNotEquals() {
      // Arrange
      PostgreSQLConfig config1 = new PostgreSQLConfig();
      config1.setReaderConfig(new PostgreSQLConfig.BaseConfig());

      PostgreSQLConfig config2 = new PostgreSQLConfig();

      // Assert
      assertNotEquals(config1, config2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testNotEqualsNull() {
      // Arrange
      PostgreSQLConfig config = new PostgreSQLConfig();

      // Assert
      assertNotEquals(null, config);
    }

    @Test
    @DisplayName("BaseConfig should be equal when all fields same")
    void testBaseConfigEquals() {
      // Arrange
      PostgreSQLConfig.BaseConfig config1 = new PostgreSQLConfig.BaseConfig();
      config1.setRetryCount(3);

      PostgreSQLConfig.BaseConfig config2 = new PostgreSQLConfig.BaseConfig();
      config2.setRetryCount(3);

      // Assert
      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("ConnectOptions should be equal when all fields same")
    void testConnectOptionsEquals() {
      // Arrange
      PostgreSQLConfig.ConnectOptions options1 = new PostgreSQLConfig.ConnectOptions();
      options1.setHost("localhost");
      options1.setPort(5432);

      PostgreSQLConfig.ConnectOptions options2 = new PostgreSQLConfig.ConnectOptions();
      options2.setHost("localhost");
      options2.setPort(5432);

      // Assert
      assertEquals(options1, options2);
      assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    @DisplayName("PoolOptions should be equal when all fields same")
    void testPoolOptionsEquals() {
      // Arrange
      PostgreSQLConfig.PoolOptions options1 = new PostgreSQLConfig.PoolOptions();
      options1.setMaxSize(20);
      options1.setMaxWaitQueueSize(100);

      PostgreSQLConfig.PoolOptions options2 = new PostgreSQLConfig.PoolOptions();
      options2.setMaxSize(20);
      options2.setMaxWaitQueueSize(100);

      // Assert
      assertEquals(options1, options2);
      assertEquals(options1.hashCode(), options2.hashCode());
    }
  }
}
