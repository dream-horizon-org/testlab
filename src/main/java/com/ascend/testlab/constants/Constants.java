package com.ascend.testlab.constants;

import lombok.experimental.UtilityClass;

/**
 * Utility class for constants.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class Constants {
  /** The name of the package. */
  public static final String PACKAGE_NAME = "com.ascend.testlab";

  /** The key for the application environment. */
  public static final String APP_ENV_KEY = "app.environment";

  /** The default application environment. */
  public static final String DEFAULT_APP_ENV = "dev";

  /** The maximum number of REST verticles. */
  public static final Integer MAX_NUM_REST_VERTICLES = 16;

  /* Delimiter Constants */

  /** The delimiter for comma. */
  public static final String COMMA = ",";

  /** The delimiter for colon. */
  public static final String COLON = ":";

  /** The delimiter for space. */
  public static final String SPACE = " ";

  public static final String EMPTY_STRING = "";

  public static final String GET_TYPE = "getType";
  public static final String GET_NAME = "getName";

  public static final String STATUS_ASSIGNED = "ASSIGNED";
  public static final String STATUS_CONCLUDED = "CONCLUDED";
}
