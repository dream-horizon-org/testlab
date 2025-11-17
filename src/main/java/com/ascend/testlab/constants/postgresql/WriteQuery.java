package com.ascend.testlab.constants.postgresql;

public final class WriteQuery {
  private WriteQuery() {}

  public static final String INSERT_EXPERIMENT =
      "INSERT INTO experiments (project_key, experiment_id, name, experiment_key, description, hypothesis, status, type, guardrail_health_status, cohorts, variant_weights, variants, distribution_strategy, assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold, start_time, end_time, created_by, name_tsvector) "
          + "VALUES ($1, $2, $3, $4, $5, $6, $7::experiment_status, $8::experiment_type, $9::experiment_health, $10::varchar[], $11::jsonb, $12::jsonb, $13::experiment_strategy, $14::assignment_domain, $15, $16::jsonb, $17::jsonb, $18, $19, $20, $21, $22, to_tsvector('simple', LOWER(REPLACE($3, '-', ' '))))";

  public static final String INSERT_EXPERIMENT_TAG =
      "INSERT INTO experiment.tags (experiment_id, project_key, tag) VALUES ($1, $2, $3) "
          + "ON CONFLICT (project_key, experiment_id, tag) DO UPDATE SET updated_at = CURRENT_TIMESTAMP";

  public static final String INSERT_EXPERIMENT_OWNER =
      "INSERT INTO experiment.owners (experiment_id, project_key, owner) VALUES ($1, $2, $3)";

  public static final String GET_TAGS =
      "SELECT tag FROM experiment.tags WHERE project_key = $1 AND experiment_id = $2";

  public static final String DELETE_TAGS =
      "DELETE FROM experiment.tags WHERE project_key = $1 AND experiment_id = $2 AND tag = ANY($3)";

  public static final String DELETE_EXPERIMENT_TAGS =
      "DELETE FROM experiment.tags WHERE project_key = $1 AND experiment_id = $2";

  public static final String DELETE_EXPERIMENT_OWNERS =
      "DELETE FROM experiment.owners WHERE project_key = $1 AND experiment_id = $2";

  public static final String CREATE_EXPERIMENTS_PARTITION =
      "CREATE TABLE IF NOT EXISTS experiments_%s PARTITION OF experiments FOR VALUES IN ('%s')";

  public static final String CREATE_TAGS_PARTITION =
      "CREATE TABLE IF NOT EXISTS experiment.tags_%s PARTITION OF experiment.tags FOR VALUES IN ('%s')";

  public static final String CREATE_OWNERS_PARTITION =
      "CREATE TABLE IF NOT EXISTS experiment.owners_%s PARTITION OF experiment.owners FOR VALUES IN ('%s')";

  public static final String CREATE_UPDATE_LOG_PARTITION =
      "CREATE TABLE IF NOT EXISTS experiment.experiment_update_log_%s PARTITION OF experiment.experiment_update_log FOR VALUES IN ('%s')";

  public static final String CREATE_ANALYSIS_PARTITION =
      "CREATE TABLE IF NOT EXISTS experiment.experiment_analysis_%s PARTITION OF experiment.experiment_analysis FOR VALUES IN ('%s')";

  public static final String INSERT_EXPERIMENT_UPDATE_LOG =
      "INSERT INTO experiment.experiment_update_log (project_key, experiment_id, previous_data, current_data, updated_by) "
          + "VALUES ($1, $2, $3, $4, $5) "
          + "ON CONFLICT (project_key, experiment_id) DO UPDATE SET "
          + "previous_data = EXCLUDED.previous_data, "
          + "current_data = EXCLUDED.current_data, "
          + "updated_by = EXCLUDED.updated_by, "
          + "updated_at = CURRENT_TIMESTAMP";

  public static final String INSERT_EXPERIMENT_ANALYSIS =
      "INSERT INTO experiment.experiment_analysis (project_key, experiment_id, config, primary_metrics, secondary_metrics, metric_tokens) "
          + "VALUES ($1, $2, $3, $4, $5, $6) "
          + "ON CONFLICT (project_key, experiment_id) DO NOTHING";

  public static final String UPDATE_EXPERIMENT_PREFIX = "UPDATE experiments SET ";
}
