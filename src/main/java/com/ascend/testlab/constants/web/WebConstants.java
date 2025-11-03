package com.ascend.testlab.constants.web;

import lombok.experimental.UtilityClass;

/**
 * Utility class for web constants.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class WebConstants {

  /** The key for the content type header. */
  public static final String CONTENT_TYPE = "Content-Type";

  /** The value for the application/json content type. */
  public static final String APPLICATION_JSON = "application/json";

  public static final String USER_ID_HEADER = "x-user-Id";
  public static final String GUEST_ID_HEADER = "x-guest-Id";
  public static final String TENANT_ID_HEADER = "x-tenant-Id";

  /* Circuit Breaker Constants */

  /** The name of the buffered calls count. */
  public static final String BUFFERED_CALLS_COUNT = "bufferedCallsCount";

  /** The name of the failed calls count. */
  public static final String FAILED_CALLS_COUNT = "failedCallsCount";

  /** The name of the failure rate. */
  public static final String FAILURE_RATE = "failureRate";

  /** The name of the not permitted calls count. */
  public static final String NOT_PERMITTED_CALLS_COUNT = "notPermittedCallsCount";

  /** The name of the slow calls count. */
  public static final String SLOW_CALLS_COUNT = "slowCallsCount";

  /** The name of the slow failed calls count. */
  public static final String SLOW_FAILED_CALLS_COUNT = "slowFailedCallsCount";

  /** The name of the slow successful calls count. */
  public static final String SLOW_SUCCESSFUL_CALLS_COUNT = "slowSuccessfulCallsCount";

  /** The name of the slow call rate. */
  public static final String SLOW_CALL_RATE = "slowCallRate";

  /** The name of the state. */
  public static final String STATE = "state";

  /** The name of the successful calls count. */
  public static final String SUCCESSFUL_CALLS_COUNT = "successfulCallsCount";
}
