package com.ascend.testlab.dto.response;

import java.util.List;
import lombok.Builder;

/**
 * Response DTO for the Get Experiment History API containing the experiment id, history entries,
 * and total count.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
public record GetExperimentHistoryResponse(
    String experimentId, List<ExperimentHistoryEntry> history, Integer totalCount) {}
