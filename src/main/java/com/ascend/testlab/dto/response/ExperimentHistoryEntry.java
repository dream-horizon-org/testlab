package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ExperimentHistoryEntry(
    @JsonProperty("updated_by") String updatedBy,
    @JsonProperty("previous_data") String previousData,
    @JsonProperty("current_data") String currentData,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("updated_at") LocalDateTime updatedAt) {}
