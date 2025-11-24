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

  /* Server Errors */

  /** The error code for the health check failed. */
  REST_HEALTH_CHECK_FAILED(
      // TODO: Verify error code standards
      "testlab_REST_HEALTH_CHECK_FAILED",
      "HealthCheck Failed for testlab service",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for the tags listing failed. */
  REST_FETCH_TAGS_FAILED(
      "testlab_REST_FETCH_TAGS_FAILED",
      "Tags Listing failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for experiment key availability check failed. */
  REST_EXPERIMENT_KEY_CHECK_FAILED(
      "testlab_REST_EXPERIMENT_KEY_CHECK_FAILED",
      "Failed to check experiment key availability",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for get experiment history failed. * */
  REST_FETCH_EXPERIMENT_HISTORY_FAILED(
      "testlab_REST_GET_EXPERIMENT_HISTORY_FAILED",
      "Failed to fetch experiment history due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for getting experiment by ID failed. */
  REST_GET_EXPERIMENT_BY_ID_FAILED(
      "testlab_REST_GET_EXPERIMENT_BY_ID_FAILED",
      "Get Experiment by Id failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /** The error code for filtering experiments failed. */
  REST_FILTER_EXPERIMENTS_FAILED(
      "testlab_REST_FILTER_EXPERIMENTS_FAILED",
      "Filter Experiments failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),

  /* Client Errors */

  /** The error code when experiment is not found. */
  EXPERIMENT_NOT_FOUND(
      "EXPERIMENT_NOT_FOUND",
      "Experiment not found for the given experimentId",
      HttpStatus.SC_NOT_FOUND),

  /** The error code for validating experiment status failed. */
  INVALID_EXPERIMENT_STATUS(
      "INVALID_EXPERIMENT_STATUS", "Experiment status is not valid", HttpStatus.SC_BAD_REQUEST),

  /** The error code for validating experiment type failed. */
  INVALID_EXPERIMENT_TYPE(
      "INVALID_EXPERIMENT_TYPE", "Experiment type is not valid", HttpStatus.SC_BAD_REQUEST);

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
