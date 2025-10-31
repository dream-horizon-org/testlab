package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.HealthCheckDAO;
import com.ascend.testlab.dto.response.HealthCheckResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.HealthCheckService;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementation of the HealthCheckService interface.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see HealthCheckService
 */
public class HealthCheckServiceImpl implements HealthCheckService {

  /** The health check DAO. */
  private final HealthCheckDAO healthCheckDAO;

  /**
   * Constructor for the HealthCheckServiceImpl.
   *
   * @param healthCheckDAO the health check DAO
   */
  @Inject
  public HealthCheckServiceImpl(HealthCheckDAO healthCheckDAO) {
    this.healthCheckDAO = healthCheckDAO;
  }

  /** {@inheritDoc} */
  @Override
  public Single<HealthCheckResponse> healthCheck() {
    return Single.zip(
        healthCheckDAO.isPgReaderConnected(),
        healthCheckDAO.isAerospikeConnected(),
        healthCheckDAO.isUnderMaintenance(),
        (isPgReaderUp, isAerospikeUp, isUnderMaintenance) -> {
          if (!isPgReaderUp && !isAerospikeUp)
            throw ExceptionUtil.getException(ErrorEnum.REST_HEALTH_CHECK_FAILED);
          else return new HealthCheckResponse(isPgReaderUp, isAerospikeUp, isUnderMaintenance);
        });
  }
}
