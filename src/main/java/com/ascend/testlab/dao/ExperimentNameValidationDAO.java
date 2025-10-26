package com.ascend.testlab.dao;

import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public interface ExperimentNameValidationDAO {
  Single<Boolean> isExperimentNameExists(UUID projectId, String name);
}
