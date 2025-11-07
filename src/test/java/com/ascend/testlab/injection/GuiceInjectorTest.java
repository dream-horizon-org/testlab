package com.ascend.testlab.injection;

import static org.junit.jupiter.api.Assertions.*;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for GuiceInjector.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("GuiceInjector Tests")
public class GuiceInjectorTest {

  @BeforeEach
  void setUp() throws Exception {
    // Reset the singleton instance before each test using reflection
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @AfterEach
  void tearDown() throws Exception {
    // Clean up the singleton instance after each test
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Nested
  @DisplayName("Initialization Tests")
  class InitializationTests {

    @Test
    @DisplayName("Should initialize injector with single module")
    void testInitializeWithSingleModule() {
      // Arrange
      List<Module> modules = List.of(new TestModule());

      // Act
      GuiceInjector.initializeInjector(modules);
      TestClass instance = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertNotNull(instance);
    }

    @Test
    @DisplayName("Should initialize injector with multiple modules")
    void testInitializeWithMultipleModules() {
      // Arrange
      List<Module> modules = List.of(new TestModule(), new AnotherTestModule());

      // Act
      GuiceInjector.initializeInjector(modules);
      TestClass instance1 = GuiceInjector.getInstance(TestClass.class);
      AnotherTestClass instance2 = GuiceInjector.getInstance(AnotherTestClass.class);

      // Assert
      assertNotNull(instance1);
      assertNotNull(instance2);
    }

    @Test
    @DisplayName("Should initialize injector with empty module list")
    void testInitializeWithEmptyModules() {
      // Arrange
      List<Module> modules = new ArrayList<>();

      // Act & Assert - Should not throw exception
      assertDoesNotThrow(() -> GuiceInjector.initializeInjector(modules));
    }

    @Test
    @DisplayName("Should throw exception when initializing twice")
    void testInitializeTwice() {
      // Arrange
      List<Module> modules = List.of(new TestModule());
      GuiceInjector.initializeInjector(modules);

      // Act & Assert
      assertThrows(IllegalStateException.class, () -> GuiceInjector.initializeInjector(modules));
    }

    @Test
    @DisplayName("Should throw exception when initializing after already initialized")
    void testInitializeAfterInitialized() {
      // Arrange
      List<Module> modules1 = List.of(new TestModule());
      List<Module> modules2 = List.of(new AnotherTestModule());
      GuiceInjector.initializeInjector(modules1);

      // Act & Assert
      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class, () -> GuiceInjector.initializeInjector(modules2));
      assertEquals("GuiceInjector is already initialized", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("GetInstance Tests")
  class GetInstanceTests {

    @Test
    @DisplayName("Should get instance of bound class")
    void testGetInstanceBoundClass() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));

      // Act
      TestClass instance = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertNotNull(instance);
    }

    @Test
    @DisplayName("Should return same instance for singleton")
    void testGetInstanceSingleton() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));

      // Act
      TestClass instance1 = GuiceInjector.getInstance(TestClass.class);
      TestClass instance2 = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should get multiple different instances")
    void testGetMultipleDifferentInstances() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule(), new AnotherTestModule()));

      // Act
      TestClass instance1 = GuiceInjector.getInstance(TestClass.class);
      AnotherTestClass instance2 = GuiceInjector.getInstance(AnotherTestClass.class);

      // Assert
      assertNotNull(instance1);
      assertNotNull(instance2);
    }

    @Test
    @DisplayName("Should throw exception when getting instance before initialization")
    void testGetInstanceBeforeInitialization() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> GuiceInjector.getInstance(TestClass.class));
    }

    @Test
    @DisplayName("Should be able to get instance of class with default constructor")
    void testGetInstanceUnboundClass() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));

      // Act
      UnboundClass instance = GuiceInjector.getInstance(UnboundClass.class);

      // Assert - Guice can instantiate classes with default constructors
      assertNotNull(instance);
    }
  }

  @Nested
  @DisplayName("Singleton Pattern Tests")
  class SingletonPatternTests {

    @Test
    @DisplayName("Should maintain singleton instance across multiple calls")
    void testSingletonMaintained() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));

      // Act
      TestClass instance1 = GuiceInjector.getInstance(TestClass.class);
      TestClass instance2 = GuiceInjector.getInstance(TestClass.class);
      TestClass instance3 = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertSame(instance1, instance2);
      assertSame(instance2, instance3);
    }
  }

  @Nested
  @DisplayName("Module Configuration Tests")
  class ModuleConfigurationTests {

    @Test
    @DisplayName("Should handle module with bindings")
    void testModuleWithBindings() {
      // Arrange
      Module module =
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(TestClass.class);
              bind(AnotherTestClass.class);
            }
          };

      // Act
      GuiceInjector.initializeInjector(List.of(module));

      // Assert
      assertNotNull(GuiceInjector.getInstance(TestClass.class));
      assertNotNull(GuiceInjector.getInstance(AnotherTestClass.class));
    }

    @Test
    @DisplayName("Should handle module with instance bindings")
    void testModuleWithInstanceBindings() {
      // Arrange
      TestClass testInstance = new TestClass();
      Module module =
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(TestClass.class).toInstance(testInstance);
            }
          };

      // Act
      GuiceInjector.initializeInjector(List.of(module));
      TestClass retrievedInstance = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertSame(testInstance, retrievedInstance);
    }

    @Test
    @DisplayName("Should handle module with singleton scope")
    void testModuleWithSingletonScope() {
      // Arrange
      Module module =
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(TestClass.class).in(Singleton.class);
            }
          };

      // Act
      GuiceInjector.initializeInjector(List.of(module));
      TestClass instance1 = GuiceInjector.getInstance(TestClass.class);
      TestClass instance2 = GuiceInjector.getInstance(TestClass.class);

      // Assert
      assertSame(instance1, instance2);
    }
  }

  @Nested
  @DisplayName("Thread Safety Tests")
  class ThreadSafetyTests {

    @Test
    @DisplayName("Should handle concurrent initialization attempts")
    void testConcurrentInitialization() throws InterruptedException {
      // Arrange
      List<Module> modules = List.of(new TestModule());
      int threadCount = 10;
      CountDownLatch latch = new CountDownLatch(threadCount);
      AtomicInteger successCount = new AtomicInteger(0);
      AtomicInteger exceptionCount = new AtomicInteger(0);

      // Act
      for (int i = 0; i < threadCount; i++) {
        new Thread(
                () -> {
                  try {
                    GuiceInjector.initializeInjector(modules);
                    successCount.incrementAndGet();
                  } catch (IllegalStateException e) {
                    exceptionCount.incrementAndGet();
                  } finally {
                    latch.countDown();
                  }
                })
            .start();
      }
      latch.await();

      // Assert - Only one should succeed, others should get exception
      assertEquals(1, successCount.get());
      assertEquals(threadCount - 1, exceptionCount.get());
    }

    @Test
    @DisplayName("Should handle concurrent getInstance calls")
    void testConcurrentGetInstance() throws InterruptedException {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));
      int threadCount = 10;
      CountDownLatch latch = new CountDownLatch(threadCount);
      ConcurrentHashMap<Integer, TestClass> results = new ConcurrentHashMap<>();

      // Act
      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        new Thread(
                () -> {
                  results.put(index, GuiceInjector.getInstance(TestClass.class));
                  latch.countDown();
                })
            .start();
      }
      latch.await();

      // Assert - All should get the same singleton instance
      TestClass firstInstance = results.get(0);
      results.values().forEach(instance -> assertSame(firstInstance, instance));
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null module list gracefully")
    void testNullModuleList() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> GuiceInjector.initializeInjector(null));
    }

    @Test
    @DisplayName("Should handle module list with null elements")
    void testModuleListWithNullElements() {
      // Arrange
      List<Module> modules = new ArrayList<>();
      modules.add(null);

      // Act & Assert
      assertThrows(Exception.class, () -> GuiceInjector.initializeInjector(modules));
    }

    @Test
    @DisplayName("Should handle getting null class")
    void testGetInstanceNullClass() {
      // Arrange
      GuiceInjector.initializeInjector(List.of(new TestModule()));

      // Act & Assert
      assertThrows(NullPointerException.class, () -> GuiceInjector.getInstance(null));
    }
  }

  // Helper classes and interfaces
  interface TestInterface {}

  static class TestClass implements TestInterface {}

  static class AnotherTestClass {}

  static class UnboundClass {}

  static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TestClass.class).in(Singleton.class);
    }
  }

  static class AnotherTestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(AnotherTestClass.class).in(Singleton.class);
    }
  }
}
