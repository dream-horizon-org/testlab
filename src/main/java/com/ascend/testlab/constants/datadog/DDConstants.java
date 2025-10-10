package com.ascend.testlab.constants.datadog;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DDConstants {
  public static final String DD_AGENT_HOST = "DD_AGENT_HOST";
  public static final String DD_PREFIX = "testlab";
  public static final String SERVICE_NAME = "SERVICE_NAME";

  public static final String CB_METRIC = "circuitBreaker";
  public static final String CB_NAME = "cbName";
}
