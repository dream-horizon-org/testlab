package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object interface for ExperimentUpdateLog operations.
 *
 * <p>Provides methods for creating and managing experiment update logs in the database.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface ExperimentUpdateLogDAO {

  /**
   * Inserts an experiment update log entry.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param previousData previous experiment data (null for create operation)
   * @param currentData current experiment data
   * @param updatedBy user who performed the update
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertUpdateLog(
      SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      Map<String, Object> currentData,
      String updatedBy);
}
