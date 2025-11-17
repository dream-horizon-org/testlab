package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.Experiment;
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

  private List<Experiment> experimentList;
  private PaginationMeta pagination;

  /** Metadata class for pagination information. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PaginationMeta {
    private int currentPage;
    private int pageSize;
    private int totalCount;
  }
}
