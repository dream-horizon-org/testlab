package com.ascend.testlab.constants.mysql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";

  public static final String GET_EXPERIMENTS =
      " SELECT experiment_id, project_id, name, description, status, cohorts,"
          + "        variant_weights, rule_attributes, start_time, overrides, end_time,"
          + "        entities, exposure, threshold, distribution_strategy"
          + "        FROM experiments"
          + "        WHERE project_id = ? AND status = 'ACTIVE' "
          + "        AND start_time <= ? AND (end_time IS NULL OR end_time > ?)";
}
