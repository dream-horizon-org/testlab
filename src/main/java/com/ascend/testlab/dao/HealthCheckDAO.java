package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;

public interface HealthCheckDAO {
  Single<Boolean> isPgReaderConnected();

  Single<Boolean> isAerospikeConnected();

  Single<Boolean> isUnderMaintenance();
}
