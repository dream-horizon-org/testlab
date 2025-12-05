package com.ascend.testlab.provider;

import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * Custom exception mapper for Jackson JSON mapping exceptions.
 *
 * <p>Handles deserialization errors including:
 *
 * <ul>
 *   <li>Invalid enum values
 *   <li>Invalid polymorphic type identifiers (e.g., variant_weights type)
 *   <li>Type mismatches in JSON structure
 * </ul>
 *
 * @author Anudeep Reddy
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

    String errorMessage = buildErrorMessage(exception);

    RestException restException =
        new RestException("INVALID_REQUEST", errorMessage, HttpStatus.SC_BAD_REQUEST, exception);

    log.info("Returning 400 Bad Request with message: {}", errorMessage);

    return Response.status(restException.getHttpStatusCode())
        .entity(restException.toString())
        .build();
  }

  /**
   * Builds an appropriate error message based on the exception type.
   *
   * @param exception the JSON mapping exception
   * @return user-friendly error message
   */
  private String buildErrorMessage(JsonMappingException exception) {
    String fieldName = getFieldName(exception);

    if (exception instanceof InvalidTypeIdException typeIdException) {
      return handleInvalidTypeIdException(typeIdException, fieldName);
    }

    if (exception instanceof InvalidFormatException formatException) {
      return handleInvalidFormatException(formatException, fieldName);
    }

    if (exception instanceof MismatchedInputException mismatchException) {
      return handleMismatchedInputException(mismatchException, fieldName);
    }

    log.warn("Invalid format for field '{}': {}", fieldName, exception.getOriginalMessage());
    return String.format(
        "Invalid format for field '%s': %s", fieldName, exception.getOriginalMessage());
  }

  /**
   * Handles InvalidTypeIdException for polymorphic type parsing errors.
   *
   * @param exception the invalid type id exception
   * @param fieldName the field name
   * @return error message
   */
  private String handleInvalidTypeIdException(InvalidTypeIdException exception, String fieldName) {
    String typeId = exception.getTypeId();
    String baseTypeName =
        exception.getBaseType() != null
            ? exception.getBaseType().getRawClass().getSimpleName()
            : "unknown";

    log.warn(
        "Invalid type '{}' for polymorphic field '{}' of base type '{}'",
        typeId,
        fieldName,
        baseTypeName);

    if ("VariantWeights".equals(baseTypeName)) {
      if (typeId == null || typeId.isEmpty()) {
        return "Missing 'type' field in variant_weights. Required values: STRATIFIED, COHORT";
      }
      return String.format(
          "Invalid variant_weights type '%s'. Allowed values: STRATIFIED, COHORT", typeId);
    }

    if (typeId == null || typeId.isEmpty()) {
      return String.format("Missing 'type' field for '%s'", fieldName);
    }

    return String.format(
        "Invalid type '%s' for field '%s'. Check the 'type' property value.", typeId, fieldName);
  }

  /**
   * Handles InvalidFormatException for format/enum parsing errors.
   *
   * @param exception the invalid format exception
   * @param fieldName the field name
   * @return error message
   */
  private String handleInvalidFormatException(InvalidFormatException exception, String fieldName) {
    if (exception.getTargetType() != null && exception.getTargetType().isEnum()) {
      String validValues = getEnumValues(exception.getTargetType());
      log.warn(
          "Invalid enum value '{}' for field '{}'. Valid values: {}",
          exception.getValue(),
          fieldName,
          validValues);
      return String.format(
          "Invalid value '%s' for field '%s'. Allowed values: %s",
          exception.getValue(), fieldName, validValues);
    }

    log.warn("Invalid value '{}' for field '{}'", exception.getValue(), fieldName);
    return String.format("Invalid value '%s' for field '%s'", exception.getValue(), fieldName);
  }

  /**
   * Handles MismatchedInputException for type mismatch errors.
   *
   * @param exception the mismatched input exception
   * @param fieldName the field name
   * @return error message
   */
  private String handleMismatchedInputException(
      MismatchedInputException exception, String fieldName) {
    Class<?> targetType = exception.getTargetType();
    String expectedType = targetType != null ? targetType.getSimpleName() : "unknown";

    log.warn("Type mismatch for field '{}'. Expected: {}", fieldName, expectedType);
    return String.format("Invalid data type for field '%s'. Expected: %s", fieldName, expectedType);
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
