package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

/**
 * Utility class for PostgreSQL writer queries.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class WriteQuery {

  /** Query to delete an experiment by project key and experiment ID. */
  public static final String DELETE_EXPERIMENT_BY_ID =
      "DELETE FROM experiment.experiments WHERE project_key = $1 AND experiment_id = $2;";

  /** Query to delete all tags associated with an experiment. */
  public static final String DELETE_TAG_FOR_EXPERIMENT =
      "DELETE FROM experiment.tags WHERE project_key = $1 AND experiment_id = $2;";

  /** Query to delete all owners associated with an experiment. */
  public static final String DELETE_OWNER_FOR_EXPERIMENT =
      "DELETE FROM experiment.owners WHERE project_key = $1 AND experiment_id = $2;";

  /** Query to insert the experiment update log with previous and current data. */
  public static final String INSERT_EXPERIMENT_UPDATE_LOG =
      """
         INSERT INTO experiment.experiment_update_log ( project_key, experiment_id, previous_data,
         current_data ) VALUES ( $1, $2, $3, $4);
         """;
}
