package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FilterExperimentsResponse {

  private List<Experiment> experiments;
  private PaginationMeta pagination;
}
