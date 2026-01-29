package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.PartitionResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Interface for the partition service. Contains methods to create project partitions.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 * @see com.ascend.testlab.dao.PartitionDAO
 */
public interface PartitionService {

  /**
   * Creates PostgreSQL list partitions for all partitioned experiment tables for a given project
   * key.
   *
   * <p>Creates partitions for the following tables: experiments, owners, tags,
   * experiment_update_log, and experiment_analysis. The operation is idempotent - if partitions
   * already exist for the project key, returns success without creating duplicates. Updates
   * partition metadata to track creation status.
   *
   * @param projectKey the project key from the header
   * @param userId the user id from the header
   * @return a Single that emits the partition response with creation status and message
   */
  Single<PartitionResponse> createProjectPartition(String projectKey, String userId);
}
