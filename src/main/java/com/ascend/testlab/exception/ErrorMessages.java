package com.ascend.testlab.exception;

import lombok.experimental.UtilityClass;

/** This class contains constant error message strings used throughout the application. */
@UtilityClass
public final class ErrorMessages {
  /** The error message for the project key missing. */
  public static final String PROJECT_KEY_MISSING = "x-project-key header is missing";

  /** The error message for the experiment name missing. */
  public static final String EXPERIMENT_NAME_MISSING = "name query parameter is missing";

  /** The error message for the experiment id missing. */
  public static final String EXPERIMENT_ID_MISSING = "experiment_id parameter is missing";

  /** The error message for invalid limit value. */
  public static final String INVALID_LIMIT_VALUE = "Limit must be greater than zero";

  /** The error message for invalid page value. */
  public static final String INVALID_PAGE_VALUE = "Page value must be greater than zero";

  /** The error message for invalid experiment status. */
  public static final String INVALID_EXPERIMENT_STATUS = "Experiment status is not valid";

  /** The error message for invalid experiment type. */
  public static final String INVALID_EXPERIMENT_TYPE = "Experiment type is not valid";

  /** The error message for experiment name too long. */
  public static final String EXPERIMENT_NAME_TOO_LONG =
      "Experiment name is too long (max 64 characters)";
}
