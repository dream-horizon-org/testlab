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

@Slf4j
public class MainLauncher extends Launcher {

  public static void main(String[] args) {
    log.info("Starting Application..........");
    new MainLauncher().dispatch(new String[] {"run", "com.ascend.testlab.verticle.MainVerticle"});
  }

  @Override
  public void beforeStartingVertx(VertxOptions vertxOptions) {
    vertxOptions
        .setEventLoopPoolSize(CommonUtil.getNumberOfCores())
        .setPreferNativeTransport(true)
        .setMetricsOptions(new DropwizardMetricsOptions().setJmxEnabled(true));
  }

  @Override
  public void afterStartingVertx(Vertx vertx) {
    log.info("Initializing Guice Modules..........");
    initializeGuiceInjector(vertx);
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    log.info("Deploying Verticles..........");
    deploymentOptions.setInstances(1);
  }

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

  static void initializeGuiceInjector(Vertx vertx) {
    GuiceInjector.initializeInjector(getGuiceModules(vertx));
  }

  private static List<Module> getGuiceModules(Vertx vertx) {
    return List.of(new ServiceModule(io.vertx.rxjava3.core.Vertx.newInstance(vertx)));
  }
}
