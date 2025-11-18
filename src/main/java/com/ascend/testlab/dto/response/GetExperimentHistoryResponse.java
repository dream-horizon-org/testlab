package com.ascend.testlab.dto.response;

import java.util.List;
import lombok.Builder;

/**
 * Response DTO for the Get Experiment History API containing the experiment id, history entries,
 * and pagination metadata.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Builder
public record GetExperimentHistoryResponse(
    String experimentId, List<ExperimentHistoryEntry> history, PaginationMeta pagination) {

  /** Metadata class for pagination information. */
  @Builder
  public record PaginationMeta(int currentPage, int pageSize, int totalCount) {}
}
