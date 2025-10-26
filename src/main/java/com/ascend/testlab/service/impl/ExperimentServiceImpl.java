package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  @Inject private ExperimentDAO experimentDAO;

  @Override
  public Single<Experiment> getExperiment(String projectId, String experimentId) {
    return experimentDAO
        .getExperiment(projectId, experimentId);
  }

  @Override
  public Single<List<Experiment>> filterExperiments(
      String projectId, FilterExperimentsRequest request) {
    return experimentDAO.filterExperiments(projectId, request);
  }
}
