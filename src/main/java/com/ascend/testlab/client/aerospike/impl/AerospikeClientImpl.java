package com.ascend.testlab.client.aerospike.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.*;
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
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AerospikeClient interface.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see AerospikeClient
 */
@Slf4j
public class AerospikeClientImpl implements AerospikeClient {

  /** The Vertx instance. */
  private final Vertx vertx;

  /** The Aerospike connect options. */
  private final AerospikeConnectOptions aerospikeConnectOptions;

  /** The default read policy. */
  private final Policy defaultPolicy;

  /** The default write policy. */
  private final WritePolicy defaultWritePolicy;

  /** The Aerospike client. */
  private io.d11.aerospike.client.AerospikeClient aerospikeClient = null;

  /**
   * Constructor for the AerospikeClientImpl.
   *
   * @param vertx the Vertx instance
   * @param aerospikeConfig the Aerospike config
   */
  @Inject
  public AerospikeClientImpl(Vertx vertx, AerospikeConfig aerospikeConfig) {
    this.vertx = vertx;
    this.aerospikeConnectOptions = getAerospikeConnectOptions(aerospikeConfig);
    this.defaultPolicy = defaultPolicy();
    this.defaultWritePolicy = defaultWritePolicy();
    retryConnection(aerospikeConfig.getConnectRetryIntervalMS());
  }

  /**
   * Constructor for the AerospikeClientImpl.
   *
   * @param vertx the Vertx instance
   * @param aerospikeClient the already initialized {@link io.d11.aerospike.client.AerospikeClient}
   *     instance
   */
  public AerospikeClientImpl(Vertx vertx, io.d11.aerospike.client.AerospikeClient aerospikeClient) {
    this.vertx = vertx;
    this.aerospikeConnectOptions = new AerospikeConnectOptions();
    this.defaultPolicy = defaultPolicy();
    this.defaultWritePolicy = defaultWritePolicy();
    this.aerospikeClient = aerospikeClient;
  }

  /** {@inheritDoc} */
  @Override
  public Completable close() {
    if (aerospikeClient != null) return Completable.fromAction(() -> aerospikeClient.close());
    else return Completable.complete();
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isConnected() {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.isConnected(handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  /** {@inheritDoc} */
  @Override
  public Policy getDefaultPolicy() {
    return new Policy(defaultPolicy);
  }

  /** {@inheritDoc} */
  @Override
  public WritePolicy getDefaultWritePolicy() {
    return new WritePolicy(defaultWritePolicy);
  }

  /** {@inheritDoc} */
  @Override
  public Single<Record> get(Policy policy, Key key, String... binNames) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.get(policy, key, binNames, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  /** {@inheritDoc} */
  @Override
  public Single<List<Record>> get(BatchPolicy batchPolicy, Key[] keys, String... binNames) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.get(batchPolicy, keys, binNames, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  /** {@inheritDoc} */
  @Override
  public Single<Key> put(WritePolicy writePolicy, Key key, Bin... bins) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.put(writePolicy, key, bins, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  /** {@inheritDoc} */
  @Override
  public Single<Record> operate(WritePolicy writePolicy, Key key, Operation... operations) {
    return AsyncResultSingle.toSingle(
        handler ->
            getClient()
                .onSuccess(client -> client.operate(writePolicy, key, operations, handler))
                .onFailure(err -> handler.handle(Future.failedFuture(err))));
  }

  /** {@inheritDoc} */
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

  private static Policy defaultPolicy() {
    Policy policy = new Policy();
    policy.replica = Replica.MASTER_PROLES;
    policy.sendKey = true;
    return policy;
  }

  private static WritePolicy defaultWritePolicy() {
    WritePolicy writePolicy = new WritePolicy();
    writePolicy.replica = Replica.MASTER_PROLES;
    writePolicy.sendKey = true;
    writePolicy.commitLevel = CommitLevel.COMMIT_ALL;
    return writePolicy;
  }
}
