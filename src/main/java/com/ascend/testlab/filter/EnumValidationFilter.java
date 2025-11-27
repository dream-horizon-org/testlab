package com.ascend.testlab.filter;

import com.ascend.testlab.constants.enums.*;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

/**
 * Pre-matching filter to validate enum values in JSON requests.
 *
 * <p>This filter intercepts requests before they reach the JacksonProvider and validates enum
 * fields, providing clear error messages for invalid enum values.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Provider
@PreMatching
public class EnumValidationFilter implements ContainerRequestFilter {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // Only validate POST and PATCH requests to experiment endpoints
    String method = requestContext.getMethod();
    String path = requestContext.getUriInfo().getPath();

    if ((method.equals("POST") || method.equals("PATCH")) && path.contains("/v1/experiment")) {

      // Read the request body
      InputStream inputStream = requestContext.getEntityStream();
      byte[] requestBody = IOUtils.toByteArray(inputStream);

      // Reset the stream so it can be read again by the next filter/provider
      requestContext.setEntityStream(new ByteArrayInputStream(requestBody));

      if (requestBody.length > 0) {
        try {
          JsonNode jsonNode = objectMapper.readTree(requestBody);

          // Validate enum fields - check both camelCase and snake_case
          validateEnumField(jsonNode, "status", ExperimentStatus.class, "status", requestContext);
          validateEnumField(jsonNode, "type", ExperimentType.class, "type", requestContext);

          // Check both formats for guardrail_health_status
          if (jsonNode.has("guardrail_health_status")) {
            validateEnumField(
                jsonNode,
                "guardrail_health_status",
                HealthStatus.class,
                "guardrail_health_status",
                requestContext);
          } else if (jsonNode.has("guardrailHealthStatus")) {
            validateEnumField(
                jsonNode,
                "guardrailHealthStatus",
                HealthStatus.class,
                "guardrail_health_status",
                requestContext);
          }

          // Check both formats for distribution_strategy
          if (jsonNode.has("distribution_strategy")) {
            validateEnumField(
                jsonNode,
                "distribution_strategy",
                DistributionStrategy.class,
                "distribution_strategy",
                requestContext);
          } else if (jsonNode.has("distributionStrategy")) {
            validateEnumField(
                jsonNode,
                "distributionStrategy",
                DistributionStrategy.class,
                "distribution_strategy",
                requestContext);
          }

          // Check both formats for assignment_domain
          if (jsonNode.has("assignment_domain")) {
            validateEnumField(
                jsonNode,
                "assignment_domain",
                AssignmentDomain.class,
                "assignment_domain",
                requestContext);
          } else if (jsonNode.has("assignmentDomain")) {
            validateEnumField(
                jsonNode,
                "assignmentDomain",
                AssignmentDomain.class,
                "assignment_domain",
                requestContext);
          }

        } catch (Exception e) {
          log.debug("Error parsing JSON for enum validation: {}", e.getMessage());
          // If we can't parse, let it through - the JacksonProvider will handle it
        }
      }
    }
  }

  /**
   * Validates a single enum field in the JSON.
   *
   * @param jsonNode the JSON node
   * @param fieldName the field name to validate
   * @param enumClass the enum class
   * @param displayName the display name for error messages
   * @param requestContext the request context
   */
  private void validateEnumField(
      JsonNode jsonNode,
      String fieldName,
      Class<? extends Enum<?>> enumClass,
      String displayName,
      ContainerRequestContext requestContext) {

    if (jsonNode.has(fieldName) && !jsonNode.get(fieldName).isNull()) {
      String value = jsonNode.get(fieldName).asText();

      Enum<?>[] enumConstants = enumClass.getEnumConstants();
      boolean isValid =
          Arrays.stream(enumConstants).anyMatch(e -> e.name().equalsIgnoreCase(value.trim()));

      if (!isValid) {
        String validValues =
            Arrays.stream(enumConstants).map(Enum::name).collect(Collectors.joining(", "));

        String errorMessage =
            String.format(
                "Invalid value '%s' for field '%s'. Allowed values are: %s",
                value, displayName, validValues);

        log.warn("Enum validation failed: {}", errorMessage);

        RestException restException =
            new RestException("INVALID_REQUEST", errorMessage, HttpStatus.SC_BAD_REQUEST, null);

        requestContext.abortWith(
            Response.status(HttpStatus.SC_BAD_REQUEST).entity(restException.toString()).build());
      }
    }
  }
}
