package com.ascend.testlab.service;

import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.dto.response.AllocationResponse;
import com.ascend.testlab.dto.response.GetAllocationsResponse;
import io.reactivex.rxjava3.core.Single;

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
  Single<AllocationResponse> allotExperiments(
      String projectKey, AllocationRequest allocationRequest);

  /**
   * Retrieves all experiment allocations for a specific user within a project.
   *
   * @param userId the unique identifier of the user whose allocations are being retrieved
   * @param projectKey the project identifier to scope the allocations
   * @return a Single that emits the GetAllocationsResponse containing the user's experiment
   *     allocations
   */
  Single<GetAllocationsResponse> getAllocations(String userId, String projectKey);
}
