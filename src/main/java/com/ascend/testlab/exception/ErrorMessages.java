package com.ascend.testlab.exception;

import lombok.experimental.UtilityClass;

/**
 * Utility class containing constant error message strings used throughout the application.
 *
 * <p>This class provides centralized error messages for validation failures, missing parameters,
 * and invalid values. All messages are public static final strings that can be used across
 * different layers of the application for consistent error reporting.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class ErrorMessages {
  /** Error message when the required x-project-key header is missing from the request. */
  public static final String PROJECT_KEY_MISSING = "x-project-key header is missing";

  /** Error message when an invalid data type is provided in the request. */
  public static final String INVALID_DATA_TYPE = "invalid dataType present";

  /** Error message when a condition value does not meet validation requirements. */
  public static final String INVALID_CONDITION_VALUE = "invalid condition value";

  /** Error message when an enum value provided does not match any valid enum constant. */
  public static final String INVALID_ENUM_VALUE = "invalid enum value";

  /** Error message when an operator value is not recognized or supported. */
  public static final String INVALID_OPERATOR = "invalid operator value";

  /** Error message when a rule name is blank or empty. */
  public static final String BLANK_RULE_NAME = "rule name cannot be blank";

  /** Error message when an operand value is invalid or not supported. */
  public static final String INVALID_OPERAND = "invalid operand value";

  /** Error message when an operand data type is invalid or not recognized. */
  public static final String INVALID_OPERAND_DATA_TYPE = "invalid operand data type";

  /** Error message when an experiment status value is invalid. */
  public static final String INVALID_EXPERIMENT_STATUS = "invalid experiment status";

  /** Error message when an experiment type value is invalid. */
  public static final String INVALID_EXPERIMENT_TYPE = "invalid experiment type";

  /** Error message when an experiment health status value is invalid. */
  public static final String INVALID_EXPERIMENT_HEALTH = "invalid experiment health status";

  /** Error message when an experiment strategy value is invalid. */
  public static final String INVALID_EXPERIMENT_STRATEGY = "invalid experiment strategy";

  /** Error message when an assignment domain value is invalid. */
  public static final String INVALID_ASSIGNMENT_DOMAIN = "invalid assignment domain";

  /** Error message when the required experimentKey query parameter is missing. */
  public static final String EXPERIMENT_KEY_MISSING = "experimentKey query parameter is missing";

  /** Error message when the required experiment_id parameter is missing from the request. */
  public static final String EXPERIMENT_ID_MISSING = "experiment_id parameter is missing";

  /** Error message when the limit value for pagination is less than or equal to zero. */
  public static final String INVALID_LIMIT_VALUE = "Limit must be greater than zero";

  /** Error message when the page value for pagination is less than or equal to zero. */
  public static final String INVALID_PAGE_VALUE = "Page value must be greater than zero";

  /**
   * Error message when the experiment key exceeds the maximum allowed length.
   *
   * <p>Maximum length is 255 characters as defined in the database schema.
   */
  public static final String EXPERIMENT_KEY_TOO_LONG =
      "Experiment key is too long (max 255 characters)";

  /** Error message when a variant name is missing or empty in the request. */
  public static final String VARIANT_NAME_MISSING = "Variant name is required";

  /** Error message when a user ID is missing or empty in the allocation request. */
  public static final String USER_ID_MISSING = "User ID is required";
}
