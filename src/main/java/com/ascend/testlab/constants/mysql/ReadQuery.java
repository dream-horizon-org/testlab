package com.ascend.testlab.constants.mysql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";
  public static final String FETCH_TAGS =
      "SELECT distinct tag FROM experiment.tags WHERE project_id = UUID_TO_BIN(?, true);";
}
