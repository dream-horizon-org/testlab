package com.ascend.testlab.dao;

import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.UUID;

/**
 * Interface for the assignment DAO. Contains methods to manage experiment assignments, user
 * assignments, variant counts, and locking mechanisms.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public interface AssignmentDAO {

  /**
   * Fetches all active experiments for a given tenant
   *
   * @param tenantId tenant identifier
   * @return list of active experiments
   */
  Single<List<Experiment>> fetchActiveExperiments(UUID tenantId);

  /**
   * Fetches user's current experiment assignments from Aerospike
   *
   * @param userId user identifier
   * @param tenantId tenant identifier
   * @return list of user experiment mappings
   */
  Single<List<UserExperimentMap>> getUserAssignments(String userId, UUID tenantId);

  /**
   * Inserts user experiment assignments in MySQL and Aerospike
   *
   * @param userId user identifier
   * @param tenantId tenant identifier
   * @param assignments list of assignments to insert
   * @return success status
   */
  Single<Boolean> insertUserAssignments(
      String userId, UUID tenantId, List<UserExperimentMap> assignments)
      throws JsonProcessingException;

  /**
   * Updates variant count in Aerospike for threshold checking
   *
   * @param experimentId experiment identifier
   * @param variantName variant name
   * @return updated count
   */
  Single<Long> incrementVariantCount(UUID tenantId, UUID experimentId, String variantName);

  /**
   * Checks if experiment has reached threshold
   *
   * @param experimentId experiment identifier
   * @return true if threshold not breached
   */
  Single<Boolean> checkThreshold(UUID tenantId, Experiment experimentId);

  /**
   * Acquires lock for user assignment in Aerospike
   *
   * @param userId user identifier
   * @param tenantId tenant identifier
   * @return true if lock acquired
   */
  Single<Boolean> acquireUserLock(String userId, UUID tenantId);

  /**
   * Releases lock for user assignment in Aerospike
   *
   * @param userId user identifier
   * @param tenantId tenant identifier
   * @return true if lock released
   */
  Single<Boolean> releaseUserLock(String userId, UUID tenantId);

  /**
   * Fetches concluded experiments with winning variant for a tenant
   *
   * @param tenantId tenant identifier
   * @return list of concluded experiments
   */
  Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId);

  //  /**
  //   * Updates an existing user experiment assignment with a new variant
  //   *
  //   * @param userId user identifier
  //   * @param tenantId tenant identifier
  //   * @param assignment the updated assignment
  //   * @return success status
  //   */
  //  Single<Boolean> updateUserAssignment(String userId, UUID tenantId, UserExperimentMap
  // assignment);

  /**
   * Decrements variant count in Aerospike (used when reassigning)
   *
   * @param experimentId experiment identifier
   * @param variantName variant name
   * @return updated count
   */
  Single<Long> decrementVariantCount(UUID tenantId, UUID experimentId, String variantName);
}
