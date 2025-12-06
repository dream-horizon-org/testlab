package com.ascend.testlab.injection.module;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.rxjava3.core.Vertx;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for DefaultModule.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("DefaultModule Tests")
public class DefaultModuleTest {

  private Vertx vertx;
  private DefaultModule module;

  @BeforeEach
  void setUp() {
    vertx = Vertx.vertx();
    module = new DefaultModule(vertx);
  }

  @AfterEach
  void tearDown() {
    if (vertx != null) {
      vertx.close();
    }
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create module with valid Vertx instance")
    void testConstructorWithVertx() {
      // Act
      DefaultModule newModule = new DefaultModule(vertx);

      // Assert
      assertNotNull(newModule);
    }

    @Test
    @DisplayName("Should throw exception when Vertx is null")
    void testConstructorWithNullVertx() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new DefaultModule(null));
    }

    @Test
    @DisplayName("Should accept different Vertx instances")
    void testConstructorWithDifferentVertx() {
      // Arrange
      Vertx anotherVertx = Vertx.vertx();

      try {
        // Act
        DefaultModule newModule = new DefaultModule(anotherVertx);

        // Assert
        assertNotNull(newModule);
      } finally {
        anotherVertx.close();
      }
    }
  }

  @Nested
  @DisplayName("Binding Tests")
  class BindingTests {

    @Test
    @DisplayName("Should bind Vertx instance")
    void testVertxBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      Vertx boundVertx = injector.getInstance(Vertx.class);

      // Assert
      assertNotNull(boundVertx);
      assertSame(vertx, boundVertx);
    }

    @Test
    @DisplayName("Should bind ObjectMapper instance")
    void testObjectMapperBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

      // Assert
      assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("Should bind same Vertx instance on multiple calls")
    void testVertxSingletonBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      Vertx vertx1 = injector.getInstance(Vertx.class);
      Vertx vertx2 = injector.getInstance(Vertx.class);

      // Assert
      assertSame(vertx1, vertx2);
      assertSame(vertx, vertx1);
    }

    @Test
    @DisplayName("Should bind same ObjectMapper instance on multiple calls")
    void testObjectMapperSingletonBinding() {
      // Act
      Injector injector = Guice.createInjector(module);
      ObjectMapper mapper1 = injector.getInstance(ObjectMapper.class);
      ObjectMapper mapper2 = injector.getInstance(ObjectMapper.class);

      // Assert
      assertSame(mapper1, mapper2);
    }
  }

  @Nested
  @DisplayName("ObjectMapper Configuration Tests")
  class ObjectMapperConfigurationTests {

    @Test
    @DisplayName("Should be able to serialize objects")
    void testObjectMapperSerialization() throws Exception {
      // Arrange
      Injector injector = Guice.createInjector(module);
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
      TestObject testObject = new TestObject("test", 123);

      // Act
      String json = objectMapper.writeValueAsString(testObject);

      // Assert
      assertNotNull(json);
      assertTrue(json.contains("test"));
      assertTrue(json.contains("123"));
    }

    @Test
    @DisplayName("Should be able to deserialize objects")
    void testObjectMapperDeserialization() throws Exception {
      // Arrange
      Injector injector = Guice.createInjector(module);
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
      String json = "{\"name\":\"test\",\"value\":123}";

      // Act
      TestObject testObject = objectMapper.readValue(json, TestObject.class);

      // Assert
      assertNotNull(testObject);
      assertEquals("test", testObject.getName());
      assertEquals(123, testObject.getValue());
    }
  }

  @Nested
  @DisplayName("Module Inheritance Tests")
  class ModuleInheritanceTests {

    @Test
    @DisplayName("Should be usable as Module")
    void testUsableAsModule() {
      // Arrange
      Module guiceModule = module;

      // Act
      Injector injector = Guice.createInjector(guiceModule);

      // Assert
      assertNotNull(injector);
      assertNotNull(injector.getInstance(Vertx.class));
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle Vertx close gracefully")
    void testVertxClose() {
      // Arrange
      Vertx testVertx = Vertx.vertx();
      DefaultModule testModule = new DefaultModule(testVertx);
      Injector injector = Guice.createInjector(testModule);
      Vertx boundVertx = injector.getInstance(Vertx.class);

      // Act
      testVertx.close();

      // Assert - Should still have reference but be closed
      assertNotNull(boundVertx);
    }

    @Test
    @DisplayName("Should create independent module instances")
    void testIndependentModuleInstances() {
      // Arrange
      Vertx vertx2 = Vertx.vertx();

      try {
        DefaultModule module1 = new DefaultModule(vertx);
        DefaultModule module2 = new DefaultModule(vertx2);

        // Act
        Injector injector1 = Guice.createInjector(module1);
        Injector injector2 = Guice.createInjector(module2);

        // Assert
        assertNotSame(injector1.getInstance(Vertx.class), injector2.getInstance(Vertx.class));
      } finally {
        vertx2.close();
      }
    }
  }

  // Helper classes
  @Data
  @NoArgsConstructor
  static class TestObject {
    private String name;
    private int value;

    public TestObject(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
