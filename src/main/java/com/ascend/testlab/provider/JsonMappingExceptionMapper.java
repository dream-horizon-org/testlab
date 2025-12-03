package com.ascend.testlab.provider;

import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * Custom exception mapper for Jackson JSON mapping exceptions.
 *
 * <p>Handles deserialization errors including invalid enum values and provides user-friendly error
 * messages.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Provider
@jakarta.annotation.Priority(1)
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

  @Override
  public Response toResponse(JsonMappingException exception) {
    log.error("JSON mapping error: {}", exception.getMessage());
    log.debug("Full exception details: ", exception);

    String errorMessage = "Invalid request format";
    String fieldName = getFieldName(exception);

    if (exception instanceof InvalidFormatException) {
      InvalidFormatException ife = (InvalidFormatException) exception;
      if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
        String validValues = getEnumValues(ife.getTargetType());
        errorMessage =
            String.format(
                "Invalid value '%s' for field '%s'. Allowed values: %s",
                ife.getValue(), fieldName, validValues);
        log.warn(
            "Invalid enum value '{}' for field '{}'. Valid values: {}",
            ife.getValue(),
            fieldName,
            validValues);
      } else {
        errorMessage =
            String.format("Invalid value '%s' for field '%s'", ife.getValue(), fieldName);
        log.warn("Invalid value '{}' for field '{}'", ife.getValue(), fieldName);
      }
    } else {
      errorMessage =
          String.format(
              "Invalid format for field '%s': %s", fieldName, exception.getOriginalMessage());
      log.warn("Invalid format for field '{}': {}", fieldName, exception.getOriginalMessage());
    }

    RestException restException =
        new RestException("INVALID_REQUEST", errorMessage, HttpStatus.SC_BAD_REQUEST, exception);

    log.info("Returning 400 Bad Request with message: {}", errorMessage);

    return Response.status(restException.getHttpStatusCode())
        .entity(restException.toString())
        .build();
  }

  /**
   * Extracts the field name from the exception path.
   *
   * @param exception the JSON mapping exception
   * @return the field name
   */
  private String getFieldName(JsonMappingException exception) {
    if (exception.getPath() != null && !exception.getPath().isEmpty()) {
      return exception.getPath().get(exception.getPath().size() - 1).getFieldName();
    }
    return "unknown";
  }

  /**
   * Gets the valid enum values as a comma-separated string.
   *
   * @param enumClass the enum class
   * @return comma-separated enum values
   */
  private String getEnumValues(Class<?> enumClass) {
    if (enumClass.isEnum()) {
      Object[] constants = enumClass.getEnumConstants();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < constants.length; i++) {
        sb.append(constants[i]);
        if (i < constants.length - 1) {
          sb.append(", ");
        }
      }
      return sb.toString();
    }
    return "";
  }
}
