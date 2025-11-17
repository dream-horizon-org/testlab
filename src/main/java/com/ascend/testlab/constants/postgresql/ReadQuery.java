package com.ascend.testlab.constants.postgresql;

import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
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

  /** Query to fetch active experiments for a tenant within a time range */
  public static final String GET_EXPERIMENTS_FROM_KEY =
      "SELECT * FROM experiment.experiments WHERE project_key = $1 AND status = 'LIVE' AND experiment_key = ANY($2::text[])";

  /** Query to fetch concluded experiments for a tenant with winning variants */
  public static final String GET_CONCLUDED_EXPERIMENTS =
      """
      SELECT *
      FROM experiment.experiments
      WHERE project_key = $1
        AND status = 'CONCLUDED'
        AND winning_variant IS NOT NULL
      """;

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
      FROM experiment.experiments e
      LEFT JOIN experiment.tags t ON e.project_key = t.project_key AND e.experiment_id = t.experiment_id
      LEFT JOIN experiment.owners o ON e.project_key = o.project_key AND e.experiment_id = o.experiment_id
      WHERE e.project_key = $1 AND e.experiment_id = $2
      GROUP BY e.project_key, e.experiment_id
      """;

  /** Filter clause for searching experiments by name using PostgreSQL full-text search. */
  public static final Function<String, String> NAME_FILTER =
      " AND e.name_tsvector @@ plainto_tsquery('simple', '%s')"::formatted;

  /** GROUP BY clause for experiment queries. Groups results by project_key and experiment_id */
  public static final String GROUP_BY = " GROUP BY e.project_key, e.experiment_id";

  /** ORDER BY clause to sort experiments by creation date in descending order (newest first). */
  public static final String ORDER_BY_CREATED_AT = " ORDER BY e.created_at DESC";

  /**
   * LIMIT and OFFSET clause for pagination. Uses format placeholders %d for limit and offset
   * values.
   */
  public static final BiFunction<Integer, Integer, String> PAGINATION =
      " LIMIT %d OFFSET %d"::formatted;

  /**
   * Filter clause for filtering experiments by status. Uses format placeholder %s for a
   * comma-separated list of status values.
   */
  public static final Function<String, String> STATUS_FILTER = " AND e.status IN (%s)"::formatted;

  /**
   * Filter clause for filtering experiments by type. Uses format placeholder %s for a
   * comma-separated list of type values.
   */
  public static final Function<String, String> TYPE_FILTER = " AND e.type in (%s)"::formatted;

  /**
   * Filter clause for filtering experiments by tags. Uses format placeholder %s for a
   * comma-separated list of tag values.
   */
  public static final Function<String, String> TAGS_FILTER = " AND t.tag in (%s)"::formatted;

  /**
   * Filter clause for filtering experiments by owners. Uses format placeholder %s for a
   * comma-separated list of owner values.
   */
  public static final Function<String, String> OWNER_FILTER = " AND o.owner in (%s)"::formatted;

  /**
   * Base query for filtering experiments by project_key. Returns experiment details including tags
   * and owners aggregated as comma-separated strings. Also includes total_count to get the total
   * number of matching experiments.
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
      FROM experiment.experiments e
      LEFT JOIN experiment.tags t ON e.project_key = t.project_key AND e.experiment_id = t.experiment_id
      LEFT JOIN experiment.owners o ON e.project_key = o.project_key AND e.experiment_id = o.experiment_id
      WHERE e.project_key = $1
      """;
}
