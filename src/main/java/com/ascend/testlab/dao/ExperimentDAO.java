package com.ascend.testlab.dao;

import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Set;

public interface ExperimentDAO {
  Single<Experiment> getExperiment(String projectId, String experimentId);

  Single<List<Experiment>> fetchExperiments(String projectId, FilterExperimentsRequest req);

  Single<Set<String>> getExperimentIdsByTags(String projectId, List<String> tags);

  Single<Set<String>> getExperimentIdsByOwners(String projectId, List<String> owners);

  Single<List<Experiment>> fetchExperimentsByIds(
      String projectId, Set<String> experimentIds, FilterExperimentsRequest req);
}
