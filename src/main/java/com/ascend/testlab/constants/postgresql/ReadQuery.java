package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

/**
 * Utility class for PostgreSQL reader queries.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class ReadQuery {

  /** The health check query to check if the PostgreSQL reader database is connected. */
  public static final String HEALTH_CHECK = "SELECT 1;";

  /** The query to fetch distinct tags for a given project key. */
  public static final String FETCH_TAGS =
      "SELECT distinct tag FROM experiment.tags WHERE project_key = $1;";

  /** The query to fetch experiment data for update log. */
  public static final String GET_EXPERIMENT_DATA =
      "SELECT project_key, experiment_id, name, description, hypothesis, status, type, "
          + "guardrail_health_status, cohorts, variant_weights, variants, distribution_strategy, "
          + "assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold, "
          + "start_time, end_time, created_by, created_at, updated_at "
          + "FROM experiments WHERE project_key = $1 AND experiment_id = $2";
}
