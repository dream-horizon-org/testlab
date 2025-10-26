package com.ascend.testlab.dto.response;

import com.ascend.testlab.dto.entity.Experiment;
import java.util.List;
import lombok.Data;

@Data
public class FilterExperimentResponse {
  private List<Experiment> experimentList;
}
