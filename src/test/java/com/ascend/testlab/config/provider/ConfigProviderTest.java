package com.ascend.testlab.config.provider;

import static org.junit.jupiter.api.Assertions.*;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for ConfigProvider.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ConfigProvider Tests")
public class ConfigProviderTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create provider with valid parameters")
    void testConstructorWithValidParameters() {
      // Arrange
      String configDirectory = "test-config";
      Class<TestConfig> clazz = TestConfig.class;

      // Act
      ConfigProvider<TestConfig> provider = new ConfigProvider<>(configDirectory, clazz);

      // Assert
      assertNotNull(provider);
      assertEquals(configDirectory, provider.getConfigDirectory());
      assertEquals(clazz, provider.getClazz());
    }

    @Test
    @DisplayName("Should create provider with different config types")
    void testConstructorWithDifferentTypes() {
      // Act
      ConfigProvider<String> stringProvider = new ConfigProvider<>("string-config", String.class);
      ConfigProvider<Integer> intProvider = new ConfigProvider<>("int-config", Integer.class);
      ConfigProvider<TestConfig> testProvider =
          new ConfigProvider<>("test-config", TestConfig.class);

      // Assert
      assertNotNull(stringProvider);
      assertNotNull(intProvider);
      assertNotNull(testProvider);
      assertEquals(String.class, stringProvider.getClazz());
      assertEquals(Integer.class, intProvider.getClazz());
      assertEquals(TestConfig.class, testProvider.getClazz());
    }

    @Test
    @DisplayName("Should create provider with empty config directory")
    void testConstructorWithEmptyDirectory() {
      // Arrange
      String configDirectory = "";
      Class<TestConfig> clazz = TestConfig.class;

      // Act
      ConfigProvider<TestConfig> provider = new ConfigProvider<>(configDirectory, clazz);

      // Assert
      assertNotNull(provider);
      assertEquals(configDirectory, provider.getConfigDirectory());
    }
  }

  @Nested
  @DisplayName("Getter Tests")
  class GetterTests {

    @Test
    @DisplayName("Should return config directory")
    void testGetConfigDirectory() {
      // Arrange
      String expectedDirectory = "aerospike";
      ConfigProvider<TestConfig> provider =
          new ConfigProvider<>(expectedDirectory, TestConfig.class);

      // Act
      String actualDirectory = provider.getConfigDirectory();

      // Assert
      assertEquals(expectedDirectory, actualDirectory);
    }

    @Test
    @DisplayName("Should return config class")
    void testGetClazz() {
      // Arrange
      Class<TestConfig> expectedClass = TestConfig.class;
      ConfigProvider<TestConfig> provider = new ConfigProvider<>("test", expectedClass);

      // Act
      Class<TestConfig> actualClass = provider.getClazz();

      // Assert
      assertEquals(expectedClass, actualClass);
    }

    @Test
    @DisplayName("Should return immutable config directory")
    void testConfigDirectoryImmutability() {
      // Arrange
      String originalDirectory = "original";
      ConfigProvider<TestConfig> provider =
          new ConfigProvider<>(originalDirectory, TestConfig.class);

      // Act
      String directory1 = provider.getConfigDirectory();
      String directory2 = provider.getConfigDirectory();

      // Assert
      assertEquals(directory1, directory2);
      assertEquals(originalDirectory, directory1);
    }

    @Test
    @DisplayName("Should return immutable config class")
    void testConfigClassImmutability() {
      // Arrange
      Class<TestConfig> originalClass = TestConfig.class;
      ConfigProvider<TestConfig> provider = new ConfigProvider<>("test", originalClass);

      // Act
      Class<TestConfig> class1 = provider.getClazz();
      Class<TestConfig> class2 = provider.getClazz();

      // Assert
      assertSame(class1, class2);
      assertEquals(originalClass, class1);
    }
  }

  @Nested
  @DisplayName("Config Path Tests")
  class ConfigPathTests {

    @Test
    @DisplayName("Should generate correct config path")
    void testGetConfigPath() {
      // Arrange
      ConfigProvider<TestConfig> provider = new ConfigProvider<>("aerospike", TestConfig.class);

      // Act
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals("config/aerospike/%s.conf", configPath);
    }

    @Test
    @DisplayName("Should generate config path for different directories")
    void testGetConfigPathForDifferentDirectories() {
      // Arrange
      ConfigProvider<TestConfig> aerospikeProvider =
          new ConfigProvider<>("aerospike", TestConfig.class);
      ConfigProvider<TestConfig> mysqlProvider = new ConfigProvider<>("mysql", TestConfig.class);
      ConfigProvider<TestConfig> webClientProvider =
          new ConfigProvider<>("webclient", TestConfig.class);

      // Act
      String aerospikePath = aerospikeProvider.getConfigPath();
      String mysqlPath = mysqlProvider.getConfigPath();
      String webClientPath = webClientProvider.getConfigPath();

      // Assert
      assertEquals("config/aerospike/%s.conf", aerospikePath);
      assertEquals("config/mysql/%s.conf", mysqlPath);
      assertEquals("config/webclient/%s.conf", webClientPath);
    }

    @Test
    @DisplayName("Should generate config path with hyphenated directory")
    void testGetConfigPathWithHyphenatedDirectory() {
      // Arrange
      ConfigProvider<TestConfig> provider =
          new ConfigProvider<>("circuit-breaker", TestConfig.class);

      // Act
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals("config/circuit-breaker/%s.conf", configPath);
    }

    @Test
    @DisplayName("Should generate config path with nested directory")
    void testGetConfigPathWithNestedDirectory() {
      // Arrange
      ConfigProvider<TestConfig> provider =
          new ConfigProvider<>("nested/directory", TestConfig.class);

      // Act
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals("config/nested/directory/%s.conf", configPath);
    }
  }

  @Nested
  @DisplayName("Provider Pattern Tests")
  class ProviderPatternTests {

    @Test
    @DisplayName("Should create different provider instances")
    void testDifferentProviderInstances() {
      // Act
      ConfigProvider<TestConfig> provider1 = new ConfigProvider<>("test", TestConfig.class);
      ConfigProvider<TestConfig> provider2 = new ConfigProvider<>("test", TestConfig.class);

      // Assert
      assertNotSame(provider1, provider2);
      assertEquals(provider1.getConfigDirectory(), provider2.getConfigDirectory());
      assertEquals(provider1.getClazz(), provider2.getClazz());
    }

    @Test
    @DisplayName("Should support generic types")
    void testGenericTypeSupport() {
      // Act
      ConfigProvider<String> stringProvider = new ConfigProvider<>("string", String.class);
      ConfigProvider<Integer> intProvider = new ConfigProvider<>("int", Integer.class);
      ConfigProvider<TestConfig> configProvider = new ConfigProvider<>("test", TestConfig.class);

      // Assert
      assertEquals(String.class, stringProvider.getClazz());
      assertEquals(Integer.class, intProvider.getClazz());
      assertEquals(TestConfig.class, configProvider.getClazz());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle special characters in directory name")
    void testSpecialCharactersInDirectory() {
      // Arrange
      String specialDirectory = "config_with-special.chars";
      ConfigProvider<TestConfig> provider =
          new ConfigProvider<>(specialDirectory, TestConfig.class);

      // Act
      String configDirectory = provider.getConfigDirectory();
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals(specialDirectory, configDirectory);
      assertEquals("config/config_with-special.chars/%s.conf", configPath);
    }

    @Test
    @DisplayName("Should handle very long directory names")
    void testLongDirectoryName() {
      // Arrange
      String longDirectory = "very-long-directory-name-for-testing-purposes";
      ConfigProvider<TestConfig> provider = new ConfigProvider<>(longDirectory, TestConfig.class);

      // Act
      String configDirectory = provider.getConfigDirectory();
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals(longDirectory, configDirectory);
      assertTrue(configPath.contains(longDirectory));
    }

    @Test
    @DisplayName("Should handle single character directory name")
    void testSingleCharacterDirectory() {
      // Arrange
      String singleChar = "a";
      ConfigProvider<TestConfig> provider = new ConfigProvider<>(singleChar, TestConfig.class);

      // Act
      String configDirectory = provider.getConfigDirectory();
      String configPath = provider.getConfigPath();

      // Assert
      assertEquals(singleChar, configDirectory);
      assertEquals("config/a/%s.conf", configPath);
    }
  }

  @Nested
  @DisplayName("Configuration Format Tests")
  class ConfigurationFormatTests {

    @Test
    @DisplayName("Should use correct config path format")
    void testConfigPathFormat() {
      // Arrange
      ConfigProvider<TestConfig> provider = new ConfigProvider<>("test", TestConfig.class);

      // Act
      String configPath = provider.getConfigPath();

      // Assert
      assertTrue(configPath.startsWith("config/"));
      assertTrue(configPath.contains("/%s.conf"));
      assertTrue(configPath.matches("config/.+/%s\\.conf"));
    }

    @Test
    @DisplayName("Should maintain config path format consistency")
    void testConfigPathFormatConsistency() {
      // Arrange
      ConfigProvider<TestConfig> provider1 = new ConfigProvider<>("test1", TestConfig.class);
      ConfigProvider<TestConfig> provider2 = new ConfigProvider<>("test2", TestConfig.class);

      // Act
      String path1 = provider1.getConfigPath();
      String path2 = provider2.getConfigPath();

      // Assert
      assertTrue(path1.matches("config/test1/%s\\.conf"));
      assertTrue(path2.matches("config/test2/%s\\.conf"));
    }

    @Test
    @DisplayName("Should generate path with placeholder for environment")
    void testConfigPathPlaceholder() {
      // Arrange
      ConfigProvider<TestConfig> provider = new ConfigProvider<>("application", TestConfig.class);

      // Act
      String configPath = provider.getConfigPath();

      // Assert
      assertTrue(configPath.contains("%s"));
      assertEquals("config/application/%s.conf", configPath);
    }
  }

  @Nested
  @DisplayName("Type Safety Tests")
  class TypeSafetyTests {

    @Test
    @DisplayName("Should maintain type safety with different config types")
    void testTypeSafety() {
      // Arrange
      ConfigProvider<TestConfig> testProvider = new ConfigProvider<>("test", TestConfig.class);
      ConfigProvider<AnotherTestConfig> anotherProvider =
          new ConfigProvider<>("another", AnotherTestConfig.class);

      // Act
      Class<TestConfig> testClass = testProvider.getClazz();
      Class<AnotherTestConfig> anotherClass = anotherProvider.getClazz();

      // Assert
      assertEquals(TestConfig.class, testClass);
      assertEquals(AnotherTestConfig.class, anotherClass);
      assertNotEquals(testClass, anotherClass);
    }

    @Test
    @DisplayName("Should handle nested class types")
    void testNestedClassTypes() {
      // Arrange
      ConfigProvider<OuterConfig.InnerConfig> provider =
          new ConfigProvider<>("nested", OuterConfig.InnerConfig.class);

      // Act
      Class<OuterConfig.InnerConfig> clazz = provider.getClazz();

      // Assert
      assertEquals(OuterConfig.InnerConfig.class, clazz);
    }
  }

  @Nested
  @DisplayName("Multiple Provider Tests")
  class MultipleProviderTests {

    @Test
    @DisplayName("Should create multiple providers independently")
    void testMultipleProvidersIndependence() {
      // Act
      ConfigProvider<TestConfig> provider1 = new ConfigProvider<>("config1", TestConfig.class);
      ConfigProvider<TestConfig> provider2 = new ConfigProvider<>("config2", TestConfig.class);
      ConfigProvider<TestConfig> provider3 = new ConfigProvider<>("config3", TestConfig.class);

      // Assert
      assertNotSame(provider1, provider2);
      assertNotSame(provider2, provider3);
      assertNotSame(provider1, provider3);

      assertEquals("config/config1/%s.conf", provider1.getConfigPath());
      assertEquals("config/config2/%s.conf", provider2.getConfigPath());
      assertEquals("config/config3/%s.conf", provider3.getConfigPath());
    }

    @Test
    @DisplayName("Should handle providers with same directory but different types")
    void testSameDirectoryDifferentTypes() {
      // Act
      ConfigProvider<TestConfig> provider1 = new ConfigProvider<>("test", TestConfig.class);
      ConfigProvider<AnotherTestConfig> provider2 =
          new ConfigProvider<>("test", AnotherTestConfig.class);

      // Assert
      assertEquals(provider1.getConfigDirectory(), provider2.getConfigDirectory());
      assertNotEquals(provider1.getClazz(), provider2.getClazz());
    }
  }

  // Helper test classes
  @Data
  private static class TestConfig {
    private String value;
  }

  @Data
  private static class AnotherTestConfig {
    private int number;
  }

  private static class OuterConfig {

    @Data
    static class InnerConfig {
      private String data;
    }
  }
}
