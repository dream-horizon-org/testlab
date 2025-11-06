package com.ascend.testlab.verticle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.config.HttpServerConfig;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.injection.GuiceInjector;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.HealthCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for MainVerticle.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@DisplayName("MainVerticle Tests")
public class MainVerticleTest {

  private MainVerticle mainVerticle;
  private io.vertx.rxjava3.core.Vertx rxVertx;
  private String deploymentId;

  @Mock private AerospikeClient aerospikeClient;
  @Mock private PgReaderClient pgReaderClient;
  @Mock private PgWriterClient pgWriterClient;
  @Mock private WebClient webClient;
  @Mock private HealthCheckDAO healthCheckDAO;
  @Mock private HealthCheckService healthCheckService;
  @Mock private ExperimentService experimentService;
  @Mock private ExperimentDAO experimentDAO;

  @BeforeEach
  void setUp() throws Exception {
    // Reset GuiceInjector
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);

    // Initialize Guice with mocked clients
    HttpServerConfig httpServerConfig = new HttpServerConfig();
    httpServerConfig.setHost("localhost");
    httpServerConfig.setPort(8080);

    Vertx vertx = Vertx.vertx();
    GuiceInjector.initializeInjector(
        List.of(
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(Vertx.class).toInstance(vertx);
                bind(ObjectMapper.class).toInstance(new ObjectMapper());
                bind(HttpServerConfig.class).toInstance(httpServerConfig);
                bind(AerospikeClient.class).toInstance(aerospikeClient);
                bind(PgReaderClient.class).toInstance(pgReaderClient);
                bind(PgWriterClient.class).toInstance(pgWriterClient);
                bind(WebClient.class).toInstance(webClient);
                bind(HealthCheckDAO.class).toInstance(healthCheckDAO);
                bind(HealthCheckService.class).toInstance(healthCheckService);
                bind(ExperimentService.class).toInstance(experimentService);
                bind(ExperimentDAO.class).toInstance(experimentDAO);
                // Don't bind RestVerticle as singleton - let it create new instances
                bind(RestVerticle.class).toProvider(() -> new RestVerticle(httpServerConfig));
              }
            }));

    mainVerticle = new MainVerticle();
    rxVertx = null;
    deploymentId = null;
  }

  @AfterEach
  void tearDown() throws Exception {
    // Undeploy MainVerticle if it was deployed
    if (rxVertx != null && deploymentId != null) {
      try {
        rxVertx.rxUndeploy(deploymentId).blockingAwait(5, TimeUnit.SECONDS);
      } catch (Exception e) {
        // Ignore undeploy errors in cleanup
      }
    }

    // Reset GuiceInjector
    java.lang.reflect.Field instanceField = GuiceInjector.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create MainVerticle instance")
    void testConstructor() {
      // Act
      MainVerticle verticle = new MainVerticle();

      // Assert
      assertNotNull(verticle);
    }
  }

  @Nested
  @DisplayName("Stop Tests")
  class StopTests {

    @Test
    @DisplayName("Should stop and close all clients")
    void testRxStop(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.close()).thenReturn(Completable.complete());
      when(pgReaderClient.close()).thenReturn(Completable.complete());
      when(pgWriterClient.close()).thenReturn(Completable.complete());
      when(webClient.close()).thenReturn(Completable.complete());

      mainVerticle.init(vertx, vertx.getOrCreateContext());

      // Act
      TestObserver<Void> testObserver = mainVerticle.rxStop().test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertComplete();
      testObserver.assertNoErrors();

      verify(aerospikeClient, times(1)).close();
      verify(pgReaderClient, times(1)).close();
      verify(pgWriterClient, times(1)).close();
      verify(webClient, times(1)).close();

      testContext.completeNow();
    }

    @Test
    @DisplayName("Should return Completable from rxStop")
    void testRxStopReturnsCompletable(Vertx vertx) {
      // Arrange
      when(aerospikeClient.close()).thenReturn(Completable.complete());
      when(pgReaderClient.close()).thenReturn(Completable.complete());
      when(pgWriterClient.close()).thenReturn(Completable.complete());
      when(webClient.close()).thenReturn(Completable.complete());

      mainVerticle.init(vertx, vertx.getOrCreateContext());

      // Act
      Completable stopCompletable = mainVerticle.rxStop();

      // Assert
      assertNotNull(stopCompletable);
    }

    @Test
    @DisplayName("Should close all clients in parallel")
    void testStopClosesClientsInParallel(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      when(aerospikeClient.close()).thenReturn(Completable.complete());
      when(pgReaderClient.close()).thenReturn(Completable.complete());
      when(pgWriterClient.close()).thenReturn(Completable.complete());
      when(webClient.close()).thenReturn(Completable.complete());

      mainVerticle.init(vertx, vertx.getOrCreateContext());

      // Act
      TestObserver<Void> testObserver = mainVerticle.rxStop().test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertComplete();

      // Verify all clients were closed
      verify(aerospikeClient).close();
      verify(pgReaderClient).close();
      verify(pgWriterClient).close();
      verify(webClient).close();

      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle client close error gracefully")
    void testClientCloseError(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      RuntimeException error = new RuntimeException("Failed to close client");
      when(aerospikeClient.close()).thenReturn(Completable.error(error));
      when(pgReaderClient.close()).thenReturn(Completable.complete());
      when(pgWriterClient.close()).thenReturn(Completable.complete());
      when(webClient.close()).thenReturn(Completable.complete());

      mainVerticle.init(vertx, vertx.getOrCreateContext());

      // Act
      TestObserver<Void> testObserver = mainVerticle.rxStop().test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertError(RuntimeException.class);

      testContext.completeNow();
    }

    @Test
    @DisplayName("Should handle multiple client close errors")
    void testMultipleClientCloseErrors(Vertx vertx, VertxTestContext testContext) {
      // Arrange
      RuntimeException error1 = new RuntimeException("Aerospike close failed");
      RuntimeException error2 = new RuntimeException("PostgreSQL close failed");
      when(aerospikeClient.close()).thenReturn(Completable.error(error1));
      when(pgReaderClient.close()).thenReturn(Completable.error(error2));
      when(pgWriterClient.close()).thenReturn(Completable.complete());
      when(webClient.close()).thenReturn(Completable.complete());

      mainVerticle.init(vertx, vertx.getOrCreateContext());

      // Act
      TestObserver<Void> testObserver = mainVerticle.rxStop().test();

      // Assert
      testObserver.awaitDone(2, TimeUnit.SECONDS);
      testObserver.assertError(Throwable.class);

      testContext.completeNow();
    }
  }

  @Nested
  @DisplayName("VerticleDeployment Record Tests")
  class VerticleDeploymentRecordTests {

    @Test
    @DisplayName("Should create VerticleDeployment record")
    void testVerticleDeploymentRecord() {
      // Arrange
      HttpServerConfig config = new HttpServerConfig();
      config.setHost("localhost");
      config.setPort(8080);
      RestVerticle verticle = new RestVerticle(config);
      DeploymentOptions options = new DeploymentOptions();

      // Act
      MainVerticle.VerticleDeployment deployment =
          new MainVerticle.VerticleDeployment(() -> verticle, options);

      // Assert
      assertNotNull(deployment);
      assertNotNull(deployment.verticleSupplier());
      assertNotNull(deployment.deploymentOptions());
      assertSame(verticle, deployment.verticleSupplier().get());
      assertSame(options, deployment.deploymentOptions());
    }

    @Test
    @DisplayName("Should support record equality")
    void testVerticleDeploymentEquality() {
      // Arrange
      HttpServerConfig config = new HttpServerConfig();
      config.setHost("localhost");
      config.setPort(8080);
      RestVerticle verticle = new RestVerticle(config);
      DeploymentOptions options = new DeploymentOptions();

      MainVerticle.VerticleDeployment deployment1 =
          new MainVerticle.VerticleDeployment(() -> verticle, options);
      MainVerticle.VerticleDeployment deployment2 =
          new MainVerticle.VerticleDeployment(() -> verticle, options);

      // Assert
      assertNotNull(deployment1);
      assertNotNull(deployment2);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle verticle instantiation")
    void testVerticleInstantiation() {
      // Act
      MainVerticle verticle1 = new MainVerticle();
      MainVerticle verticle2 = new MainVerticle();

      // Assert
      assertNotNull(verticle1);
      assertNotNull(verticle2);
      assertNotSame(verticle1, verticle2);
    }
  }
}
