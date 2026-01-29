package com.ascend.testlab;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.TestConstants;
import com.ascend.testlab.util.TestUtil;
import com.ascend.testlab.verticle.MainVerticle;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;

@Slf4j
public class Setup
    implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {

  private final Vertx vertx = Vertx.vertx();
  private static boolean IS_STARTED = Boolean.FALSE;
  private PostgreSQLContainer<?> postgreSQLContainer;
  private GenericContainer<?> aerospikeContainer;

  @Override
  public void afterAll(ExtensionContext context) {}

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!IS_STARTED) {
      IS_STARTED = Boolean.TRUE;
      System.setProperty(Constants.APP_ENV_KEY, TestConstants.TEST_APP_ENV);

      this.initializePostgreSQLContainer();
      this.initializeAerospikeContainer();

      context
          .getRoot()
          .getStore(ExtensionContext.Namespace.GLOBAL)
          .put(TestConstants.TEST_APP_ENV, this);

      this.startApplication();
      this.configureRestAssured();
      this.waitForApplicationReady();
    }
  }

  @Override
  public void close() {
    log.info("Closing all resources...");
    AsyncResultSingle.<Void>toSingle(this.vertx::close).subscribe();
    this.aerospikeContainer.close();
    this.postgreSQLContainer.close();
  }

  @SneakyThrows
  private void startApplication() {
    TestUtil.prepareDatabase();

    // Set the port for the HTTP server to match what tests expect
    System.setProperty("http.default.port", "8100");

    MainLauncher.initializeGuiceInjector(this.vertx);

    AsyncResultSingle.<String>toSingle(
            handler ->
                this.vertx.deployVerticle(
                    MainVerticle.class.getName(), new DeploymentOptions().setInstances(1), handler))
        .doOnSuccess(deploymentId -> log.info("Main Verticle deployed with id:{}", deploymentId))
        .doOnError(err -> log.error("Failed to deploy Main Verticle due to: ", err))
        .subscribe();
  }

  private void initializeAerospikeContainer() {
    String aerospikeImage =
        System.getProperty(
            TestConstants.AEROSPIKE_IMAGE_KEY, TestConstants.DEFAULT_AEROSPIKE_IMAGE);

    this.aerospikeContainer = new GenericContainer<>(aerospikeImage);
    log.info("Starting aerospike container from image:{}", aerospikeImage);
    this.aerospikeContainer
        .withEnv(TestConstants.NAMESPACE, TestConstants.AEROSPIKE_NAMESPACE)
        .withExposedPorts(3000)
        .withStartupTimeout(Duration.ofSeconds(10))
        .withStartupCheckStrategy(
            new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(10)))
        .start();

    TestUtil.setAerospikeSystemProperties(this.aerospikeContainer);
  }

  private void initializePostgreSQLContainer() {
    String postgreSQLImage =
        System.getProperty(TestConstants.POSTGRES_IMAGE_KEY, TestConstants.DEFAULT_POSTGRES_IMAGE);

    this.postgreSQLContainer = new PostgreSQLContainer<>(postgreSQLImage);
    log.info("Starting postgresql container from image:{}", postgreSQLImage);
    this.postgreSQLContainer
        .withDatabaseName(TestConstants.POSTGRES_DATABASE)
        .withUsername(TestConstants.POSTGRES_USER)
        .withPassword(TestConstants.POSTGRES_PASSWORD)
        .start();

    TestUtil.setPostgresSystemProperties(this.postgreSQLContainer);
  }

  private void configureRestAssured() {
    String host = System.getProperty(TestConstants.APPLICATION_HOST_KEY, "localhost");
    String port = System.getProperty(TestConstants.APPLICATION_PORT_KEY, "8100");
    RestAssured.baseURI = String.format("http://%s:%s", host, port);
    log.info("RestAssured configured with base URI: {}", RestAssured.baseURI);
  }

  private void waitForApplicationReady() {
    String host = System.getProperty(TestConstants.APPLICATION_HOST_KEY, "localhost");
    int port = Integer.parseInt(System.getProperty(TestConstants.APPLICATION_PORT_KEY, "8100"));

    log.info("Waiting for port {} to be ready...", port);

    await()
        .atMost(30, SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .ignoreExceptions()
        .until(() -> isPortOpen(host, port));

    log.info("Port {} is ready!", port);
  }

  private boolean isPortOpen(String host, int port) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), 1000);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
