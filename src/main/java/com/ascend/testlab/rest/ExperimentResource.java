package com.ascend.testlab.rest;

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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
@Path("/v1/experiment")
public class ExperimentResource {

  @Inject private ExperimentService experimentService;

  /**
   * Creates a new experiment.
   *
   * <p>Endpoint: POST /v1/experiment
   *
   * <p>Creates a new experiment with all provided details including cohorts, variant weights, and
   * assignment strategies. Requires validation of request body.
   *
   * @param tenantId tenant identifier from x-tenant-id header
   * @param projectKey project identifier from x-project-key header
   * @param request validated experiment creation request
   * @return CompletionStage with CreateExperimentResponse containing id, status, and message
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Create a new experiment",
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "Created",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON,
                      schema = @Schema(implementation = CreateExperimentResponse.class))))
  public CompletionStage<ResponseEntity.Success<CreateExperimentResponse>> create(
      @HeaderParam("x-tenant-id") UUID tenantId,
      @HeaderParam("x-project-key") UUID projectKey,
      @Valid CreateExperimentRequest request) {
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
   * Gets assigned experiment for a user.
   *
   * <p>Endpoint: GET /v1/experiment/{experiment_id}?status=LIVE,DRAFT
   *
   * <p>Retrieves experiment assigned to a specific user based on status filter. Status parameter
   * accepts comma-separated values.
   *
   * @param tenantId tenant identifier from x-tenant-id header
   * @param projectKey project identifier from x-project-key header
   * @param userId user identifier from path parameter
   * @param status comma-separated list of experiment statuses to filter
   * @return CompletionStage with assigned experiment details
   */
  @GET
  @Path("/{experiment_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get Assign experiment of the user")
  public CompletionStage<ResponseEntity.Success<CreateExperimentRequest>> getAssignExperiment(
      @HeaderParam("x-tenant-id") UUID tenantId,
      @HeaderParam("x-project-key") UUID projectKey,
      @PathParam("x-user-id") UUID userId,
      @QueryParam("status") String status) {
    List<String> statusList = List.of(status.split(","));
    return experimentService
        .assignExperiment(tenantId, projectKey, userId, statusList)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
