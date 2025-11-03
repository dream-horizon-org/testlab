package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.AssignmentRequest;
import com.ascend.testlab.service.AssignmentService;
import com.ascend.testlab.util.CommonUtil;
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
import lombok.RequiredArgsConstructor;

@Path("/v1")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class Assignment {
  private final AssignmentService assignmentService;

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
      @HeaderParam(WebConstants.USER_ID_HEADER) String userId,
      @HeaderParam(WebConstants.GUEST_ID_HEADER) String guestId,
      @HeaderParam(WebConstants.TENANT_ID_HEADER) String tenantId,
      @Valid AssignmentRequest assignRequest) {

//    CommonUtil.validateUser(userId);

    if (guestId != null && !guestId.isEmpty()) {
      assignRequest.setGuestId(guestId);
    }

    return assignmentService
        .assignExperiments(UUID.fromString(tenantId), userId, assignRequest)
        .map(ResponseEntity.Success::new)
        .map(successData -> Response.ok(successData).build())
        .toCompletionStage();
  }
}
