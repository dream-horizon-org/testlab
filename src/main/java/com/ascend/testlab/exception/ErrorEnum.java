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

  VALID_EXPERIMENT_STATUS_FAILED(
          "VALID_EXPERIMENT_STATUS_FAILED",
          "Experiment status is not valid",
          HttpStatus.SC_BAD_REQUEST
  ),

  VALID_EXPERIMENT_TYPE_FAILED(
          "VALID_EXPERIMENT_TYPE_FAILED",
          "Experiment type is not valid",
          HttpStatus.SC_BAD_REQUEST
  ),

  INVALID_PAGE_LIMIT(
          "INVALID_PAGE_LIMIT",
          "Page limit must be greater than 0",
          HttpStatus.SC_BAD_REQUEST
  ),

  INVALID_PAGE_NUMBER(
          "INVALID_PAGE_NUMBER",
          "Page number must be greater than 0",
          HttpStatus.SC_BAD_REQUEST
  );

  private final String errorCode;
  private final String errorMessage;
  private final int httpStatusCode;

  public static RestException handleException(Throwable throwable, RestException defaultException) {
    if (throwable instanceof RestException restException) return restException;
    else return defaultException;
  }
}
