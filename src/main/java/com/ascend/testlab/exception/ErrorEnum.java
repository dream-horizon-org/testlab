package com.ascend.testlab.exception;

import com.dream11.rest.exception.RestError;
import com.dream11.rest.exception.RestException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorEnum implements RestError {
  REST_HEALTH_CHECK_FAILED(
      "testlab_REST_HEALTH_CHECK_FAILED",
      "HealthCheck Failed for testlab service",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),
  REST_FETCH_TAGS_FAILED(
      "testlab_REST_FETCH_TAGS_FAILED",
      "Tags Listing failed due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR),
  INVALID_PROJECT_ID(
      "testlab_INVALID_PROJECT_ID",
      "tenant-id header is missing/invalid",
      HttpStatus.SC_BAD_REQUEST),
  INVALID_EXPERIMENT_ID(
      "testlab_INVALID_EXPERIMENT_ID",
      "experiment-id is missing/invalid",
      HttpStatus.SC_BAD_REQUEST),
  REST_GET_EXPERIMENT_HISTORY_FAILED(
      "testlab_REST_GET_EXPERIMENT_HISTORY_FAILED",
      "Failed to fetch experiment history due to: %s",
      HttpStatus.SC_INTERNAL_SERVER_ERROR);

  private final String errorCode;
  private final String errorMessage;
  private final int httpStatusCode;

  public static RestException handleException(Throwable throwable, RestException defaultException) {
    if (throwable instanceof RestException restException) return restException;
    else return defaultException;
  }
}
