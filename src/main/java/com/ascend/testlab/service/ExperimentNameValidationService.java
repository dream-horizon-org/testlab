package com.ascend.testlab.service;

import com.ascend.testlab.dto.response.ValidateExperimentNameResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public interface ExperimentNameValidationService {
  Single<ValidateExperimentNameResponse> validateExperimentName(UUID projectId, String name);
}
