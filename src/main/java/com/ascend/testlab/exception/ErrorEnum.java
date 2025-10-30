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

  VALID_EXPERIMENT_STATUS_FAILED(
      "VALID_EXPERIMENT_STATUS_FAILED",
      "Experiment status is not valid",
      HttpStatus.SC_BAD_REQUEST),

  VALID_EXPERIMENT_TYPE_FAILED(
      "VALID_EXPERIMENT_TYPE_FAILED", "Experiment type is not valid", HttpStatus.SC_BAD_REQUEST),

  INVALID_PAGE_LIMIT(
      "INVALID_PAGE_LIMIT", "Page limit must be greater than 0", HttpStatus.SC_BAD_REQUEST),

  INVALID_PAGE_NUMBER(
      "INVALID_PAGE_NUMBER", "Page number must be greater than 0", HttpStatus.SC_BAD_REQUEST),

  EXPERIMENT_NOT_FOUND(
      "EXPERIMENT_NOT_FOUND",
      "Experiment not found for the given experimentId",
      HttpStatus.SC_NOT_FOUND),

  DATABASE_ERROR(
      "DATABASE_ERROR",
      "Database error occurred while processing the request",
      HttpStatus.SC_INTERNAL_SERVER_ERROR);

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
