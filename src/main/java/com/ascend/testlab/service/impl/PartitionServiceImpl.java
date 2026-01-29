package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.PartitionDAO;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.ascend.testlab.service.PartitionService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementation of the PartitionService interface.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 * @see PartitionService
 */
public class PartitionServiceImpl implements PartitionService {

  /** The partition DAO. */
  private final PartitionDAO partitionDAO;

  /**
   * Constructor for the PartitionServiceImpl.
   *
   * @param partitionDAO the partition DAO
   */
  @Inject
  public PartitionServiceImpl(PartitionDAO partitionDAO) {
    this.partitionDAO = partitionDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<PartitionResponse> createProjectPartition(String projectKey, String userId) {
    return partitionDAO.createProjectPartition(projectKey, userId);
  }
}
