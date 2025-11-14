package com.ascend.testlab.dto.response;

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
    String updatedBy, Object previousData, Object currentData, Long createdAt, Long updatedAt) {}
