package com.ascend.testlab.constants.web;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class WebConstants {
  /* Circuit Breaker Constants */
  public static final String BUFFERED_CALLS_COUNT = "bufferedCallsCount";
  public static final String FAILED_CALLS_COUNT = "failedCallsCount";
  public static final String FAILURE_RATE = "failureRate";
  public static final String NOT_PERMITTED_CALLS_COUNT = "notPermittedCallsCount";
  public static final String SLOW_CALLS_COUNT = "slowCallsCount";
  public static final String SLOW_FAILED_CALLS_COUNT = "slowFailedCallsCount";
  public static final String SLOW_SUCCESSFUL_CALLS_COUNT = "slowSuccessfulCallsCount";
  public static final String SLOW_CALL_RATE = "slowCallRate";
  public static final String STATE = "state";
  public static final String SUCCESSFUL_CALLS_COUNT = "successfulCallsCount";

  /* Header Constants */
  public static final String PROJECT_ID_HEADER = "x-project-id";

  /* Query Parameters*/
  public static final String EXPERIMENT_NAME = "name";
}
