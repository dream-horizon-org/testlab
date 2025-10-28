package com.ascend.testlab.constants;

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

  /* API Path */
  public static final String FILTER_EXPERIMENTS_PATH = "/v1/experiment";
  public static final String GET_EXPERIMENT_PATH = "/v1/experiment/{experimentId}";

  public static final String PROJECT_ID = "x-project-id";
  public static final String EXPERIMENT_ID = "experimentId";

  /* Filter Query Params */
  public static final String EXPERIMENT_STATUS = "status";
  public static final String EXPERIMENT_TYPE = "type";
  public static final String TAG = "tag";
  public static final String OWNER = "owner";
  public static final String NAME = "name";

  /* Pagination */
  public static final String OFFSET = "page";
  public static final String LIMIT = "limit";

}
