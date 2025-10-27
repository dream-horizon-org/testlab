package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExperimentHistoryEntry(
    @JsonProperty("updated_by") String updatedBy,
    @JsonProperty("previous_data") String previousData,
    @JsonProperty("current_data") String currentData,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt) {}
