package com.ascend.testlab;

import com.ascend.testlab.injection.GuiceInjector;
import com.ascend.testlab.injection.module.ServiceModule;
import com.ascend.testlab.util.CommonUtil;
import com.google.inject.Module;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Main launcher for the testlab application. This is the entry point of the application. Contains
 * methods to initialize the Guice injector and deploy the verticles.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see Launcher
 */
@Slf4j
public class MainLauncher extends Launcher {

  /**
   * Main method for the testlab application.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    log.info("Starting Application..........");
    new MainLauncher().dispatch(new String[] {"run", "com.ascend.testlab.verticle.MainVerticle"});
  }

  /** {@inheritDoc} */
  @Override
  public void beforeStartingVertx(VertxOptions vertxOptions) {
    vertxOptions
        .setEventLoopPoolSize(CommonUtil.getNumberOfCores())
        .setPreferNativeTransport(true)
        .setMetricsOptions(new DropwizardMetricsOptions().setJmxEnabled(true));
  }

  /** {@inheritDoc} */
  @Override
  public void afterStartingVertx(Vertx vertx) {
    log.info("Initializing Guice Modules..........");
    initializeGuiceInjector(vertx);
  }

  /** {@inheritDoc} */
  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    log.info("Deploying Verticles..........");
    deploymentOptions.setInstances(1);
  }

  /** {@inheritDoc} */
  @Override
  public void handleDeployFailed(
      Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
    log.error(
        "Deployment of {} verticle failed with options {} due to error: ",
        mainVerticle,
        deploymentOptions,
        cause);
    super.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
  }

  /**
   * Initialize the Guice injector.
   *
   * @param vertx the Vertx instance
   */
  static void initializeGuiceInjector(Vertx vertx) {
    GuiceInjector.initializeInjector(getGuiceModules(vertx));
  }

  /**
   * Get the Guice modules.
   *
   * @param vertx the Vertx instance
   * @return the Guice modules
   */
  private static List<Module> getGuiceModules(Vertx vertx) {
    return List.of(new ServiceModule(io.vertx.rxjava3.core.Vertx.newInstance(vertx)));
  }
}
