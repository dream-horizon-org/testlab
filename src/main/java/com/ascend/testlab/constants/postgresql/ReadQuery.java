package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";
  public static final String FETCH_TAGS =
      "SELECT distinct tag FROM experiment.tags WHERE project_key = $1;";

  public static final String CHECK_EXPERIMENT_NAME_EXISTS =
      "SELECT COUNT(*) FROM experiment.experiments WHERE project_id = $1::uuid AND name = $2;";

  public static final String FETCH_EXPERIMENT_HISTORY =
      "SELECT updated_by, previous_data, current_data, created_at, updated_at "
          + "FROM experiment.experiment_update_log "
          + "WHERE project_id = $1::uuid AND experiment_id = $2::uuid "
          + "ORDER BY created_at DESC;";
}
