package com.ascend.testlab.constants.postgresql;

public final class WriteQuery {
  private WriteQuery() {}

  public static final String INSERT_EXPERIMENT =
      "INSERT INTO experiments (project_key, experiment_id, name, description, hypothesis, status, type, guardrail_health_status, cohorts, variant_weights, assignment_strategy, overrides, rule_attributes, winning_variant, exposure, threshold, start_time, end_time, created_by) "
          + "VALUES ($1, $2, $3, $4, $5, $6::experiment_status, $7::experiment_type, $8::experiment_health, $9::varchar[], $10::jsonb, $11::experiment_strategy, $12::jsonb, $13::jsonb, $14::jsonb, $15, $16, $17, $18, $19)";

  public static final String UPDATE_EXPERIMENT_PREFIX = "UPDATE experiments SET ";
}
