package com.ascend.testlab.exception;

import lombok.experimental.UtilityClass;

/** This class contains constant error message strings used throughout the application. */
@UtilityClass
public final class ErrorMessages {
  /** The error message for the project key missing. */
  public static final String PROJECT_KEY_MISSING = "x-project-key header is missing";

  public static final String INVALID_DATA_TYPE = "invalid dataType present";

  public static final String INVALID_CONDITION_VALUE = "invalid condition value";

  public static final String INVALID_ENUM_VALUE = "invalid enum value";

  public static final String INVALID_OPERATOR = "invalid operator value";

  public static final String BLANK_RULE_NAME = "rule name cannot be blank";

  public static final String INVALID_OPERAND = "invalid operand value";

  public static final String INVALID_OPERAND_DATA_TYPE = "invalid operand data type";

  public static final String INVALID_EXPERIMENT_STATUS = "invalid experiment status";

  public static final String INVALID_EXPERIMENT_TYPE = "invalid experiment type";

  public static final String INVALID_EXPERIMENT_HEALTH = "invalid experiment health status";

  public static final String INVALID_EXPERIMENT_STRATEGY = "invalid experiment strategy";

  public static final String INVALID_ASSIGNMENT_DOMAIN = "invalid assignment domain";

  /** The error message for the experiment key missing. */
  public static final String EXPERIMENT_KEY_MISSING = "experimentKey query parameter is missing";

  /** The error message for the experiment id missing. */
  public static final String EXPERIMENT_ID_MISSING = "experiment_id parameter is missing";

  /** The error message for invalid limit value. */
  public static final String INVALID_LIMIT_VALUE = "Limit must be greater than zero";

  /** The error message for invalid page value. */
  public static final String INVALID_PAGE_VALUE = "Page value must be greater than zero";

  /** The error message for experiment key too long. */
  public static final String EXPERIMENT_KEY_TOO_LONG =
      "Experiment key is too long (max 255 characters)";
}
