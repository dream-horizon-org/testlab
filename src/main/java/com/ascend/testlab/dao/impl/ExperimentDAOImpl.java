package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentDAOImpl implements ExperimentDAO {

  private final MySQLWriterClient mySQLWriterClient;

  private static final String INSERT_SQL =
      "INSERT INTO experiments (tenant_id, name, description, metrics, assignment_domain, exposure, threshold, type, end_date, tags) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  @Override
  public Single<Long> create(String tenantId, CreateExperimentRequest request) {
    return Single.just(1l);
  }
}
