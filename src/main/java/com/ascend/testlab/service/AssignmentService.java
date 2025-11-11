package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.dto.response.AssignmentResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;

/**
 * Interface for the assignment service. Contains methods to assign and reassign experiments to
 * users based on various filters and strategies.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public interface AssignmentService {

  /**
   * Assigns experiments to a user based on the provided request parameters.
   *
   * @param tenantId the tenant identifier
   * @param assignmentRequest the assignment request containing filters and attributes
   * @return a Single that emits the assignment response
   */
  Single<AssignmentResponse> assignExperiments(UUID tenantId, AssignmentRequest assignmentRequest);

  //  /**
  //   * Reassigns a user to a different variant for an already assigned experiment. Can specify a
  //   * particular variant or let the system choose based on distribution strategy.
  //   *
  //   * @param tenantId the tenant identifier
  //   * @param reassignmentRequest the reassignment request containing experiment and variant
  // details
  //   * @return a Single that emits the updated user experiment mapping
  //   */
  //  Single<UserExperimentMap> reassignExperiment(
  //      UUID tenantId, ReassignmentRequest reassignmentRequest);
}
