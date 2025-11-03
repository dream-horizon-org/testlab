package com.ascend.testlab.dao;

import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.UUID;

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
      String userId, UUID tenantId, List<UserExperimentMap> assignments);

  /**
   * Updates variant count in Aerospike for threshold checking
   *
   * @param experimentId experiment identifier
   * @param variantName variant name
   * @return updated count
   */
  Single<Long> incrementVariantCount(UUID experimentId, String variantName);

  /**
   * Checks if experiment has reached threshold
   *
   * @param experimentId experiment identifier
   * @return true if threshold not breached
   */
  Single<Boolean> checkThreshold(Experiment experimentId);

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
   * Fetches concluded experiments with winning variant
   *
   * @param tenantId tenant identifier
   * @param apiPath API path filter
   * @param entities entity filter
   * @return list of concluded experiments
   */
  Single<List<UserExperimentMap>> fetchConcludedExperiments(
      UUID tenantId, String apiPath, List<String> entities);

  //  Single<List<UserExperimentMap>> getGuestAssignments(String guestId, UUID tenantId);
}
