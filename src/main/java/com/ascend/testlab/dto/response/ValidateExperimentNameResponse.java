package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidateExperimentNameResponse(
    @JsonProperty("is_available") Boolean isAvailable, @JsonProperty("message") String message) {}
