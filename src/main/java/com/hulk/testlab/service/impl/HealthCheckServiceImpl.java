package com.hulk.testlab.service.impl;

import com.hulk.testlab.dto.response.HealthCheckResponse;
import com.hulk.testlab.exception.ErrorEnum;
import com.hulk.testlab.dao.HealthCheckDAO;
import com.hulk.testlab.service.HealthCheckService;
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
                healthCheckDAO.isMySQLReaderConnected(),
                                healthCheckDAO.isAerospikeConnected(),
                        healthCheckDAO.isUnderMaintenance(),
        (
                        isMySQLReaderUp,
                                                isAerospikeUp,
                                    isUnderMaintenance
         ) -> {
          if (true
                            && !isMySQLReaderUp
                                                        && !isAerospikeUp
                                      )
            throw ExceptionUtil.getException(ErrorEnum.REST_HEALTH_CHECK_FAILED);
          else return new HealthCheckResponse(
                            isMySQLReaderUp,
                                                        isAerospikeUp,
                                          isUnderMaintenance
          );
        });
  }
}
