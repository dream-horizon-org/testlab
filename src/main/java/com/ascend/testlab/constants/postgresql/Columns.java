package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

/**
 * Utility class for PostgreSQL column names. Contains constants for all column names used in
 * database queries and mappers.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class Columns {

  /** Column name for project_key. */
  public static final String PROJECT_KEY = "project_key";

  /** Column name for experiment_id. */
  public static final String EXPERIMENT_ID = "experiment_id";

  /** Column name for name. */
  public static final String NAME = "name";

  /** Column name for description. */
  public static final String DESCRIPTION = "description";

  /** Column name for hypothesis. */
  public static final String HYPOTHESIS = "hypothesis";

  /** Column name for status. */
  public static final String STATUS = "status";

  /** Column name for type. */
  public static final String TYPE = "type";

  /** Column name for guardrail_health_status. */
  public static final String GUARDRAIL_HEALTH_STATUS = "guardrail_health_status";

  /** Column name for cohorts. */
  public static final String COHORTS = "cohorts";

  /** Column name for variant_weights. */
  public static final String VARIANT_WEIGHTS = "variant_weights";

  /** Column name for assignment_strategy. */
  public static final String ASSIGNMENT_STRATEGY = "assignment_strategy";

  /** Column name for overrides. */
  public static final String OVERRIDES = "overrides";

  /** Column name for rule_attributes. */
  public static final String RULE_ATTRIBUTES = "rule_attributes";

  /** Column name for winning_variant. */
  public static final String WINNING_VARIANT = "winning_variant";

  /** Column name for exposure. */
  public static final String EXPOSURE = "exposure";

  /** Column name for threshold. */
  public static final String THRESHOLD = "threshold";

  /** Column name for start_time. */
  public static final String START_TIME = "start_time";

  /** Column name for end_time. */
  public static final String END_TIME = "end_time";

  /** Column name for created_by. */
  public static final String CREATED_BY = "created_by";

  /** Column name for created_at. */
  public static final String CREATED_AT = "created_at";

  /** Column name for updated_at. */
  public static final String UPDATED_AT = "updated_at";

  /** Column name for updated_by. */
  public static final String UPDATED_BY = "updated_by";

  /** Column name for previous_data. */
  public static final String PREVIOUS_DATA = "previous_data";

  /** Column name for current_data. */
  public static final String CURRENT_DATA = "current_data";

  /** Column name for tag. */
  public static final String TAG = "tag";

  /** Column name for tags (aggregated). */
  public static final String TAGS = "tags";

  /** Column name for owners (aggregated). */
  public static final String OWNERS = "owners";

  /** Column name for total_count (window function result). */
  public static final String TOTAL_COUNT = "total_count";
}
