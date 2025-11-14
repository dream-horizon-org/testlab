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
}
