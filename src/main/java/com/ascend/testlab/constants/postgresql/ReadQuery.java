package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";
  public static final String FETCH_TAGS =
      "SELECT distinct tag FROM experiment.tags WHERE project_id = UUID_TO_BIN(?, true);";

  public static final String CHECK_EXPERIMENT_NAME_EXISTS =
      "SELECT COUNT(*) FROM experiment.experiments WHERE project_id = UUID_TO_BIN(?, true) AND name = ?;";

  public static final String FETCH_EXPERIMENT_HISTORY =
      "SELECT updated_by, previous_data, current_data, created_at, updated_at "
          + "FROM experiment.experiment_update_log "
          + "WHERE project_id = UUID_TO_BIN(?, true) AND experiment_id = UUID_TO_BIN(?, true) "
          + "ORDER BY created_at DESC;";
}
