package com.ascend.testlab.dto.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import lombok.Builder;

/**
 * Record encapsulating the experiment history entry.
 *
 * @param updatedBy The user who updated the experiment.
 * @param previousData The previous data of the experiment.
 * @param currentData The current data of the experiment.
 * @param createdAt The timestamp when the entry was created.
 * @param updatedAt The timestamp when the entry was updated.
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ExperimentHistoryEntry(
    String updatedBy,
    JsonObject previousData,
    JsonObject currentData,
    Instant createdAt,
    Instant updatedAt) {}
