package com.ascend.testlab.provider;

import com.ascend.testlab.constants.Constants;
import com.dream11.rest.exception.RestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * Custom exception mapper for constraint violation exceptions. This mapper aggregates the
 * constraint violation messages and returns a REST exception.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see RestException
 */
@Slf4j
@Provider
public class CustomConstraintExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

  /**
   * Map a constraint violation exception to a REST exception.
   *
   * @param constraintViolationException the constraint violation exception
   * @return the response
   */
  @Override
  public Response toResponse(ConstraintViolationException constraintViolationException) {
    log.error("Constraint violation: ", constraintViolationException);

    String errorMessage =
        constraintViolationException.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessageTemplate)
            .collect(Collectors.joining(Constants.COMMA));

    RestException restException =
        new RestException(
            "INVALID_REQUEST",
            errorMessage,
            HttpStatus.SC_BAD_REQUEST,
            constraintViolationException);
    return Response.status(restException.getHttpStatusCode())
        .entity(restException.toString())
        .build();
  }
}
