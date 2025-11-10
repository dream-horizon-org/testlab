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

  /**
   * Query to retrieve a single experiment by project_key and experiment_id. Returns experiment
   * details including tags and owners aggregated as comma-separated strings.
   */
  public static final String GET_EXPERIMENT =
      """
      SELECT e.project_key, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type,
             e.guardrail_health_status, e.cohorts, e.variant_weights, e.assignment_strategy, e.overrides,
             e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time,
             e.created_by, e.created_at, e.updated_at,
             string_agg(DISTINCT t.tag, ',') as tags,
             string_agg(DISTINCT o.owner, ',') as owners
      FROM experiments e
      LEFT JOIN experiment.tags t ON e.project_key = t.project_key AND e.experiment_id = t.experiment_id
      LEFT JOIN experiment.owners o ON e.project_key = o.project_key AND e.experiment_id = o.experiment_id
      WHERE e.project_key = $1 AND e.experiment_id = $2
      GROUP BY e.project_key, e.experiment_id
      """;

  /**
   * Filter clause for searching experiments by name using PostgreSQL full-text search.
   */
  public static final String NAME_FILTER =
      " AND e.name_tsvector @@ plainto_tsquery('simple', <<NAME>>)";

  /**
   * GROUP BY clause for experiment queries. Groups results by project_key and experiment_id
   */
  public static final String GROUP_BY = " GROUP BY e.project_key, e.experiment_id";

  /** ORDER BY clause to sort experiments by creation date in descending order (newest first). */
  public static final String ORDER_BY_CREATED_AT = " ORDER BY e.created_at DESC";

  /**
   * LIMIT and OFFSET clause for pagination. Placeholders <<LIMIT>> and <<OFFSET>> should be
   * replaced with the limit and offset values respectively.
   */
  public static final String PAGINATION = " LIMIT <<LIMIT>> OFFSET <<OFFSET>>";

  /**
   * Filter clause for filtering experiments by status. Placeholder <<STATUS>> should be replaced
   * with a comma-separated list of status values.
   */
  public static final String STATUS_FILTER = " AND e.status in (<<STATUS>>)";

  /**
   * Filter clause for filtering experiments by type. Placeholder <<TYPE>> should be replaced with a
   * comma-separated list of type values.
   */
  public static final String TYPE_FILTER = " AND e.type in (<<TYPE>>)";

  /**
   * Query to find distinct experiment IDs that have the specified tags. Placeholder <<TAG>> should
   * be replaced with a comma-separated list of tag values. Used as a subquery to filter experiments
   * by tags.
   */
  public static final String TAGS_FILTER = " AND t.tag in (<<TAG>>)";

  /**
   * Query to find distinct experiment IDs that have the specified owners. Placeholder <<OWNER>>
   * should be replaced with a comma-separated list of owner values.
   */
  public static final String GET_EXPERIMENT_BY_OWNER_FILTER = " AND o.owner in (<<OWNER>>)";

  /**
   * Base query for filtering experiments by project_key. Returns experiment details including tags
   * and owners aggregated as comma-separated strings. Also includes total_count  to get the total number of matching experiments.
   */
  public static final String FILTER_EXPERIMENT =
      """
      SELECT e.project_key, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type,
             e.guardrail_health_status, e.cohorts, e.variant_weights, e.assignment_strategy, e.overrides,
             e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time,
             e.created_by, e.created_at, e.updated_at,
             string_agg(DISTINCT t.tag, ',') as tags,
             string_agg(DISTINCT o.owner, ',') as owners,
             COUNT(*) OVER() as total_count
      FROM experiments e
      LEFT JOIN experiment.tags t ON e.project_key = t.project_key AND e.experiment_id = t.experiment_id
      LEFT JOIN experiment.owners o ON e.project_key = o.project_key AND e.experiment_id = o.experiment_id
      WHERE e.project_key = $1
      """;
}
