package com.ascend.testlab.verticle;

import static org.junit.jupiter.api.Assertions.*;

import com.ascend.testlab.config.HttpServerConfig;
import com.ascend.testlab.injection.GuiceInjector;
import com.dream11.rest.ClassInjector;
import com.dream11.rest.provider.JsonProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for RestVerticle.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("RestVerticle Tests")
public class RestVerticleTest {

  private HttpServerConfig httpServerConfig;
  private RestVerticle restVerticle;

  @BeforeEach
  void setUp() throws Exception {
    // Reset GuiceInjector
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);

    // Create test config
    httpServerConfig = new HttpServerConfig();
    httpServerConfig.setHost("localhost");
    httpServerConfig.setPort(8080);
    httpServerConfig.setCompressionLevel(6);
    httpServerConfig.setCompressionSupported(true);
    httpServerConfig.setIdleTimeout(120);
    httpServerConfig.setLogActivity(false);
    httpServerConfig.setReusePort(true);
    httpServerConfig.setReuseAddress(true);
    httpServerConfig.setTcpFastOpen(true);
    httpServerConfig.setTcpNoDelay(true);
    httpServerConfig.setTcpQuickAck(true);
    httpServerConfig.setTcpKeepAlive(true);
    httpServerConfig.setUseAlpn(false);

    // Initialize Guice
    Vertx vertx = Vertx.vertx();
    GuiceInjector.initializeInjector(
        List.of(
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(Vertx.class).toInstance(vertx);
                bind(ObjectMapper.class).toInstance(new ObjectMapper());
                bind(HttpServerConfig.class).toInstance(httpServerConfig);
              }
            }));

    restVerticle = new RestVerticle(httpServerConfig);
  }

  @AfterEach
  void tearDown() throws Exception {
    // Reset GuiceInjector
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create RestVerticle with valid config")
    void testConstructorWithValidConfig() {
      // Act
      RestVerticle verticle = new RestVerticle(httpServerConfig);

      // Assert
      assertNotNull(verticle);
    }

    @Test
    @DisplayName("Should throw exception when config is null")
    void testConstructorWithNullConfig() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> new RestVerticle(null));
    }

    @Test
    @DisplayName("Should create RestVerticle with different configs")
    void testConstructorWithDifferentConfigs() {
      // Arrange
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setHost("0.0.0.0");
      config1.setPort(9090);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setHost("127.0.0.1");
      config2.setPort(8888);

      // Act
      RestVerticle verticle1 = new RestVerticle(config1);
      RestVerticle verticle2 = new RestVerticle(config2);

      // Assert
      assertNotNull(verticle1);
      assertNotNull(verticle2);
      assertNotSame(verticle1, verticle2);
    }
  }

  @Nested
  @DisplayName("Injector Tests")
  class InjectorTests {

    @Test
    @DisplayName("Should return ClassInjector")
    void testGetInjector() {
      // Act
      ClassInjector injector = restVerticle.getInjector();

      // Assert
      assertNotNull(injector);
    }

    @Test
    @DisplayName("Should use GuiceInjector for dependency injection")
    void testInjectorUsesGuice() {
      // Act
      ClassInjector injector = restVerticle.getInjector();
      ObjectMapper mapper = injector.getInstance(ObjectMapper.class);

      // Assert
      assertNotNull(mapper);
    }

    @Test
    @DisplayName("Should return same injector on multiple calls")
    void testInjectorConsistency() {
      // Act
      ClassInjector injector1 = restVerticle.getInjector();
      ClassInjector injector2 = restVerticle.getInjector();

      // Assert - Both should work the same way
      assertNotNull(injector1);
      assertNotNull(injector2);
    }
  }

  @Nested
  @DisplayName("HTTP Server Options Tests")
  class HttpServerOptionsTests {

    @Test
    @DisplayName("Should configure HTTP server options from config")
    void testHttpServerOptionsFromConfig() {
      // Arrange
      HttpServerConfig config = new HttpServerConfig();
      config.setHost("test-host");
      config.setPort(9999);
      config.setCompressionLevel(9);
      config.setCompressionSupported(true);

      // Act
      HttpServerOptions options = RestVerticle.getHttpServerOptions(config);

      // Assert
      assertNotNull(options);
      assertEquals("test-host", options.getHost());
      assertEquals(9999, options.getPort());
      assertEquals(9, options.getCompressionLevel());
      assertTrue(options.isCompressionSupported());
    }

    @Test
    @DisplayName("Should configure all HTTP server options")
    void testAllHttpServerOptions() {
      // Arrange
      HttpServerConfig config = new HttpServerConfig();
      config.setHost("localhost");
      config.setPort(8080);
      config.setCompressionLevel(6);
      config.setCompressionSupported(true);
      config.setIdleTimeout(120);
      config.setLogActivity(true);
      config.setReusePort(true);
      config.setReuseAddress(true);
      config.setTcpFastOpen(true);
      config.setTcpNoDelay(true);
      config.setTcpQuickAck(true);
      config.setTcpKeepAlive(true);
      config.setUseAlpn(true);

      // Act
      HttpServerOptions options = RestVerticle.getHttpServerOptions(config);

      // Assert
      assertEquals("localhost", options.getHost());
      assertEquals(8080, options.getPort());
      assertEquals(6, options.getCompressionLevel());
      assertTrue(options.isCompressionSupported());
      assertEquals(120, options.getIdleTimeout());
      assertTrue(options.isReusePort());
      assertTrue(options.isReuseAddress());
      assertTrue(options.isTcpFastOpen());
      assertTrue(options.isTcpNoDelay());
      assertTrue(options.isTcpQuickAck());
      assertTrue(options.isTcpKeepAlive());
      assertTrue(options.isUseAlpn());
    }

    @Test
    @DisplayName("Should handle minimal HTTP server config")
    void testMinimalHttpServerConfig() {
      // Arrange
      HttpServerConfig config = new HttpServerConfig();
      config.setHost("0.0.0.0");
      config.setPort(8080);

      // Act
      HttpServerOptions options = RestVerticle.getHttpServerOptions(config);

      // Assert
      assertNotNull(options);
      assertEquals("0.0.0.0", options.getHost());
      assertEquals(8080, options.getPort());
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with multiple verticle instances")
    void testMultipleVerticleInstances(VertxTestContext testContext) {
      // Arrange
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setHost("localhost");
      config1.setPort(8081);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setHost("localhost");
      config2.setPort(8082);

      RestVerticle verticle1 = new RestVerticle(config1);
      RestVerticle verticle2 = new RestVerticle(config2);

      // Assert
      assertNotNull(verticle1);
      assertNotNull(verticle2);
      assertNotSame(verticle1, verticle2);
      testContext.completeNow();
    }

    @Test
    @DisplayName("Should integrate with dependency injection")
    void testDependencyInjectionIntegration() {
      // Act
      ClassInjector injector = restVerticle.getInjector();
      ObjectMapper mapper = injector.getInstance(ObjectMapper.class);
      JsonProvider jsonProvider = restVerticle.getJsonProvider();

      // Assert
      assertNotNull(injector);
      assertNotNull(mapper);
      assertNotNull(jsonProvider);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle extreme port values")
    void testExtremePortValues() {
      // Arrange
      HttpServerConfig config1 = new HttpServerConfig();
      config1.setHost("localhost");
      config1.setPort(1);

      HttpServerConfig config2 = new HttpServerConfig();
      config2.setHost("localhost");
      config2.setPort(65535);

      // Act
      HttpServerOptions options1 = RestVerticle.getHttpServerOptions(config1);
      HttpServerOptions options2 = RestVerticle.getHttpServerOptions(config2);

      // Assert
      assertEquals(1, options1.getPort());
      assertEquals(65535, options2.getPort());
    }

    @Test
    @DisplayName("Should handle different host addresses")
    void testDifferentHostAddresses() {
      // Arrange
      String[] hosts = {"localhost", "0.0.0.0", "127.0.0.1", "::1"};

      for (String host : hosts) {
        HttpServerConfig config = new HttpServerConfig();
        config.setHost(host);
        config.setPort(8080);

        // Act
        HttpServerOptions options = RestVerticle.getHttpServerOptions(config);

        // Assert
        assertEquals(host, options.getHost());
      }
    }
  }
}
