package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.UUID;

/**
 * Data Access Object interface for Owner operations.
 *
 * <p>Provides methods for creating and managing experiment owners in the database.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public interface OwnerDAO {

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param owner owner name/email
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> insertOwner(
      SqlConnection connection, UUID projectKey, UUID experimentId, String owner);

  /**
   * Deletes all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  Single<Boolean> deleteOwners(SqlConnection connection, UUID projectKey, UUID experimentId);
}
