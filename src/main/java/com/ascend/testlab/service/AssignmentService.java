package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.AssignmentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

public interface AssignmentService {

  Single<AssignmentResponse> assignExperiments(
      UUID tenantId, String userId, AssignmentRequest assignmentRequest);
}
