package com.ascend.testlab.service;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.dto.response.AllocationsResponse;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Interface for the allocation service. Contains methods to assign and reassign experiments to
 * users based on various filters and strategies.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public interface AllocationService {

  /**
   * Assigns experiments to a user based on the provided request parameters.
   *
   * @param projectKey the tenant identifier
   * @param allocationRequest the allocation request containing filters and attributes
   * @return a Single that emits the allocation response
   */
  Single<AllocationsResponse> allotExperiments(
      String projectKey, AllocationRequest allocationRequest);

  /**
   * Retrieves all experiment allocations for a specific user within a project.
   *
   * @param userId the unique identifier of the user whose allocations are being retrieved
   * @param projectKey the project identifier to scope the allocations
   * @return a Single that emits the AllocationsResponse containing the user's experiment
   *     allocations
   */
  Single<AllocationsResponse> getAllocations(String userId, String projectKey);

  /**
   * Reassigns an experiment allocation for a user within the given project.
   *
   * @param projectKey the project identifier to scope the reallocation
   * @param reallocateRequest the reallocation request containing user, experiment and any
   *     reallocation parameters
   * @return a Single that emits a UserExperimentMap representing the updated mapping after
   *     reallocation
   */
  Single<UserExperimentMap> reallocateExperiment(
      String projectKey, ReallocateRequest reallocateRequest);

  /**
   * Applies user overrides for an experiment. For each user specified in the overrides, either
   * creates a new allocation or reallocates them to the specified variant.
   *
   * <p>This method processes the variant-to-userIds map where key is variant name and value is list
   * of user IDs. For users without existing allocations, new allocations are created. For users
   * with existing allocations, they are reallocated to the override variant.
   *
   * @param projectKey the project identifier to scope the allocations
   * @param experiment the experiment entity containing variants and configuration
   * @param overrides the overrides containing variant to user IDs mapping
   * @return a Single that emits a list of UserExperimentMap representing all applied overrides
   */
  Single<List<UserExperimentMap>> applyOverrides(
      String projectKey, Experiment experiment, Overrides overrides);
}
