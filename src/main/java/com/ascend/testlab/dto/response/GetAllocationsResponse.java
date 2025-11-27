package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import java.util.List;

/**
 * Response DTO for get allocations API.
 *
 * @param experimentMap the list of user experiment maps
 */
public record GetAllocationsResponse(List<UserExperimentMap> experimentMap) {}
