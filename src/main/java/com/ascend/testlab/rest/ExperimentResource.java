package com.ascend.testlab.rest;

import com.ascend.testlab.constants.ExperimentStatus;
import com.ascend.testlab.constants.ExperimentType;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API resource for Experiment operations.
 *
 * <p>Provides RESTful endpoints for creating, updating, and assigning experiments. All endpoints
 * require tenant-id and project-key headers for multi-tenancy and partitioning.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Path("/v1/experiment")
public class ExperimentResource {

  @Inject private ExperimentService experimentService;

  /**
   * Creates a new experiment.
   *
   * <p>Endpoint: POST /v1/experiment
   *
   * <p>Creates a new experiment with all provided details including cohorts, variant weights, and
   * assignment strategies. Requires validation of request body. Validates that status, type,
   * guardrail_health_status, and assignment_strategy match existing enum values.
   *
   * @param tenantId tenant identifier from x-tenant-id header
   * @param projectKey project identifier from x-project-key header
   * @param request validated experiment creation request
   * @return CompletionStage with CreateExperimentResponse containing id, status, and message
   * @throws BadRequestException if status, type, guardrail_health_status, or assignment_strategy
   *     don't match enum values
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Create a new experiment",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Created",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CreateExperimentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid enum values for status or type")
      })
  public CompletionStage<ResponseEntity.Success<CreateExperimentResponse>> create(
      @HeaderParam("x-tenant-id") UUID tenantId,
      @HeaderParam("x-project-key") UUID projectKey,
      @Valid CreateExperimentRequest request) {

    // Validate enum values
    validateEnumValues(request);

    return experimentService
        .create(tenantId, projectKey, request)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }

  /**
   * Updates experiment fields partially.
   *
   * <p>Endpoint: PATCH /v1/experiment/{experiment_id}
   *
   * <p>Performs dynamic partial update of experiment fields. Only provided fields are updated.
   * Supports updating enums, arrays, JSONB fields, and scalar values.
   *
   * @param tenantId tenant identifier from x-tenant-id header
   * @param projectKey project identifier from x-project-key header
   * @param experimentId experiment identifier from path parameter
   * @param request map of field names to values for update
   * @return CompletionStage with Boolean indicating success or failure
   */
  @PATCH
  @Path("/{experiment_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update experiment partially with optional tag replace")
  public CompletionStage<ResponseEntity.Success<Boolean>> update(
      @HeaderParam("x-tenant-id") UUID tenantId,
      @HeaderParam("x-project-key") UUID projectKey,
      @PathParam("experiment_id") UUID experimentId,
      java.util.Map<String, Object> request) {
    return experimentService
        .update(tenantId, projectKey, experimentId, request)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }

  /**
   * Validates enum values in the experiment request.
   *
   * <p>Checks if status, type, guardrail_health_status, and assignment_strategy match valid enum
   * values.
   *
   * @param request experiment creation request
   * @throws BadRequestException if any enum value is invalid
   */
  private void validateEnumValues(CreateExperimentRequest request) {
    List<String> errors = new ArrayList<>();

    // Validate status
    if (request.getStatus() != null) {
      try {
        ExperimentStatus.valueOf(request.getStatus().name());
      } catch (IllegalArgumentException e) {
        errors.add(
            String.format(
                "Invalid status value: '%s'. Valid values are: %s",
                request.getStatus(), getEnumValues(ExperimentStatus.class)));
        log.warn("Invalid status value: {}", request.getStatus());
      }
    }

    // Validate type
    if (request.getType() != null) {
      try {
        ExperimentType.valueOf(request.getType().name());
      } catch (IllegalArgumentException e) {
        errors.add(
            String.format(
                "Invalid type value: '%s'. Valid values are: %s",
                request.getType(), getEnumValues(ExperimentType.class)));
        log.warn("Invalid type value: {}", request.getType());
      }
    }

    // If there are validation errors, throw BadRequestException
    if (!errors.isEmpty()) {
      String errorMessage = String.join("; ", errors);
      log.error("Validation failed for create experiment request: {}", errorMessage);
      throw new BadRequestException(errorMessage);
    }
  }

  /**
   * Gets all enum values as a comma-separated string.
   *
   * @param enumClass enum class
   * @return comma-separated string of enum values
   */
  private <E extends Enum<E>> String getEnumValues(Class<E> enumClass) {
    E[] enumConstants = enumClass.getEnumConstants();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < enumConstants.length; i++) {
      sb.append(enumConstants[i].name());
      if (i < enumConstants.length - 1) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }
}
