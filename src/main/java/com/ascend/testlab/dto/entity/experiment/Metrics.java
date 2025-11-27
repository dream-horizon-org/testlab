package com.ascend.testlab.dto.entity.experiment;

import java.util.List;
import lombok.Data;

@Data
public class Metrics {

  List<String> primary;
  List<String> secondary;
}
