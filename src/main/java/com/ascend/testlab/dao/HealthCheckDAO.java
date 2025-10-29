package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;

public interface HealthCheckDAO {
  Single<Boolean> isMySQLReaderConnected();

  Single<Boolean> isAerospikeConnected();

  Single<Boolean> isUnderMaintenance();
}
