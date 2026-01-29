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

  /** The name of the main verticle. */
  public static final String MAIN_VERTICLE = "com.ascend.testlab.verticle.MainVerticle";

  /** The key for the application environment. */
  public static final String APP_ENV_KEY = "app.environment";

  /** The default application environment. */
  public static final String DEFAULT_APP_ENV = "dev";

  /** The admin role. */
  public static final String ADMIN = "ADMIN";

  /** Default value for updatedBy field when not provided. */
  public static final String SYSTEM = "SYSTEM";

  /** The maximum number of REST verticles. */
  public static final Integer MAX_NUM_REST_VERTICLES = 16;

  /* Delimiter Constants */

  /** The delimiter for comma. */
  public static final String COMMA = ",";

  /** The delimiter for empty string. */
  public static final String EMPTY_STRING = "";

  /** The delimiter for colon. */
  public static final String COLON = ":";

  /** The delimiter for underscore. */
  public static final String UNDER_SCORE = "_";

  /** The method name for getting enum name. */
  public static final String NAME = "name";

  /** The delimiter for apostrophe. */
  public static final String APOSTROPHE = "'";

  /** The maximum length for experiment key. */
  public static final int MAX_EXPERIMENT_KEY_LENGTH = 255;

  /** The name of the getType method. */
  public static final String GET_TYPE = "getType";

  /** The name of the getName method. */
  public static final String GET_NAME = "getName";

  /** The name of the schema. */
  public static final String SCHEMA = "experiment";
}
