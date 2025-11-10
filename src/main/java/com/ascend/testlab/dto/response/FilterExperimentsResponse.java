package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.Experiment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterExperimentsResponse {

  private List<Experiment> experimentList;
  private PaginationMeta pagination;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaginationMeta {
    private int currentPage;
    private int pageSize;
    private int totalCount;
  }
}
