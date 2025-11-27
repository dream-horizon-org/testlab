package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.ExperimentHistoryEntry;
import java.util.List;
import lombok.Builder;

/**
 * Response DTO for the Get Experiment History API containing the experiment id, history entries,
 * and pagination metadata.
 *
 * @param experimentId The unique identifier of the experiment.
 * @param history The list of history entries for the experiment.
 * @param pagination The pagination metadata.
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
public record ExperimentHistoryResponse(
    String experimentId, List<ExperimentHistoryEntry> history, PaginationMeta pagination) {}
