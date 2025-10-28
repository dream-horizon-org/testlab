package com.ascend.testlab.verticle;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.client.webclient.WebClient;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.injection.GuiceInjector;
import com.ascend.testlab.util.CommonUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.rxjava3.core.AbstractVerticle;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Main verticle for the testlab application. Contains methods to deploy the verticles and stop the
 * clients.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see AbstractVerticle
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {

  /** {@inheritDoc} */
  @Override
  public Completable rxStart() {
    return Observable.fromIterable(this.getVerticleDeployments())
        .flatMapSingle(
            verticleDeployment ->
                vertx.rxDeployVerticle(
                    verticleDeployment.verticleSupplier(), verticleDeployment.deploymentOptions()))
        .ignoreElements()
        .doOnError(err -> log.error("Failed to deploy verticles due to error: ", err))
        .doOnComplete(() -> log.info("Deployed all verticles. Started Application.........."));
  }

  /** {@inheritDoc} */
  @Override
  public Completable rxStop() {
    return stopClients();
  }

  /**
   * Get the verticle deployments.
   *
   * @return the verticle deployments
   */
  private List<VerticleDeployment> getVerticleDeployments() {
    return List.of(
        new VerticleDeployment(
            () -> GuiceInjector.getInstance(RestVerticle.class),
            new DeploymentOptions()
                .setInstances(
                    Math.min(CommonUtil.getNumberOfCores(), Constants.MAX_NUM_REST_VERTICLES))
                .setWorkerPoolSize(40)));
  }

  /**
   * Record describing the verticle deployment.
   *
   * @param verticleSupplier the verticle supplier
   * @param deploymentOptions the deployment options
   */
  record VerticleDeployment(
      Supplier<Verticle> verticleSupplier, DeploymentOptions deploymentOptions) {}

  /**
   * Stop the clients.
   *
   * @return a Completable that completes when the clients are stopped
   */
  private Completable stopClients() {
    AerospikeClient aerospikeClient = GuiceInjector.getInstance(AerospikeClient.class);
    MySQLReaderClient mySQLReaderClient = GuiceInjector.getInstance(MySQLReaderClient.class);
    MySQLWriterClient mySQLWriterClient = GuiceInjector.getInstance(MySQLWriterClient.class);
    WebClient webClient = GuiceInjector.getInstance(WebClient.class);

    return Completable.mergeArray(
        aerospikeClient.close(),
        mySQLReaderClient.close(),
        mySQLWriterClient.close(),
        webClient.close());
  }
}
