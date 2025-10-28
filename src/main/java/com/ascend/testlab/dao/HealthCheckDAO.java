package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;

/**
 * Interface for the health check DAO. Contains methods to check the health of the MySQL reader,
 * Aerospike and maintenance status.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public interface HealthCheckDAO {

  /**
   * Check if the MySQL reader is connected.
   *
   * @return a Single that emits true if the MySQL reader is connected, false otherwise
   */
  Single<Boolean> isMySQLReaderConnected();

  /**
   * Check if the Aerospike is connected.
   *
   * @return a Single that emits true if the Aerospike is connected, false otherwise
   */
  Single<Boolean> isAerospikeConnected();

  /**
   * Check if the service is under maintenance.
   *
   * @return a Single that emits true if the service is under maintenance, false otherwise
   */
  Single<Boolean> isUnderMaintenance();
}
