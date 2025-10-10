package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentServiceImpl implements ExperimentService {

  private final ExperimentDAO experimentDAO;

  @Override
  public Single<CreateExperimentResponse> create(String tenantId, CreateExperimentRequest request) {
    return experimentDAO.create(tenantId, request).map(CreateExperimentResponse::new);
  }
}
