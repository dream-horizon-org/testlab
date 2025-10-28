package com.ascend.testlab.service;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import io.reactivex.rxjava3.core.Single;

public interface ExperimentService {
  Single<Experiment> getExperiment(String projectId, String experimentId);

  Single<FilterExperimentsResponse> filterExperiments(String projectId, FilterExperimentsRequest request);
}
