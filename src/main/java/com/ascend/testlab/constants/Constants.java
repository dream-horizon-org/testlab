package com.ascend.testlab.constants;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {
  public static final String PACKAGE_NAME = "com.ascend.testlab";
  public static final String APP_ENV_KEY = "app.environment";
  public static final String DEFAULT_APP_ENV = "dev";
  public static final Integer MAX_NUM_REST_VERTICLES = 16;

  /* Delimiter Constants */
  public static final String COMMA = ",";
  public static final String COLON = ":";
  public static final String SPACE = " ";

  /* Regex Pattern Constants */
  public static final Pattern UUID_REGEX =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
}
