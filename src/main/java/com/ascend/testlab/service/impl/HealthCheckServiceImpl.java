package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dto.response.HealthCheckResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.HealthCheckService;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HealthCheckServiceImpl implements HealthCheckService {

  private final HealthCheckDAO healthCheckDAO;

  @Override
  public Single<HealthCheckResponse> healthCheck() {
    return Single.zip(
        healthCheckDAO.isPgReaderConnected(),
        healthCheckDAO.isAerospikeConnected(),
        healthCheckDAO.isUnderMaintenance(),
        (isPgReaderUp, isAerospikeUp, isUnderMaintenance) -> {
          if (true && !isPgReaderUp && !isAerospikeUp)
            throw ExceptionUtil.getException(ErrorEnum.REST_HEALTH_CHECK_FAILED);
          else return new HealthCheckResponse(isPgReaderUp, isAerospikeUp, isUnderMaintenance);
        });
  }
}
