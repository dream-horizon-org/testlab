package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.service.AssignmentService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * Assignment endpoint for the testlab application. Contains methods to handle experiment assignment
 * requests for users.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see AssignmentService
 */
@Path("/v1")
public class Assignment {
  private final AssignmentService assignmentService;

  @Inject
  public Assignment(AssignmentService assignmentService) {
    this.assignmentService = assignmentService;
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
  public CompletionStage<Response> handle(
      @HeaderParam(WebConstants.TENANT_ID_HEADER) String tenantId,
      @Valid AssignmentRequest assignRequest) {

    assignRequest.validate();
    return assignmentService
        .assignExperiments(UUID.fromString(tenantId), assignRequest)
        .map(ResponseEntity.Success::new)
        .map(successData -> Response.ok(successData).build())
        .toCompletionStage();
  }

  //  @PUT
  //  @Path("/allocation")
  //  @Consumes(MediaType.APPLICATION_JSON)
  //  @Produces(MediaType.APPLICATION_JSON)
  //  @ApiResponse(
  //      responseCode = "200",
  //      description = "Successful Reassignment",
  //      content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)))
  //  @ApiResponse(
  //      responseCode = "400",
  //      description = "Bad Request due to invalid/missing body params / header",
  //      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  //  @ApiResponse(
  //      responseCode = "404",
  //      description = "Experiment or user assignment not found",
  //      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  //  @ApiResponse(
  //      responseCode = "500",
  //      description = "Internal Server Error",
  //      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  //  public CompletionStage<Response> handle(
  //      @HeaderParam(WebConstants.USER_ID_HEADER) String userId,
  //      @HeaderParam(WebConstants.TENANT_ID_HEADER) String tenantId,
  //      @Valid ReassignmentRequest reassignRequest) {
  //
  //    return assignmentService
  //        .reassignExperiment(UUID.fromString(tenantId), reassignRequest)
  //        .map(ResponseEntity.Success::new)
  //        .map(successData -> Response.ok(successData).build())
  //        .onErrorReturn(
  //            error -> {
  //              if (error instanceof IllegalArgumentException) {
  //                return Response.status(Response.Status.BAD_REQUEST)
  //                    .entity(new ResponseEntity.Failure("INVALID_REQUEST", error.getMessage(),
  // ""))
  //                    .build();
  //              } else if (error.getMessage() != null && error.getMessage().contains("not found"))
  // {
  //                return Response.status(Response.Status.NOT_FOUND)
  //                    .entity(new ResponseEntity.Failure("NOT_FOUND", error.getMessage(), ""))
  //                    .build();
  //              } else {
  //                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
  //                    .entity(
  //                        new ResponseEntity.Failure(
  //                            "REASSIGNMENT_FAILED",
  //                            error.getMessage() != null ? error.getMessage() : "Unknown error",
  //                            ""))
  //                    .build();
  //              }
  //            })
  //        .toCompletionStage();
  //  }
}
