package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for experiment allocation containing the list of assigned experiments.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResponse {
  @JsonProperty(value = "experiment_map")
  private List<UserExperimentMap> experimentMap;
}
