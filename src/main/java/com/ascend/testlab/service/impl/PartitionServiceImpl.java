package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.PartitionDAO;
import com.ascend.testlab.dto.request.PartitionRequest;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.ascend.testlab.service.PartitionService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;

public class PartitionServiceImpl implements PartitionService {

  private final PartitionDAO partitionDAO;

  @Inject
  public PartitionServiceImpl(PartitionDAO partitionDAO) {
    this.partitionDAO = partitionDAO;
  }

  @Override
  public Single<PartitionResponse> createProjectPartition(PartitionRequest request) {
    return partitionDAO.createProjectPartition(request);
  }
}
