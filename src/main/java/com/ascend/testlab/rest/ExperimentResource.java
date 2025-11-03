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
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Path("/v1/experiment")
public class ExperimentResource {

  @Inject private ExperimentService experimentService;

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
}
