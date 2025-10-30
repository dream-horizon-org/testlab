package com.ascend.testlab.config;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.provider.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  void setUp() {}

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
}
