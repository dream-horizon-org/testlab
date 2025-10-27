package com.ascend.testlab.constants.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MySQLColumn {
  PROJECT_ID("project_id"),
  EXPERIMENT_ID("experiment_id"),
  NAME("name"),
  DESCRIPTION("description"),
  HYPOTHESIS("hypothesis"),
  STATUS("status"),
  TYPE("type"),
  GUARDRAIL_HEALTH_STATUS("guardrail_health_status"),
  COHORTS("cohorts"),
  VARIANT_WEIGHTS("variant_weights"),
  ASSIGNMENT_STRATEGY("assignment_strategy"),
  OVERRIDES("overrides"),
  RULE_ATTRIBUTES("rule_attributes"),
  WINNING_VARIANT("winning_variant"),
  EXPOSURE("exposure"),
  THRESHOLD("threshold"),
  START_TIME("start_time"),
  END_TIME("end_time"),
  CREATED_BY("created_by"),
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
