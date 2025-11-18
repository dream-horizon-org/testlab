package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.Experiment;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for filtered experiments with pagination metadata.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterExperimentsResponse {

  private List<Experiment> experiments;
  private PaginationMeta pagination;

  /** Metadata class for pagination information. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PaginationMeta {
    @JsonProperty("current_page")
    private int currentPage;

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("has_next_page")
    private boolean hasNextPage;
  }
}
