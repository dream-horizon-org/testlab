package com.ascend.testlab.constants.postgresql;

/**
 * PostgreSQL write query constants for experiment management.
 *
 * <p>Contains parameterized SQL queries for INSERT, UPDATE, and DELETE operations on experiment
 * tables. All queries use positional parameters ($1, $2, etc.) for safe parameterized execution.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
public final class WriteQuery {
  /**
   * Inserts a new experiment into the experiments table.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: name (VARCHAR) - also used for tsvector generation
   *   <li>$4: experiment_key (VARCHAR)
   *   <li>$5: description (VARCHAR)
   *   <li>$6: hypothesis (VARCHAR)
   *   <li>$7: status (experiment_status enum)
   *   <li>$8: type (experiment_type enum)
   *   <li>$9: guardrail_health_status (experiment_health enum)
   *   <li>$10: cohorts (VARCHAR[])
   *   <li>$11: variant_weights (JSONB)
   *   <li>$12: variants (JSONB)
   *   <li>$13: distribution_strategy (experiment_strategy enum)
   *   <li>$14: assignment_domain (assignment_domain enum)
   *   <li>$15: overrides (VARCHAR[])
   *   <li>$16: rule_attributes (JSONB)
   *   <li>$17: winning_variant (JSONB)
   *   <li>$18: exposure (INTEGER)
   *   <li>$19: threshold (INTEGER)
   *   <li>$20: start_time (BIGINT)
   *   <li>$21: end_time (BIGINT)
   *   <li>$22: created_by (VARCHAR)
   * </ol>
   */
  public static final String INSERT_EXPERIMENT =
      """
      INSERT INTO experiment.experiments (
        project_key, experiment_id, name, experiment_key, description, hypothesis,
        status, type, cohorts, variant_weights, variants,
        distribution_strategy, assignment_domain, overrides, rule_attributes,
        exposure, threshold, start_time, end_time, created_by
      )
      VALUES (
        $1, $2, $3::varchar, $4, $5, $6,\s
        $7::experiment.experiment_status, $8::experiment.experiment_type, $9::varchar[], $10::jsonb, $11::jsonb,
        $12::experiment.experiment_strategy, $13::experiment.assignment_domain, $14::jsonb, $15::jsonb,
        $16, $17, $18, $19, $20
      )
     \s""";

  /**
   * Inserts a tag for an experiment.
   *
   * <p>Uses ON CONFLICT to update the timestamp if the tag already exists for the experiment.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: experiment_id (UUID)
   *   <li>$2: project_key (VARCHAR)
   *   <li>$3: tag (VARCHAR)
   * </ol>
   */
  public static final String INSERT_EXPERIMENT_TAG =
      """
      INSERT INTO experiment.tags (experiment_id, project_key, tag)
      VALUES ($1, $2, $3)
      ON CONFLICT (project_key, experiment_id, tag)
      DO UPDATE SET updated_at = CURRENT_TIMESTAMP
      """;

  /**
   * Inserts an owner for an experiment.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: experiment_id (UUID)
   *   <li>$2: project_key (VARCHAR)
   *   <li>$3: owner (VARCHAR) - email or identifier of the owner
   * </ol>
   */
  public static final String INSERT_EXPERIMENT_OWNER =
      """
      INSERT INTO experiment.owners (experiment_id, project_key, owner)
      VALUES ($1, $2, $3)
      """;

  /**
   * Deletes specific tags from an experiment.
   *
   * <p>Uses the ANY operator to delete multiple tags in a single query.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: tags (VARCHAR[]) - array of tags to delete
   * </ol>
   */
  public static final String DELETE_TAGS =
      """
      DELETE FROM experiment.tags
      WHERE project_key = $1 AND experiment_id = $2 AND tag = ANY($3)
      """;

  /**
   * Deletes specific owners from an experiment.
   *
   * <p>Uses the ANY operator to delete multiple owners in a single query.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: owners (VARCHAR[]) - array of owners to delete
   * </ol>
   */
  public static final String DELETE_OWNERS =
      """
      DELETE FROM experiment.owners
      WHERE project_key = $1 AND experiment_id = $2 AND owner = ANY($3)
      """;

  /**
   * Deletes all tags associated with an experiment.
   *
   * <p>Used during experiment deletion or when clearing all tags.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   * </ol>
   */
  public static final String DELETE_EXPERIMENT_TAGS =
      """
      DELETE FROM experiment.tags
      WHERE project_key = $1 AND experiment_id = $2
      """;

  /**
   * Deletes all tags associated with an experiment.
   *
   * <p>Used during experiment deletion or when clearing all tags.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   * </ol>
   */
  public static final String DELETE_EXPERIMENT_ANALYSIS =
      """
            DELETE FROM experiment.experiment_analysis
            WHERE project_key = $1 AND experiment_id = $2
            """;

  /**
   * Inserts an experiment update log entry.
   *
   * <p>Records changes to experiments for audit trail and history tracking. Each update creates a
   * new log entry with previous and current state.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: previous_data (JSONB) - experiment state before update
   *   <li>$4: current_data (JSONB) - experiment state after update
   *   <li>$5: updated_by (VARCHAR) - user who made the update
   * </ol>
   */
  public static final String INSERT_EXPERIMENT_UPDATE_LOG =
      """
      INSERT INTO experiment.experiment_update_log (
        project_key, experiment_id, previous_data, current_data, updated_by
      )
      VALUES ($1, $2, $3, $4, $5)
      """;

  /**
   * Inserts experiment analysis configuration and metrics.
   *
   * <p>Stores analysis settings, primary and secondary metrics for experiment evaluation. Uses ON
   * CONFLICT DO NOTHING to avoid duplicate entries.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: config (VARCHAR) - analysis configuration
   *   <li>$4: primary_metrics (VARCHAR) - comma-separated primary metrics
   *   <li>$5: secondary_metrics (VARCHAR) - comma-separated secondary metrics
   *   <li>$6: metric_tokens (VARCHAR) - tokenized metrics for search
   * </ol>
   */
  public static final String INSERT_EXPERIMENT_ANALYSIS =
      """
      INSERT INTO experiment.experiment_analysis (
        project_key, experiment_id, primary_metrics, secondary_metrics
      )
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (project_key, experiment_id)
      DO NOTHING
      """;

  /**
   * Updates experiment analysis metrics.
   *
   * <p>Updates the primary and secondary metrics for an existing experiment analysis record.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   *   <li>$3: primary_metrics (VARCHAR)
   *   <li>$4: secondary_metrics (VARCHAR)
   * </ol>
   */
  public static final String UPDATE_EXPERIMENT_ANALYSIS =
      """
      UPDATE experiment.experiment_analysis
      SET primary_metrics = $3, secondary_metrics = $4
      WHERE project_key = $1 AND experiment_id = $2
      """;

  /**
   * Deletes an experiment by project key and experiment ID.
   *
   * <p>This is a hard delete that removes the experiment record. Related data (tags, owners, logs)
   * should be deleted separately or via cascade rules.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   * </ol>
   */
  public static final String DELETE_EXPERIMENT_BY_ID =
      "DELETE FROM experiment.experiments WHERE project_key = $1 AND experiment_id = $2;";

  /**
   * Deletes all tags associated with an experiment.
   *
   * <p>Used during experiment deletion to clean up related data.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   * </ol>
   */
  public static final String DELETE_TAG_FOR_EXPERIMENT =
      "DELETE FROM experiment.tags WHERE project_key = $1 AND experiment_id = $2;";

  /**
   * Deletes all owners associated with an experiment.
   *
   * <p>Used during experiment deletion to clean up related data.
   *
   * <p>Parameters:
   *
   * <ol>
   *   <li>$1: project_key (VARCHAR)
   *   <li>$2: experiment_id (UUID)
   * </ol>
   */
  public static final String DELETE_OWNER_FOR_EXPERIMENT =
      "DELETE FROM experiment.owners WHERE project_key = $1 AND experiment_id = $2;";

  public static final String UPDATE_PARTITION_STATUS =
      """
      UPDATE experiment.partition_metadata
      SET status = $1::experiment.partition_status,
          updated_at = $2
      WHERE project_key = $3
      """;

  public static final String UPSERT_PARTITION_METADATA_CREATING =
      """
    INSERT INTO experiment.partition_metadata (project_key, status, created_by, created_at, updated_at)
    VALUES ($1, $2::experiment.partition_status, $3, $4, $5)
    ON CONFLICT (project_key)
    DO UPDATE SET
      status = EXCLUDED.status,
      updated_at = EXCLUDED.updated_at
    """;

  public static String buildCreateListPartitionQuery(
      String schema, String parentTable, String projectKey) {

    String suffix = projectKey.replace("-", "_");

    return String.format(
        """
        CREATE TABLE IF NOT EXISTS %s.%s_%s
        PARTITION OF %s.%s
        FOR VALUES IN ('%s')
        """,
        schema, parentTable, suffix, schema, parentTable, projectKey);
  }
}
