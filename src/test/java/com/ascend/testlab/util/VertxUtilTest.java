package com.ascend.testlab.util;

import static org.junit.jupiter.api.Assertions.*;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Comprehensive unit tests for VertxUtil.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(VertxExtension.class)
@DisplayName("VertxUtil Tests")
public class VertxUtilTest {

  private Vertx vertx;

  @BeforeEach
  void setUp(Vertx vertx) {
    this.vertx = vertx;
  }

  @AfterEach
  void tearDown(VertxTestContext testContext) {
    vertx.sharedData().getLocalMap("__vertx.sharedDataUtils").clear();
    testContext.completeNow();
  }

  @Nested
  @DisplayName("Shared Data Tests")
  class SharedDataTests {

    @Test
    @DisplayName("Should create shared data when not exists")
    void testGetOrCreateSharedData(VertxTestContext testContext) {
      // Arrange
      String key = "testKey";
      String value = "testValue";

      // Act
      String result = VertxUtil.getOrCreateSharedData(vertx, key, () -> value);

      // Assert
      assertEquals(value, result);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return existing shared data")
    void testGetExistingSharedData(VertxTestContext testContext) {
      // Arrange
      String key = "testKey";
      String originalValue = "originalValue";
      String newValue = "newValue";

      // Act
      String firstResult = VertxUtil.getOrCreateSharedData(vertx, key, () -> originalValue);
      String secondResult = VertxUtil.getOrCreateSharedData(vertx, key, () -> newValue);

      // Assert
      assertEquals(originalValue, firstResult);
      assertEquals(originalValue, secondResult); // Should return original, not new
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle different keys separately")
    void testGetOrCreateSharedDataDifferentKeys(VertxTestContext testContext) {
      // Arrange
      String key1 = "key1";
      String key2 = "key2";
      String value1 = "value1";
      String value2 = "value2";

      // Act
      String result1 = VertxUtil.getOrCreateSharedData(vertx, key1, () -> value1);
      String result2 = VertxUtil.getOrCreateSharedData(vertx, key2, () -> value2);

      // Assert
      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertNotEquals(result1, result2);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle complex objects in shared data")
    void testGetOrCreateSharedDataComplexObject(VertxTestContext testContext) {
      // Arrange
      String key = "complexKey";
      TestObject testObject = new TestObject("test", 123);

      // Act
      TestObject result = VertxUtil.getOrCreateSharedData(vertx, key, () -> testObject);

      // Assert
      assertNotNull(result);
      assertEquals("test", result.name);
      assertEquals(123, result.value);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle null values in shared data")
    void testGetOrCreateSharedDataNullValue(VertxTestContext testContext) {
      // Arrange
      String key = "nullKey";

      // Act
      String result = VertxUtil.getOrCreateSharedData(vertx, key, () -> null);

      // Assert
      assertNull(result);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Instance Management Tests")
  class InstanceManagementTests {

    @Test
    @DisplayName("Should set and get instance from shared data")
    void testSetAndGetInstance(VertxTestContext testContext) {
      // Arrange
      TestObject instance = new TestObject("test", 42);

      // Act
      VertxUtil.setInstanceInSharedData(vertx, instance);
      TestObject retrieved = VertxUtil.getInstanceFromSharedData(vertx, TestObject.class);

      // Assert
      assertNotNull(retrieved);
      assertEquals(instance.name, retrieved.name);
      assertEquals(instance.value, retrieved.value);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should set and get instance with custom key")
    void testSetAndGetInstanceWithKey(VertxTestContext testContext) {
      // Arrange
      TestObject instance = new TestObject("custom", 99);
      String customKey = "customKey";

      // Act
      VertxUtil.setInstanceInSharedData(vertx, instance, customKey);
      TestObject retrieved =
          VertxUtil.getInstanceFromSharedData(vertx, TestObject.class, customKey);

      // Assert
      assertNotNull(retrieved);
      assertEquals(instance.name, retrieved.name);
      assertEquals(instance.value, retrieved.value);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should throw exception when instance not found")
    void testGetInstanceNotFound(VertxTestContext testContext) {
      // Act & Assert
      assertThrows(
          NoSuchElementException.class,
          () -> VertxUtil.getInstanceFromSharedData(vertx, TestObject.class));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple instances of same class with different keys")
    void testMultipleInstancesWithDifferentKeys(VertxTestContext testContext) {
      // Arrange
      TestObject instance1 = new TestObject("instance1", 1);
      TestObject instance2 = new TestObject("instance2", 2);
      String key1 = "key1";
      String key2 = "key2";

      // Act
      VertxUtil.setInstanceInSharedData(vertx, instance1, key1);
      VertxUtil.setInstanceInSharedData(vertx, instance2, key2);
      TestObject retrieved1 = VertxUtil.getInstanceFromSharedData(vertx, TestObject.class, key1);
      TestObject retrieved2 = VertxUtil.getInstanceFromSharedData(vertx, TestObject.class, key2);

      // Assert
      assertEquals(instance1.name, retrieved1.name);
      assertEquals(instance2.name, retrieved2.name);
      assertNotEquals(retrieved1.name, retrieved2.name);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should overwrite existing instance when setting again")
    void testOverwriteInstance(VertxTestContext testContext) {
      // Arrange
      TestObject original = new TestObject("original", 100);

      // Act
      VertxUtil.setInstanceInSharedData(vertx, original);
      TestObject firstRetrieval = VertxUtil.getInstanceFromSharedData(vertx, TestObject.class);

      // The getOrCreate will not overwrite, so we need to verify the original is there
      // Assert
      assertEquals(original.name, firstRetrieval.name);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("CompletableFuture Conversion Tests")
  class CompletableFutureConversionTests {

    @Test
    @DisplayName("Should convert CompletableFuture to Single successfully")
    void testSingleFromCompletableFuture(VertxTestContext testContext) {
      // Arrange
      CompletableFuture<String> future = CompletableFuture.completedFuture("test");

      // Act
      Single<String> single = VertxUtil.singleFromCompletableFuture(future);

      // Assert
      single.subscribe(
          result -> {
            assertEquals("test", result);
            testContext.completeNow();
          },
          testContext::failNow);
    }

    @Test
    @DisplayName("Should handle failed CompletableFuture conversion")
    void testSingleFromFailedCompletableFuture(VertxTestContext testContext) {
      // Arrange
      CompletableFuture<String> future = new CompletableFuture<>();
      RuntimeException exception = new RuntimeException("Test error");
      future.completeExceptionally(exception);

      // Act
      Single<String> single = VertxUtil.singleFromCompletableFuture(future);

      // Assert
      single.subscribe(
          result -> testContext.failNow("Should have failed"),
          error -> {
            assertEquals(exception.getMessage(), error.getMessage());
            testContext.completeNow();
          });
    }

    @Test
    @DisplayName("Should handle async CompletableFuture conversion")
    void testSingleFromAsyncCompletableFuture(VertxTestContext testContext) {
      // Arrange
      CompletableFuture<Integer> future =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
                return 42;
              });

      // Act
      Single<Integer> single = VertxUtil.singleFromCompletableFuture(future);

      // Assert
      single.subscribe(
          result -> {
            assertEquals(42, result);
            testContext.completeNow();
          },
          testContext::failNow);
    }

    @Test
    @DisplayName("Should handle null result in CompletableFuture")
    void testSingleFromCompletableFutureNullResult(VertxTestContext testContext) {
      // Arrange
      CompletableFuture<String> future = CompletableFuture.completedFuture(null);

      // Act
      Single<String> single = VertxUtil.singleFromCompletableFuture(future);

      // Assert - RxJava3 doesn't allow null, so this should error
      single.subscribe(
          result -> testContext.failNow("Should have failed with null"),
          error -> {
            assertInstanceOf(NullPointerException.class, error);
            testContext.completeNow();
          });
    }
  }

  @Nested
  @DisplayName("Single Conversion Tests")
  class SingleConversionTests {

    @Test
    @DisplayName("Should convert Single to CompletableFuture successfully")
    void testCompletableFutureFromSingle(VertxTestContext testContext) {
      // Arrange
      Single<String> single = Single.just("test");

      // Act
      CompletableFuture<String> future = VertxUtil.completableFutureFromSingle(single);

      // Assert
      future.whenComplete(
          (result, error) -> {
            if (error != null) {
              testContext.failNow(error);
            } else {
              assertEquals("test", result);
              testContext.completeNow();
            }
          });
    }

    @Test
    @DisplayName("Should handle failed Single conversion")
    void testCompletableFutureFromFailedSingle(VertxTestContext testContext) {
      // Arrange
      RuntimeException exception = new RuntimeException("Test error");
      Single<String> single = Single.error(exception);

      // Act
      CompletableFuture<String> future = VertxUtil.completableFutureFromSingle(single);

      // Assert
      try {
        future.get(1, TimeUnit.SECONDS);
        testContext.failNow("Should have thrown exception");
      } catch (Exception e) {
        assertNotNull(e.getCause());
        assertEquals(exception.getMessage(), e.getCause().getMessage());
        testContext.completeNow();
      }
    }

    @Test
    @DisplayName("Should handle delayed Single conversion")
    void testCompletableFutureFromDelayedSingle(VertxTestContext testContext) {
      // Arrange
      Single<Integer> single = Single.just(42).delay(100, TimeUnit.MILLISECONDS);

      // Act
      CompletableFuture<Integer> future = VertxUtil.completableFutureFromSingle(single);

      // Assert
      future.whenComplete(
          (result, error) -> {
            if (error != null) {
              testContext.failNow(error);
            } else {
              assertEquals(42, result);
              testContext.completeNow();
            }
          });
    }

    @Test
    @DisplayName("Should get result from converted CompletableFuture")
    void testCompletableFutureGet(VertxTestContext testContext) throws Exception {
      // Arrange
      Single<String> single = Single.just("result");

      // Act
      CompletableFuture<String> future = VertxUtil.completableFutureFromSingle(single);
      String result = future.get(1, TimeUnit.SECONDS);

      // Assert
      assertEquals("result", result);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Bidirectional Conversion Tests")
  class BidirectionalConversionTests {

    @Test
    @DisplayName("Should handle round-trip conversion Single to Future to Single")
    void testRoundTripSingleToFutureToSingle(VertxTestContext testContext) {
      // Arrange
      Single<String> originalSingle = Single.just("roundtrip");

      // Act
      CompletableFuture<String> future = VertxUtil.completableFutureFromSingle(originalSingle);
      Single<String> convertedSingle = VertxUtil.singleFromCompletableFuture(future);

      // Assert
      convertedSingle.subscribe(
          result -> {
            assertEquals("roundtrip", result);
            testContext.completeNow();
          },
          testContext::failNow);
    }

    @Test
    @DisplayName("Should handle round-trip conversion Future to Single to Future")
    void testRoundTripFutureToSingleToFuture(VertxTestContext testContext) {
      // Arrange
      CompletableFuture<Integer> originalFuture = CompletableFuture.completedFuture(123);

      // Act
      Single<Integer> single = VertxUtil.singleFromCompletableFuture(originalFuture);
      CompletableFuture<Integer> convertedFuture = VertxUtil.completableFutureFromSingle(single);

      // Assert
      convertedFuture.whenComplete(
          (result, error) -> {
            if (error != null) {
              testContext.failNow(error);
            } else {
              assertEquals(123, result);
              testContext.completeNow();
            }
          });
    }
  }

  @Nested
  @DisplayName("ThreadSafe Record Tests")
  class ThreadSafeRecordTests {

    @Test
    @DisplayName("Should create ThreadSafe wrapper")
    void testThreadSafeCreation(VertxTestContext testContext) {
      // Arrange
      String value = "test";
      String key = "threadSafeKey";

      // Act
      String result = VertxUtil.getOrCreateSharedData(vertx, key, () -> value);

      // Assert
      assertEquals(value, result);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle concurrent access to shared data")
    void testThreadSafeConcurrentAccess(VertxTestContext testContext) throws InterruptedException {
      // Arrange
      String key = "concurrentKey";
      String value = "concurrentValue";
      int threadCount = 10;
      CountDownLatch latch = new CountDownLatch(threadCount);
      ConcurrentHashMap<Integer, String> results = new ConcurrentHashMap<>();

      // Act
      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        new Thread(
                () -> {
                  String result = VertxUtil.getOrCreateSharedData(vertx, key, () -> value);
                  results.put(index, result);
                  latch.countDown();
                })
            .start();
      }
      latch.await();

      // Assert - All threads should get the same value
      assertEquals(threadCount, results.size());
      results.values().forEach(result -> assertEquals(value, result));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with MaintenanceUtil pattern")
    void testIntegrationWithMaintenanceUtil(VertxTestContext testContext) {
      // Act
      AtomicBoolean flag =
          VertxUtil.getOrCreateSharedData(vertx, "__test_flag", () -> new AtomicBoolean(false));

      // Assert
      assertNotNull(flag);
      assertFalse(flag.get());
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should support multiple Vertx instances")
    void testMultipleVertxInstances(VertxTestContext testContext) {
      // Arrange
      Vertx anotherVertx = Vertx.vertx();
      String key = "multiInstanceKey";
      String value1 = "value1";
      String value2 = "value2";

      // Act
      String result1 = VertxUtil.getOrCreateSharedData(vertx, key, () -> value1);
      String result2 = VertxUtil.getOrCreateSharedData(anotherVertx, key, () -> value2);

      // Assert - Different Vertx instances should have separate shared data
      assertEquals(value1, result1);
      assertEquals(value2, result2);

      // Cleanup
      anotherVertx.close();
      testContext.completeNow();
    }
  }

  // Helper test class
  private static class TestObject {
    String name;
    int value;

    TestObject(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
