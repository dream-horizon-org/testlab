package com.ascend.testlab.dto.entity;

import io.vertx.core.json.JsonObject;
import java.time.Instant;
import lombok.Builder;

/**
 * Record encapsulating the experiment history entry.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
public record ExperimentHistoryEntry(
    String updatedBy,
    JsonObject previousData,
    JsonObject currentData,
    Instant createdAt,
    Instant updatedAt) {}
