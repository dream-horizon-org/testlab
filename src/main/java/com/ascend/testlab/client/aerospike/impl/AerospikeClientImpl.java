package com.ascend.testlab.client.aerospike.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.util.VertxUtil;
import com.google.inject.Inject;
import io.d11.aerospike.client.AerospikeConnectOptions;
import io.d11.aerospike.util.SharedDataUtils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AerospikeClientImpl implements AerospikeClient {
  private final Vertx vertx;
  private final AerospikeConnectOptions aerospikeConnectOptions;
  private io.d11.aerospike.client.AerospikeClient aerospikeClient = null;

  @Inject
  public AerospikeClientImpl(Vertx vertx, AerospikeConfig aerospikeConfig) {
    this.vertx = vertx;
    this.aerospikeConnectOptions = getAerospikeConnectOptions(aerospikeConfig);

    retryConnection(aerospikeConfig.getConnectRetryIntervalMS());
  }

  @Override
  public Completable close() {
    if (aerospikeClient != null) return Completable.fromAction(() -> aerospikeClient.close());
    else return Completable.complete();
  }

  @Override
  public Single<Boolean> isConnected() {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.isConnected(handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  @Override
  public Single<Record> get(Policy policy, Key key, String... binNames) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.get(policy, key, binNames, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  @Override
  public Single<Key> put(WritePolicy writePolicy, Key key, Bin... bins) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.put(writePolicy, key, bins, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  @Override
  public Single<Record> operate(WritePolicy writePolicy, Key key, Operation... operations) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.operate(writePolicy, key, operations, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  @Override
  public Single<Boolean> delete(WritePolicy writePolicy, Key key) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.delete(writePolicy, key, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  private Future<io.d11.aerospike.client.AerospikeClient> getClient() {
    if (aerospikeClient != null) return Future.succeededFuture(aerospikeClient);
    else return Future.failedFuture("AerospikeClient is not initialized");
  }

  private void initializeAerospikeClient() {
    aerospikeClient =
        VertxUtil.getOrCreateSharedData(
            vertx.getDelegate(),
            SharedDataUtils.getInstanceName(
                aerospikeConnectOptions.getHost(), aerospikeConnectOptions.getPort()),
            () ->
                new io.d11.aerospike.client.impl.AerospikeClientImpl(
                    vertx.getDelegate(), aerospikeConnectOptions));
  }

  private void retryConnection(long connectRetryIntervalMS) {
    vertx.setPeriodic(
        0,
        connectRetryIntervalMS,
        event ->
            vertx.executeBlocking(
                () -> {
                  try {
                    if (aerospikeClient == null) {
                      log.info("Initializing AerospikeClient");
                      initializeAerospikeClient();
                    }
                    return Future.succeededFuture();
                  } catch (Exception e) {
                    log.warn("Unable to initialize AerospikeClient", e);
                    return Future.failedFuture(e);
                  }
                }));
  }

  private static AerospikeConnectOptions getAerospikeConnectOptions(
      AerospikeConfig aerospikeConfig) {

    return new AerospikeConnectOptions()
        .setHost(aerospikeConfig.getHost())
        .setPort(aerospikeConfig.getPort())
        .setMaxConnsPerNode(aerospikeConfig.getMaxConnsPerNode())
        .setMaxCommandsInProcess(aerospikeConfig.getMaxCommandsInProcess())
        .setMaxCommandsInQueue(aerospikeConfig.getMaxCommandsInQueue())
        .setMaxConnectRetries(aerospikeConfig.getMaxRetries())
        .setEventLoopSize(aerospikeConfig.getEventLoopSize())
        .updateClientPolicy();
  }
}
