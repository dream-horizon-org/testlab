package com.ascend.testlab.constants.postgresql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostgresColumn {
  EXPERIMENT_ID("experiment_id"),
  PROJECT_KEY("project_key"),
  ASSIGNMENT_DOMAIN("assignment_domain"),
  VARIANT_WEIGHTS("variant_weights"),
  RULE_ATTRIBUTES("rule_attributes"),
  OVERRIDES("overrides"),
  DISTRIBUTION_STRATEGY("distribution_strategy"),
  VARIANT("variants"),
  NAME("name"),
  DESCRIPTION("description"),
  STATUS("status"),
  COHORTS("cohorts"),
  CREATED_BY("created_by"),
  START_TIME("start_time"),
  END_TIME("end_time"),
  EXPOSURE("exposure"),
  THRESHOLD("threshold"),
  WINNING_VARIANT("winning_variant");
  private final String column;
}
