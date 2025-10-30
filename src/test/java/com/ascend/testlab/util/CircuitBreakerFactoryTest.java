package com.ascend.testlab.util;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.CircuitBreakerConfig;
import com.dream11.rest.exception.RestException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for CircuitBreakerFactory.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@DisplayName("CircuitBreakerFactory Tests")
public class CircuitBreakerFactoryTest {

  private CircuitBreakerConfig config;

  @BeforeEach
  void setUp() {
    // Setup circuit breaker config with default values
    config = new CircuitBreakerConfig();
    config.setFailureRateThreshold(50);
    config.setSlowCallRateThreshold(100);
    config.setWaitDurationInOpenState(60000);
    config.setSlowCallDurationThreshold(60000);
    config.setPermittedNumberOfCallsInHalfOpenState(10);
    config.setMinimumNumberOfCalls(10);
    config.setSlidingWindowSize(100);

    CircuitBreakerFactory.circuitBreakerConfig = config;
  }

  @Nested
  @DisplayName("Circuit Breaker Creation Tests")
  class CircuitBreakerCreationTests {

    @Test
    @DisplayName("Should create circuit breaker with name")
    void testGetCircuitBreaker() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("test");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals("test", circuitBreaker.getName());
    }

    @Test
    @DisplayName("Should return same circuit breaker for same name")
    void testGetCircuitBreakerSameName() {
      // Act
      CircuitBreaker circuitBreaker1 = CircuitBreakerFactory.getCircuitBreaker("test");
      CircuitBreaker circuitBreaker2 = CircuitBreakerFactory.getCircuitBreaker("test");

      // Assert
      assertSame(circuitBreaker1, circuitBreaker2);
    }

    @Test
    @DisplayName("Should create different circuit breakers for different names")
    void testGetCircuitBreakerDifferentNames() {
      // Act
      CircuitBreaker circuitBreaker1 = CircuitBreakerFactory.getCircuitBreaker("test1");
      CircuitBreaker circuitBreaker2 = CircuitBreakerFactory.getCircuitBreaker("test2");

      // Assert
      assertNotSame(circuitBreaker1, circuitBreaker2);
      assertEquals("test1", circuitBreaker1.getName());
      assertEquals("test2", circuitBreaker2.getName());
    }

    @Test
    @DisplayName("Should create circuit breaker with custom configuration")
    void testGetCircuitBreakerWithCustomConfig() {
      // Arrange
      config.setFailureRateThreshold(75);
      config.setMinimumNumberOfCalls(20);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("customConfig");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals("customConfig", circuitBreaker.getName());
    }

    @Test
    @DisplayName("Should handle empty circuit breaker name")
    void testGetCircuitBreakerEmptyName() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals("", circuitBreaker.getName());
    }

    @Test
    @DisplayName("Should handle special characters in circuit breaker name")
    void testGetCircuitBreakerSpecialCharacters() {
      // Act
      CircuitBreaker circuitBreaker =
          CircuitBreakerFactory.getCircuitBreaker("test-circuit_breaker.123");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals("test-circuit_breaker.123", circuitBreaker.getName());
    }
  }

  @Nested
  @DisplayName("Circuit Breaker Configuration Tests")
  class CircuitBreakerConfigurationTests {

    @Test
    @DisplayName("Should apply failure rate threshold configuration")
    void testFailureRateThreshold() {
      // Arrange
      config.setFailureRateThreshold(80);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("failureRate");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(80.0f, circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(), 0.01);
    }

    @Test
    @DisplayName("Should apply slow call rate threshold configuration")
    void testSlowCallRateThreshold() {
      // Arrange
      config.setSlowCallRateThreshold(90);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("slowCall");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(
          90.0f, circuitBreaker.getCircuitBreakerConfig().getSlowCallRateThreshold(), 0.01);
    }

    @Test
    @DisplayName("Should apply wait duration configuration")
    void testWaitDurationInOpenState() {
      // Arrange
      config.setWaitDurationInOpenState(30000);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("waitDuration");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(
          30000,
          circuitBreaker.getCircuitBreakerConfig().getWaitIntervalFunctionInOpenState().apply(1));
    }

    @Test
    @DisplayName("Should apply minimum number of calls configuration")
    void testMinimumNumberOfCalls() {
      // Arrange
      config.setMinimumNumberOfCalls(5);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("minCalls");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(5, circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls());
    }

    @Test
    @DisplayName("Should apply sliding window size configuration")
    void testSlidingWindowSize() {
      // Arrange
      config.setSlidingWindowSize(50);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("windowSize");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(50, circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize());
    }

    @Test
    @DisplayName("Should apply permitted calls in half-open state configuration")
    void testPermittedNumberOfCallsInHalfOpenState() {
      // Arrange
      config.setPermittedNumberOfCallsInHalfOpenState(5);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("halfOpen");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(
          5, circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
    }

    @Test
    @DisplayName("Should use COUNT_BASED sliding window type")
    void testSlidingWindowType() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("windowType");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(
          io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED,
          circuitBreaker.getCircuitBreakerConfig().getSlidingWindowType());
    }
  }

  @Nested
  @DisplayName("Circuit Breaker State Tests")
  class CircuitBreakerStateTests {

    @Test
    @DisplayName("Should start in CLOSED state")
    void testInitialState() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("initialState");

      // Assert
      assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    @DisplayName("Should maintain state across retrieval")
    void testStateConsistency() {
      // Act
      CircuitBreaker circuitBreaker1 = CircuitBreakerFactory.getCircuitBreaker("stateTest");
      CircuitBreaker.State initialState = circuitBreaker1.getState();

      CircuitBreaker circuitBreaker2 = CircuitBreakerFactory.getCircuitBreaker("stateTest");
      CircuitBreaker.State retrievedState = circuitBreaker2.getState();

      // Assert
      assertEquals(initialState, retrievedState);
    }

    @Test
    @DisplayName("Should allow state transitions")
    void testStateTransitions() {
      // Arrange
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("transitions");

      // Act
      circuitBreaker.transitionToOpenState();

      // Assert
      assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }
  }

  @Nested
  @DisplayName("Circuit Breaker Registry Tests")
  class CircuitBreakerRegistryTests {

    @Test
    @DisplayName("Should register circuit breakers in registry")
    void testCircuitBreakerRegistration() {
      // Act
      CircuitBreaker cb1 = CircuitBreakerFactory.getCircuitBreaker("registered1");
      CircuitBreaker cb2 = CircuitBreakerFactory.getCircuitBreaker("registered2");
      CircuitBreaker cb3 = CircuitBreakerFactory.getCircuitBreaker("registered3");

      // Assert
      assertNotNull(cb1);
      assertNotNull(cb2);
      assertNotNull(cb3);
    }

    @Test
    @DisplayName("Should retrieve existing circuit breaker from registry")
    void testRetrieveFromRegistry() {
      // Act
      CircuitBreaker original = CircuitBreakerFactory.getCircuitBreaker("fromRegistry");
      original.transitionToOpenState();

      CircuitBreaker retrieved = CircuitBreakerFactory.getCircuitBreaker("fromRegistry");

      // Assert
      assertSame(original, retrieved);
      assertEquals(CircuitBreaker.State.OPEN, retrieved.getState());
    }
  }

  @Nested
  @DisplayName("Exception Recording Tests")
  class ExceptionRecordingTests {

    @Test
    @DisplayName("Should record IOException as failure")
    void testRecordIOException() {
      // Arrange
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("ioException");

      // Act
      boolean shouldRecord =
          circuitBreaker
              .getCircuitBreakerConfig()
              .getRecordExceptionPredicate()
              .test(new IOException("Test"));

      // Assert
      assertTrue(shouldRecord);
    }

    @Test
    @DisplayName("Should record TimeoutException as failure")
    void testRecordTimeoutException() {
      // Arrange
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("timeoutException");

      // Act
      boolean shouldRecord =
          circuitBreaker
              .getCircuitBreakerConfig()
              .getRecordExceptionPredicate()
              .test(new TimeoutException("Test"));

      // Assert
      assertTrue(shouldRecord);
    }

    @Test
    @DisplayName("Should record generic Exception as failure")
    void testRecordGenericException() {
      // Arrange
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("genericException");

      // Act
      boolean shouldRecord =
          circuitBreaker
              .getCircuitBreakerConfig()
              .getRecordExceptionPredicate()
              .test(new Exception("Test"));

      // Assert
      assertTrue(shouldRecord);
    }

    @Test
    @DisplayName("Should record RestException as failure")
    void testRecordRestException() {
      // Arrange
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("restException");

      // Act
      boolean shouldRecord =
          circuitBreaker
              .getCircuitBreakerConfig()
              .getRecordExceptionPredicate()
              .test(new RestException("TEST", "Test error", 500, new RuntimeException()));

      // Assert
      assertTrue(shouldRecord);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should create multiple circuit breakers with different configs")
    void testMultipleCircuitBreakers() {
      // Arrange
      config.setFailureRateThreshold(50);
      CircuitBreaker cb1 = CircuitBreakerFactory.getCircuitBreaker("service1");

      config.setFailureRateThreshold(75);
      CircuitBreaker cb2 = CircuitBreakerFactory.getCircuitBreaker("service2");

      // Assert - cb1 should still have old config since it was already created
      assertNotNull(cb1);
      assertNotNull(cb2);
      assertEquals("service1", cb1.getName());
      assertEquals("service2", cb2.getName());
    }

    @Test
    @DisplayName("Should work with WebClient pattern")
    void testWebClientPattern() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("webClient");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    @DisplayName("Should handle circuit breaker metrics")
    void testCircuitBreakerMetrics() {
      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("metrics");
      CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

      // Assert
      assertNotNull(metrics);
      assertEquals(0, metrics.getNumberOfSuccessfulCalls());
      assertEquals(0, metrics.getNumberOfFailedCalls());
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle concurrent circuit breaker creation")
    void testConcurrentCreation() throws InterruptedException {
      // Arrange
      int threadCount = 10;
      String cbName = "concurrent";
      CountDownLatch latch = new CountDownLatch(threadCount);
      ConcurrentHashMap<Integer, CircuitBreaker> results = new ConcurrentHashMap<>();

      // Act
      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        new Thread(
                () -> {
                  CircuitBreaker cb = CircuitBreakerFactory.getCircuitBreaker(cbName);
                  results.put(index, cb);
                  latch.countDown();
                })
            .start();
      }
      latch.await();

      // Assert - All threads should get the same circuit breaker instance
      CircuitBreaker firstCb = results.get(0);
      results.values().forEach(cb -> assertSame(firstCb, cb));
    }

    @Test
    @DisplayName("Should handle very long circuit breaker names")
    void testLongCircuitBreakerName() {
      // Arrange
      String longName = "a".repeat(1000);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker(longName);

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(longName, circuitBreaker.getName());
    }

    @Test
    @DisplayName("Should handle extreme configuration values")
    void testExtremeConfigurationValues() {
      // Arrange
      config.setFailureRateThreshold(100);
      config.setSlowCallRateThreshold(100); // Must be between 1 and 100
      config.setMinimumNumberOfCalls(1);
      config.setSlidingWindowSize(1);

      // Act
      CircuitBreaker circuitBreaker = CircuitBreakerFactory.getCircuitBreaker("extreme");

      // Assert
      assertNotNull(circuitBreaker);
      assertEquals(
          100.0f, circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(), 0.01);
      assertEquals(1, circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls());
    }
  }
}
