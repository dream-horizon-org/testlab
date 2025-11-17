package com.ascend.testlab.rest;

import com.ascend.testlab.config.ApplicationConfig;
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
import org.apache.commons.lang3.StringUtils;

/**
 * Allocation endpoint for the testlab application. Contains methods to handle experiment assignment
 * requests for users.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AllocationService
 */
@Path("/v1")
public class Allocation {
  private final AllocationService allocationService;
  private final ApplicationConfig applicationConfig;

  @Inject
  public Allocation(AllocationService allocationService, ApplicationConfig applicationConfig) {
    this.allocationService = allocationService;
    this.applicationConfig = applicationConfig;
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
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER) String projectKey,
      @Valid @NotNull AllocationRequest assignRequest) {

    String effectiveProjectKey =
        StringUtils.isBlank(projectKey) ? applicationConfig.getProjectKey() : projectKey;

    assignRequest.validate();
    return allocationService
        .allotExperiments(effectiveProjectKey, assignRequest)
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
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER) String projectKey,
      @HeaderParam(WebConstants.USER_ID_HEADER) String userId) {

    String effectiveProjectKey =
        StringUtils.isBlank(projectKey) ? applicationConfig.getProjectKey() : projectKey;

    return allocationService
        .getAllocations(userId, effectiveProjectKey)
        .map(ResponseEntity.Success::new)
        .map(successData -> Response.ok(successData).build())
        .toCompletionStage();
  }
}
