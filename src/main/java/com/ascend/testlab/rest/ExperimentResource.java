package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.dto.response.UpdateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
@Path("/v1")
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
   * @param projectKey project identifier from x-project-key header
   * @param request validated experiment creation request
   * @return CompletionStage with CreateExperimentResponse containing id, status, and message
   */
  @POST
  @Path("/experiments")
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
      @HeaderParam("x-project-key") String projectKey, @Valid CreateExperimentRequest request) {

    Experiment experiment = Experiment.fromRequest(request);
    return experimentService
        .createExperiment(projectKey, experiment)
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
   * @param projectKey project identifier from x-project-key header
   * @param experimentId experiment identifier from path parameter
   * @param request map of field names to values for update
   * @return CompletionStage with Boolean indicating success or failure
   */
  @PATCH
  @Path("/experiments/{experiment_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Update experiment partially with optional tag replace",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid enum values or validation constraints")
      })
  public CompletionStage<ResponseEntity.Success<UpdateExperimentResponse>> update(
      @HeaderParam("x-project-key") String projectKey,
      @PathParam("experiment_id") UUID experimentId,
      @Valid UpdateExperimentRequest request) {

    return experimentService
        .updateExperiment(projectKey, experimentId, request)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
