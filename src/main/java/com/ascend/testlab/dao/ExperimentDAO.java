package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

public interface ExperimentDAO {
  Single<Experiment> getExperiment(String projectId, String experimentId);

  Single<List<Experiment>> filterExperiments(
      String projectId, FilterExperimentsRequest filterExperimentRequest);
}
