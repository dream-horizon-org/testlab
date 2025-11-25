package com.ascend.testlab.constants.postgresql;

import java.util.function.BiFunction;
import java.util.function.IntFunction;
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
      """
        SELECT DISTINCT tag
        FROM experiment.tags
        WHERE project_key = $1;
        """;

  /** The query to check if an experiment key exists for a given project key. */
  public static final String CHECK_EXPERIMENT_KEY =
      """
         SELECT EXISTS (
             SELECT 1
             FROM experiment.experiments
             WHERE project_key = $1
               AND experiment_key = $2
         );
         """;

  /** The query to retrieve the update history of a specific experiment with pagination. */
  public static final String FETCH_EXPERIMENT_HISTORY =
      """
        SELECT
            previous_data,
            current_data,
            updated_by,
            updated_at,
            created_at,
            COUNT(*) OVER() AS total_count
        FROM experiment.experiment_update_log
        WHERE project_key = $1
          AND experiment_id = $2
        ORDER BY created_at DESC
        LIMIT $3 OFFSET $4;
        """;

  /** Query to retrieve the count of history entries for a given project_key and experiment_id. */
  public static final String GET_EXPERIMENT_HISTORY_COUNT =
      """
        SELECT COUNT(1)
        FROM experiment.experiment_update_log
        WHERE project_key = $1
        AND experiment_id = $2;
        """;

  /** The query to fetch experiment data for update log. */
  public static final String GET_EXPERIMENT_DATA =
      """
      SELECT
        project_key, experiment_id, name, description, hypothesis, status, type,
        guardrail_health_status, cohorts, variant_weights, variants, distribution_strategy,
        assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold,
        start_time, end_time, created_by, created_at, updated_at
      FROM experiments
      WHERE project_key = $1 AND experiment_id = $2
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
      GROUP BY e.project_key, e.experiment_id;
      """;

  /**
   * Filter clause for searching experiments by name using PostgreSQL full-text search. Accepts the
   * parameter index and returns the parameterized query fragment.
   */
  public static final IntFunction<String> NAME_FILTER =
      " AND e.name_tsvector @@ plainto_tsquery('simple', $%d)"::formatted;

  /** GROUP BY clause for experiment queries. Groups results by project_key and experiment_id */
  public static final String GROUP_BY = " GROUP BY e.project_key, e.experiment_id";

  /** ORDER BY clause to sort experiments by creation date in descending order (newest first). */
  public static final String ORDER_BY_CREATED_AT = " ORDER BY e.created_at DESC";

  /**
   * LIMIT and OFFSET clause for pagination. Accepts limit and offset parameter indices and returns
   * the parameterized query fragment.
   */
  public static final BiFunction<Integer, Integer, String> PAGINATION =
      " LIMIT $%d OFFSET $%d"::formatted;

  /**
   * Filter clause for filtering experiments by status. Accepts the parameter index and returns the
   * parameterized query fragment using ANY array syntax.
   */
  public static final IntFunction<String> STATUS_FILTER = " AND e.status = ANY($%d)"::formatted;

  /**
   * Filter clause for filtering experiments by type. Accepts the parameter index and returns the
   * parameterized query fragment using ANY array syntax.
   */
  public static final IntFunction<String> TYPE_FILTER = " AND e.type = ANY($%d)"::formatted;

  /**
   * Filter clause for filtering experiments by tags. Accepts the parameter index and returns the
   * parameterized query fragment using ANY array syntax.
   */
  public static final IntFunction<String> TAGS_FILTER = " AND t.tag = ANY($%d)"::formatted;

  /**
   * Filter clause for filtering experiments by owners. Accepts the parameter index and returns the
   * parameterized query fragment using ANY array syntax.
   */
  public static final IntFunction<String> OWNER_FILTER = " AND o.owner = ANY($%d)"::formatted;

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
