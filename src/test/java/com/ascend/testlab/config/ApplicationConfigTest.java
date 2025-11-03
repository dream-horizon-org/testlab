package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for ApplicationConfig.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("ApplicationConfig Tests")
public class ApplicationConfigTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create config with no-arg constructor")
    void testNoArgConstructor() {
      // Act
      ApplicationConfig newConfig = new ApplicationConfig();

      // Assert
      assertNotNull(newConfig);
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should create ConfigProvider instance")
    void testProviderCreation() {
      // Act
      ConfigProvider<ApplicationConfig> provider = ApplicationConfig.provider();

      // Assert
      assertNotNull(provider);
      assertEquals("application", provider.getConfigDirectory());
      assertEquals(ApplicationConfig.class, provider.getClazz());
    }

    @Test
    @DisplayName("Should return new provider instance each time")
    void testProviderNewInstance() {
      // Act
      ConfigProvider<ApplicationConfig> provider1 = ApplicationConfig.provider();
      ConfigProvider<ApplicationConfig> provider2 = ApplicationConfig.provider();

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
    @DisplayName("Should be equal when both are empty")
    void testEqualsEmpty() {
      // Arrange
      ApplicationConfig config1 = new ApplicationConfig();
      ApplicationConfig config2 = new ApplicationConfig();

      // Assert
      assertEquals(config1, config2);
    }

    @Test
    @DisplayName("Should have same hashCode when equal")
    void testHashCode() {
      // Arrange
      ApplicationConfig config1 = new ApplicationConfig();
      ApplicationConfig config2 = new ApplicationConfig();

      // Assert
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testNotEqualsNull() {
      // Arrange
      ApplicationConfig config = new ApplicationConfig();

      // Assert
      assertNotEquals(null, config);
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
      // Arrange
      ApplicationConfig config = new ApplicationConfig();

      // Act
      String result = config.toString();

      // Assert
      assertNotNull(result);
      assertTrue(result.contains("ApplicationConfig"));
    }
  }
}
