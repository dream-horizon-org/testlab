package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.util.MaintenanceUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HealthCheckDAOImpl implements HealthCheckDAO {

  private final AerospikeClient aerospikeClient;
  private final PgReaderClient pgReaderClient;

  @Override
  public Single<Boolean> isPgReaderConnected() {
    return pgReaderClient
        .isConnected()
        .onErrorReturn(
            err -> {
              log.warn("Error in connecting to Pg-Reader: {}", err.getMessage());
              return false;
            });
  }

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

  @Override
  public Single<Boolean> isUnderMaintenance() {
    return Single.just(MaintenanceUtil.isUnderMaintenance(Vertx.currentContext().owner()).get());
  }
}
