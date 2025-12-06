package com.ascend.testlab.client.webclient;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.client.webclient.impl.WebClientImpl;
import com.ascend.testlab.config.WebClientConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Comprehensive unit tests for WebClientImpl.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(VertxExtension.class)
@DisplayName("WebClientImpl Tests")
public class WebClientTest {

  private WebClient webClient;
  private Vertx vertx;

  @BeforeEach
  void setUp(io.vertx.core.Vertx coreVertx) {
    this.vertx = Vertx.newInstance(coreVertx);
    WebClientConfig webClientConfig = createDefaultConfig();
    this.webClient = new WebClientImpl(vertx, webClientConfig);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create WebClient with default configuration")
    void testConstructorWithDefaultConfig(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = createDefaultConfig();

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create WebClient with custom configuration")
    void testConstructorWithCustomConfig(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setConnectTimeout(5000);
      config.setMaxPoolSize(64);
      config.setLogActivity(true);
      config.setKeepAlive(true); // Must be true when pipelining is enabled
      config.setKeepAliveTimeout(20);
      config.setPipelining(true);
      config.setPipeliningLimit(16);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should throw NullPointerException when vertx is null")
    void testConstructorWithNullVertx(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = createDefaultConfig();

      // Act & Assert
      assertThrows(NullPointerException.class, () -> new WebClientImpl(null, config));
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should throw NullPointerException when config is null")
    void testConstructorWithNullConfig(VertxTestContext testContext) {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new WebClientImpl(vertx, null));
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Close Operation Tests")
  class CloseOperationTests {

    @Test
    @DisplayName("Should close client successfully")
    void testCloseSuccess(VertxTestContext testContext) {
      // Act
      TestObserver<Void> testObserver = webClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should close client multiple times without error")
    void testCloseMultipleTimes(VertxTestContext testContext) {
      // Act
      TestObserver<Void> testObserver1 = webClient.close().test();
      TestObserver<Void> testObserver2 = webClient.close().test();

      // Assert
      testObserver1.assertComplete();
      testObserver1.assertNoErrors();
      testObserver2.assertComplete();
      testObserver2.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should close client created with custom config")
    void testCloseClientWithCustomConfig(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setConnectTimeout(2000);
      config.setMaxPoolSize(50);
      WebClient customClient = new WebClientImpl(vertx, config);

      // Act
      TestObserver<Void> testObserver = customClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Circuit Breaker Tests")
  class CircuitBreakerTests {

    @Test
    @DisplayName("Should set circuit breaker successfully")
    void testSetCircuitBreaker(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker = createCircuitBreaker("test-cb");

      // Act
      WebClient result = webClient.setCircuitBreaker(circuitBreaker);

      // Assert
      assertNotNull(result);
      assertSame(webClient, result); // Verify fluent API returns same instance
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should support fluent API pattern")
    void testFluentApiPattern(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker = createCircuitBreaker("fluent-test-cb");

      // Act
      WebClient result =
          webClient
              .setCircuitBreaker(circuitBreaker)
              .setCircuitBreaker(circuitBreaker)
              .setCircuitBreaker(circuitBreaker);

      // Assert
      assertNotNull(result);
      assertSame(webClient, result);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should allow setting null circuit breaker")
    void testSetNullCircuitBreaker(VertxTestContext testContext) {
      // Act
      WebClient result = webClient.setCircuitBreaker(null);

      // Assert
      assertNotNull(result);
      assertSame(webClient, result);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should allow replacing circuit breaker")
    void testReplaceCircuitBreaker(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker1 = createCircuitBreaker("cb-1");
      CircuitBreaker circuitBreaker2 = createCircuitBreaker("cb-2");

      // Act
      WebClient result1 = webClient.setCircuitBreaker(circuitBreaker1);
      WebClient result2 = webClient.setCircuitBreaker(circuitBreaker2);

      // Assert
      assertNotNull(result1);
      assertNotNull(result2);
      assertSame(webClient, result1);
      assertSame(webClient, result2);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should set circuit breaker with custom config")
    void testSetCircuitBreakerWithCustomConfig(VertxTestContext testContext) {
      // Arrange
      CircuitBreakerConfig config =
          CircuitBreakerConfig.custom()
              .failureRateThreshold(50)
              .slidingWindowSize(100)
              .minimumNumberOfCalls(10)
              .waitDurationInOpenState(java.time.Duration.ofSeconds(60))
              .permittedNumberOfCallsInHalfOpenState(5)
              .build();

      CircuitBreaker circuitBreaker =
          CircuitBreakerRegistry.of(config).circuitBreaker("custom-config-cb");

      // Act
      WebClient result = webClient.setCircuitBreaker(circuitBreaker);

      // Assert
      assertNotNull(result);
      assertSame(webClient, result);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should create client with minimum connect timeout")
    void testMinimumConnectTimeout(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setConnectTimeout(1);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create client with maximum pool size")
    void testMaximumPoolSize(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setMaxPoolSize(1000);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create client with log activity enabled")
    void testLogActivityEnabled(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setLogActivity(true);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create client with keep alive disabled")
    void testKeepAliveDisabled(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setKeepAlive(false);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create client with pipelining enabled")
    void testPipeliningEnabled(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setPipelining(true);
      config.setPipeliningLimit(32);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create client with all custom configurations")
    void testAllCustomConfigurations(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setConnectTimeout(3000);
      config.setMaxPoolSize(128);
      config.setLogActivity(true);
      config.setKeepAlive(true);
      config.setKeepAliveTimeout(30);
      config.setMaxWaitQueueSize(200);
      config.setPipelining(true);
      config.setPipeliningLimit(24);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should set circuit breaker and then close")
    void testSetCircuitBreakerAndClose(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker = createCircuitBreaker("integration-cb");

      // Act
      webClient.setCircuitBreaker(circuitBreaker);
      TestObserver<Void> testObserver = webClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should create multiple clients with different configs")
    void testMultipleClientsWithDifferentConfigs(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config1 = new WebClientConfig();
      config1.setConnectTimeout(1000);
      config1.setMaxPoolSize(32);

      WebClientConfig config2 = new WebClientConfig();
      config2.setConnectTimeout(2000);
      config2.setMaxPoolSize(64);

      // Act
      WebClient client1 = new WebClientImpl(vertx, config1);
      WebClient client2 = new WebClientImpl(vertx, config2);

      // Assert
      assertNotNull(client1);
      assertNotNull(client2);
      assertNotSame(client1, client2);

      // Cleanup
      client1.close().test().assertComplete();
      client2.close().test().assertComplete();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should set circuit breaker using fluent API and close")
    void testFluentApiWithClose(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker = createCircuitBreaker("fluent-integration-cb");

      // Act
      TestObserver<Void> testObserver = webClient.setCircuitBreaker(circuitBreaker).close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple circuit breaker updates and close")
    void testMultipleCircuitBreakerUpdates(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker cb1 = createCircuitBreaker("cb-1");
      CircuitBreaker cb2 = createCircuitBreaker("cb-2");
      CircuitBreaker cb3 = createCircuitBreaker("cb-3");

      // Act
      webClient.setCircuitBreaker(cb1).setCircuitBreaker(cb2).setCircuitBreaker(cb3);
      TestObserver<Void> testObserver = webClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle close after circuit breaker is set to null")
    void testCloseAfterNullCircuitBreaker(VertxTestContext testContext) {
      // Arrange
      CircuitBreaker circuitBreaker = createCircuitBreaker("null-test-cb");
      webClient.setCircuitBreaker(circuitBreaker);
      webClient.setCircuitBreaker(null);

      // Act
      TestObserver<Void> testObserver = webClient.close().test();

      // Assert
      testObserver.assertComplete();
      testObserver.assertNoErrors();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle zero connect timeout")
    void testZeroConnectTimeout(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setConnectTimeout(0);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      client.close().test().assertComplete();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle very large pool size")
    void testVeryLargePoolSize(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setMaxPoolSize(10000);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      client.close().test().assertComplete();
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle zero keep alive timeout")
    void testZeroKeepAliveTimeout(VertxTestContext testContext) {
      // Arrange
      WebClientConfig config = new WebClientConfig();
      config.setKeepAliveTimeout(0);

      // Act
      WebClient client = new WebClientImpl(vertx, config);

      // Assert
      assertNotNull(client);
      client.close().test().assertComplete();
      testContext.completeNow();
    }
  }

  // Helper methods

  private WebClientConfig createDefaultConfig() {
    return new WebClientConfig();
  }

  private CircuitBreaker createCircuitBreaker(String name) {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
    return CircuitBreakerRegistry.of(config).circuitBreaker(name);
  }
}
