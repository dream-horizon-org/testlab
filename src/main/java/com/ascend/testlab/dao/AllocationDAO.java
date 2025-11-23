package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;

/**
 * Interface for the allocation DAO. Contains methods to manage experiment allocations, user
 * allocations, variant counts, and locking mechanisms.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public interface AllocationDAO {

  /**
   * Fetches all active experiments for a given tenant
   *
   * @param projectKey project identifier
   * @return list of active experiments
   */
  Single<List<Experiment>> fetchActiveExperiments(String projectKey, List<String> experimentKeys);

  /**
   * Fetches user's current experiment allocations from Aerospike
   *
   * @param userId user identifier
   * @param projectKey project identifier
   * @return list of user experiment mappings
   */
  Single<List<UserExperimentMap>> getAllocations(String userId, String projectKey);

  /**
   * Fetches user's current experiment allocations from Aerospike
   *
   * @param userIds user identifier list
   * @param projectKey project identifier
   * @return list of user experiment mappings
   */
  Single<Map<String, List<UserExperimentMap>>> getAllocations(
      List<String> userIds, String projectKey);

  /**
   * Checks if experiment has reached threshold
   *
   * @param experimentId experiment identifier
   * @return true if threshold not breached
   */
  Single<Boolean> checkThreshold(String projectKey, Experiment experimentId);

  /**
   * Acquires lock for user allocation in Aerospike
   *
   * @param userId user identifier
   * @param projectKey tenant identifier
   * @return true if lock acquired
   */
  Single<Boolean> acquireUserLock(String userId, String projectKey);

  /**
   * Releases lock for user allocation in Aerospike
   *
   * @param userId user identifier
   * @param projectKey tenant identifier
   * @return true if lock released
   */
  Single<Boolean> releaseUserLock(String userId, String projectKey);

  /**
   * Fetches concluded experiments with winning variant for a tenant
   *
   * @param projectKey tenant identifier
   * @return list of concluded experiments
   */
  Single<List<Experiment>> fetchConcludedExperiments(String projectKey);

  /**
   * Transactionally inserts user allocations and increments variant counts. If allocation insertion
   * fails, variant counts are rolled back.
   *
   * @param userId user identifier
   * @param projectKey project identifier
   * @param allocations list of allocations to insert
   * @param variantCountMap map of experimentId:variantName -> count to increment
   * @return success status
   */
  Single<Boolean> insertAllocationsAndIncrementCounts(
      String userId,
      String projectKey,
      List<UserExperimentMap> allocations,
      Map<String, String> variantCountMap);
}
