package com.ascend.testlab.constants.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MySQLColumn {
  EXPERIMENT_ID("experiment_id"),
  TENANT_ID("tenant_id"),
  NAME("name"),
  DESCRIPTION("description"),
  HYPOTHESIS("hypothesis"),
  STATUS("status"),
  START_TIME("start_time"),
  END_TIME("end_time"),
  COHORT_ID("cohort_id"),
  DISTRIBUTION_STRATEGY("distribution_strategy"),
  ASSIGNMENT_DOMAIN("assignment_domain"),
  PERCENTAGE_DISTRIBUTION("percentage_distribution"),
  EXPOSURE("exposure"),
  CREATED_BY("created_by"),
  THRESHOLD("threshold"),
  IS_EXCLUSIVE("is_exclusive"),
  HEALTH("health"),
  TYPE("type"),
  CREATED_AT("created_at"),
  UPDATED_AT("updated_at"),
  NAME_TOKENS("name_tokens"),
  TAG("tag"),
  OWNER("owner"),
  UPDATED_BY("updated_by"),
  PREVIOUS_DATA("previous_data"),
  CURRENT_DATA("current_data");

  private final String column;
}
