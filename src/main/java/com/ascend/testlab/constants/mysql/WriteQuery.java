package com.ascend.testlab.constants.mysql;

public final class WriteQuery {
  private WriteQuery() {}

  public static final String INSERT_EXPERIMENT =
      "INSERT INTO experiments (tenant_id, experiment_id, name, description, hypothesis, rules_json, actuals, status, start_time, end_time, cohort_id, distribution_strategy, assignment_domain, percentage_distribution, exposure, created_by, threshold, is_exclusive, health, type, name_tokens) "
          + "VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String INSERT_EXPERIMENT_TAG =
      "INSERT INTO experiment_tags (tenant_id, experiment_id, tag) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?)";

  public static final String DELETE_EXPERIMENT_TAGS =
      "DELETE FROM experiment_tags WHERE tenant_id = UUID_TO_BIN(?) AND experiment_id = UUID_TO_BIN(?)";

  public static final String UPDATE_EXPERIMENT_PREFIX = "UPDATE experiments SET ";

  public static final String UPDATE_EXPERIMENT_SUFFIX =
      " WHERE tenant_id = UUID_TO_BIN(?) AND experiment_id = UUID_TO_BIN(?)";
}
