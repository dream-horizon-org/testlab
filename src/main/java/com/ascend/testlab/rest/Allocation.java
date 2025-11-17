package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.AllocationRequest;
import com.ascend.testlab.service.AllocationService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

/**
 * Allocation endpoint for the testlab application. Contains methods to handle experiment assignment
 * requests for users.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see AllocationService
 */
@Path("/v1")
public class Allocation {
  private final AllocationService allocationService;

  @Inject
  public Allocation(AllocationService allocationService) {
    this.allocationService = allocationService;
  }

  @POST
  @Path("/allocation")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successful Response",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)))
  @ApiResponse(responseCode = "304", description = "Successful Not Modified Response")
  @ApiResponse(
      responseCode = "400",
      description = "Bad Request due to invalid/missing body params / header",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Bad Request due to invalid api-key",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<Response> allocationHandle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER) @DefaultValue(WebConstants.DEFAULT_PROJECT_KEY)
          String projectKey,
      @Valid @NotNull AllocationRequest assignRequest) {

    assignRequest.validate();
    return allocationService
        .allotExperiments(projectKey, assignRequest)
        .map(ResponseEntity.Success::new)
        .map(successData -> Response.ok(successData).build())
        .toCompletionStage();
  }

  @GET
  @Path("/allocation")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successful Response",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)))
  @ApiResponse(responseCode = "304", description = "Successful Not Modified Response")
  @ApiResponse(
      responseCode = "400",
      description = "Bad Request due to invalid/missing body params / header",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Bad Request due to invalid api-key",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<Response> getAllocationHandle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER) @DefaultValue(WebConstants.DEFAULT_PROJECT_KEY)
          String projectKey,
      @HeaderParam(WebConstants.USER_ID_HEADER) String userId) {

    return allocationService
        .getAllocations(userId, projectKey)
        .map(ResponseEntity.Success::new)
        .map(successData -> Response.ok(successData).build())
        .toCompletionStage();
  }
}
