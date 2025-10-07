package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.mysql.MySQLReaderClient;
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
      private final MySQLReaderClient mySQLReaderClient;
      
    @Override
  public Single<Boolean> isMySQLReaderConnected() {
    return mySQLReaderClient
        .isConnected()
        .onErrorReturn(
            err -> {
              log.warn("Error in connecting to MySQL-Reader: {}", err.getMessage());
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
