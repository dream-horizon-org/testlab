package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetExperimentHistoryResponse {
  @JsonProperty("experiment_id")
  private String experimentId;

  @JsonProperty("history")
  private List<ExperimentHistoryEntry> history;

  @JsonProperty("total_count")
  private Integer totalCount;
}
