package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GetAllocationsResponse(
    @JsonProperty("experiment_map") List<UserExperimentMap> experimentMap) {}
