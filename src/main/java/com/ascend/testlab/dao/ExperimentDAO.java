package com.ascend.testlab.dao;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.rest.FilterExperiments;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Set;

public interface ExperimentDAO {
  Single<Experiment> getExperiment(String projectId, String experimentId);

  Single<List<Experiment>> fetchExperiments(String projectId, FilterExperimentsRequest req);

  // Fetch experiment IDs by tags
  Single<Set<String>> getExperimentIdsByTags(String projectId, List<String> tags);

  // Fetch experiment IDs by owners
  Single<Set<String>> getExperimentIdsByOwners(String projectId, List<String> owners);

  // Fetch experiments filtered by IDs (for tag/owner filtering), status, type, and name
  Single<List<Experiment>> fetchExperimentsByIds(String projectId, Set<String> experimentIds,
     FilterExperimentsRequest req);
}
