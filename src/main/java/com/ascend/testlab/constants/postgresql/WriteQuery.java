package com.ascend.testlab.constants.postgresql;

public final class WriteQuery {
  private WriteQuery() {}

  public static final String INSERT_EXPERIMENT =
      "INSERT INTO experiments (project_key, experiment_id, name, description, hypothesis, status, type, guardrail_health_status, cohorts, variant_weights, assignment_strategy, overrides, rule_attributes, winning_variant, exposure, threshold, start_time, end_time, created_by, name_tsvector) "
          + "VALUES (?, ?, ?, ?, ?, ?::experiment_status, ?::experiment_type, ?::experiment_health, ?::varchar[], ?::jsonb, ?::experiment_strategy, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, to_tsvector('english', ?))";

  public static final String INSERT_EXPERIMENT_TAG =
      "INSERT INTO experiment_tags (tenant_id, experiment_id, tag) VALUES (?::uuid, ?::uuid, ?)";

  public static final String DELETE_EXPERIMENT_TAGS =
      "DELETE FROM experiment_tags WHERE tenant_id = ?::uuid AND experiment_id = ?::uuid";

  public static final String UPDATE_EXPERIMENT_PREFIX = "UPDATE experiments SET ";

  public static final String UPDATE_EXPERIMENT_SUFFIX =
      " WHERE project_key = ? AND experiment_id = ?";
}
