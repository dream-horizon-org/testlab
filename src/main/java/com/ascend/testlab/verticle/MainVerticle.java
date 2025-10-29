package com.ascend.testlab.verticle;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
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

@Slf4j
public class MainVerticle extends AbstractVerticle {

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

  @Override
  public Completable rxStop() {
    return stopClients();
  }

  private List<VerticleDeployment> getVerticleDeployments() {
    return List.of(
        new VerticleDeployment(
            () -> GuiceInjector.getInstance(RestVerticle.class),
            new DeploymentOptions()
                .setInstances(
                    Math.min(CommonUtil.getNumberOfCores(), Constants.MAX_NUM_REST_VERTICLES))
                .setWorkerPoolSize(40)));
  }

  record VerticleDeployment(
      Supplier<Verticle> verticleSupplier, DeploymentOptions deploymentOptions) {}

  private Completable stopClients() {
    AerospikeClient aerospikeClient = GuiceInjector.getInstance(AerospikeClient.class);
    PgReaderClient pgReaderClient = GuiceInjector.getInstance(PgReaderClient.class);
    PgWriterClient pgWriterClient = GuiceInjector.getInstance(PgWriterClient.class);
    WebClient webClient = GuiceInjector.getInstance(WebClient.class);

    return Completable.mergeArray(
        aerospikeClient.close(), pgReaderClient.close(), pgWriterClient.close(), webClient.close());
  }
}
