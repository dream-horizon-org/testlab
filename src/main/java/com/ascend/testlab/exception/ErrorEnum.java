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

  /* Server Error */

  /** The error code for the health check failed. */
  REST_HEALTH_CHECK_FAILED(
      "testlab_REST_HEALTH_CHECK_FAILED",
      "HealthCheck Failed for testlab service",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for experiment name availability check failure. */
  REST_EXPERIMENT_NAME_CHECK_FAILED(
      "testlab_REST_EXPERIMENT_NAME_CHECK_FAILED",
      "Failed to check experiment name availability",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /* Client Error */

  /** The error code for the invalid project key. */
  INVALID_PROJECT_KEY(
      "testlab_INVALID_PROJECT_KEY",
      "x-project-key header is missing/invalid",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code for the invalid experiment id. */
  INVALID_EXPERIMENT_ID(
      "testlab_INVALID_EXPERIMENT_ID",
      "experiment-id is missing/invalid",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code for the invalid experiment name. */
  INVALID_EXPERIMENT_NAME(
      "testlab_INVALID_EXPERIMENT_NAME",
      "experiment name is missing/invalid",
      HttpStatus.SC_BAD_REQUEST),

  /** The error code for the experiment name too long. */
  EXPERIMENT_NAME_TOO_LONG(
      "testlab_EXPERIMENT_NAME_TOO_LONG",
      "experiment name is too long (max 255 characters)",
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
