package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.util.MaintenanceUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the HealthCheckDAO interface.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see HealthCheckDAO
 */
@Slf4j
public class HealthCheckDAOImpl implements HealthCheckDAO {

  /** The Aerospike client. */
  private final AerospikeClient aerospikeClient;

  /** The PostgreSQL reader client. */
  private final PgReaderClient pgReaderClient;

  /**
   * Constructor for the HealthCheckDAOImpl.
   *
   * @param aerospikeClient the Aerospike client
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public HealthCheckDAOImpl(AerospikeClient aerospikeClient, PgReaderClient pgReaderClient) {
    this.aerospikeClient = aerospikeClient;
    this.pgReaderClient = pgReaderClient;
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isPgReaderConnected() {
    return pgReaderClient
        .isConnected()
        .onErrorReturn(
            err -> {
              log.warn("Error in connecting to Postgres-Reader: {}", err.getMessage());
              return false;
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isAerospikeConnected() {
    return aerospikeClient
        .isConnected()
        .onErrorReturn(
            err -> {
              log.warn("Error in connecting to Aerospike: {}", err.getMessage());
              return false;
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isUnderMaintenance() {
    return Single.just(MaintenanceUtil.isUnderMaintenance(Vertx.currentContext().owner()).get());
  }
}
