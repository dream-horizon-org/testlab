package com.ascend.testlab.exception;

import lombok.experimental.UtilityClass;

/**
 * Utility class containing constant error message strings used throughout the application.
 *
 * <p>This class provides centralized error messages for validation failures, missing parameters,
 * and invalid values. All messages are public static final strings that can be used across
 * different layers of the application for consistent error reporting.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class ErrorMessages {
  /** Error message when the required x-project-key header is missing from the request. */
  public static final String PROJECT_KEY_MISSING = "x-project-key header is missing";

  /** The error message for invalid data type. */
  public static final String INVALID_DATA_TYPE = "Invalid dataType present";

  /** The error message for invalid condition value. */
  public static final String INVALID_CONDITION_VALUE = "Invalid condition value";

  /** The error message for invalid enum value. */
  public static final String INVALID_ENUM_VALUE = "Invalid enum value";

  /** The error message for invalid operator. */
  public static final String INVALID_OPERATOR = "Invalid operator value";

  /** The error message for blank rule name. */
  public static final String BLANK_RULE_NAME = "Rule name cannot be blank";

  /** The error message for invalid operand. */
  public static final String INVALID_OPERAND = "Invalid operand value";

  /** The error message for invalid operand data type. */
  public static final String INVALID_OPERAND_DATA_TYPE = "Invalid operand data type";

  /** Error message when an experiment status value is invalid. */
  public static final String INVALID_EXPERIMENT_STATUS = "Invalid experiment status";

  /** Error message when an experiment type value is invalid. */
  public static final String INVALID_EXPERIMENT_TYPE = "Invalid experiment type";

  /** Error message when an experiment health status value is invalid. */
  public static final String INVALID_EXPERIMENT_HEALTH = "Invalid experiment health status";

  /** Error message when an experiment strategy value is invalid. */
  public static final String INVALID_EXPERIMENT_STRATEGY = "Invalid distribution strategy";

  /** Error message when an assignment domain value is invalid. */
  public static final String INVALID_ASSIGNMENT_DOMAIN = "Invalid assignment domain";

  /** Error message when the required experimentKey query parameter is missing. */
  public static final String EXPERIMENT_KEY_MISSING = "ExperimentKey query parameter is missing";

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
