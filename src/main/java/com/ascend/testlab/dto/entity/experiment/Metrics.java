package com.ascend.testlab.dto.entity.experiment;

import java.util.List;
import lombok.Data;

/**
 * Entity representing experiment metrics configuration.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
public class Metrics {

  /** Primary metrics for the experiment. */
  List<String> primary;

  /** Secondary metrics for the experiment. */
  List<String> secondary;
}
