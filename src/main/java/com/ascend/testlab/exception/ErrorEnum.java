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

  MISSING_USER_IDENTIFIER(
      "EXPERIMENT_SERVICE_MISSING_USER_IDENTIFIER",
      "Missing User Identifier, pass user-id/guest-id",
      HttpStatus.SC_BAD_REQUEST);

  private final String errorCode;
  private final String errorMessage;
  private final int httpStatusCode;

  public static RestException handleException(Throwable throwable, RestException defaultException) {
    if (throwable instanceof RestException restException) return restException;
    else return defaultException;
  }
}
