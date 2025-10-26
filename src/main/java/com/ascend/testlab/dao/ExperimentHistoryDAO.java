package com.ascend.testlab.dao;

import com.ascend.testlab.dto.response.ExperimentHistoryEntry;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.UUID;

public interface ExperimentHistoryDAO {
  Single<List<ExperimentHistoryEntry>> fetchExperimentHistory(UUID projectId, UUID experimentId);
}
