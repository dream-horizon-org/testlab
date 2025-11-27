package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * Response DTO for get allocations API.
 *
 * @param experimentMap the list of user experiment maps
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AllocationsResponse(List<UserExperimentMap> experimentMap) {}
