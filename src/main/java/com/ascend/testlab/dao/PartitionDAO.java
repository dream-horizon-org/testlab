package com.ascend.testlab.dao;

import com.ascend.testlab.dto.request.PartitionRequest;
import com.ascend.testlab.dto.response.PartitionResponse;
import io.reactivex.rxjava3.core.Single;

public interface PartitionDAO {

  Single<PartitionResponse> createProjectPartition(PartitionRequest request);
}
