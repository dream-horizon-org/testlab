package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.PartitionRequest;
import com.ascend.testlab.dto.response.PartitionResponse;
import io.reactivex.rxjava3.core.Single;

public interface PartitionService {
  Single<PartitionResponse> createProjectPartition(PartitionRequest request);
}
