package com.ascend.testlab.exception;

import com.dream11.rest.exception.RestError;
import com.dream11.rest.exception.RestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

/**
 * Enum for the error codes and messages.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see RestError
 */
@Getter
@AllArgsConstructor
public enum ErrorEnum implements RestError {

  /** The error code for the health check failed. */
  REST_HEALTH_CHECK_FAILED(
      "testlab_REST_HEALTH_CHECK_FAILED",
      "HealthCheck Failed for testlab service",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  MISSING_USER_IDENTIFIER(
      "EXPERIMENT_SERVICE_MISSING_USER_IDENTIFIER",
      "Missing User Identifier, pass user-id/guest-id",
      HttpStatus.SC_BAD_REQUEST),

  USER_COHORTS_SERVICE_REQUEST_FAILED(
      "FF_USER_COHORTS_SERVICE_REQUEST_FAILED",
      "User-Cohorts service request failed with statusCode:%s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  INVALID_REQUEST_BODY(
      "FF_INVALID_REQUEST_BODY",
      "Request body param(s) is/are missing/invalid",
      HttpStatus.SC_BAD_REQUEST);

  /** The error code. */
  private final String errorCode;

  /** The error message. */
  private final String errorMessage;

  /** The HTTP status code. */
  private final int httpStatusCode;

  /**
   * Handle the exception, mapping the throwable to a RestException.
   *
   * @param throwable the throwable
   * @param defaultException the default exception to return if the throwable is not a RestException
   * @return the rest exception
   */
  public static RestException handleException(Throwable throwable, RestException defaultException) {
    if (throwable instanceof RestException restException) return restException;
    else return defaultException;
  }
}
