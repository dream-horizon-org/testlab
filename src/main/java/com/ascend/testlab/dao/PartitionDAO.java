package com.ascend.testlab.dao;

import com.ascend.testlab.dto.response.PartitionResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Interface for the partition DAO. Contains methods to create project partitions.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 */
public interface PartitionDAO {
  /**
   * Transactionally creates PostgreSQL list partitions for all partitioned tables and updates
   * partition metadata. Handles partition creation for experiments, owners, tags,
   * experiment_update_log, and experiment_analysis tables.
   *
   * @param projectKey the project key
   * @param userId the user id (optional)
   * @return a Single that emits the partition response with status and message
   */
  Single<PartitionResponse> createProjectPartition(String projectKey, String userId);
}
