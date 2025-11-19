package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.ExperimentHistoryRequest;
import com.ascend.testlab.dto.response.GetExperimentHistoryResponse;
import com.ascend.testlab.service.AdminService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

/**
 * REST endpoint for fetching experiment update history for a specific experiment.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see GetExperimentHistoryResponse
 */
@Path("/v1/experiments/{experimentId}/history")
public class GetExperimentHistory {

  /** The admin service. */
  private final AdminService adminService;

  /**
   * Constructor for the GetExperimentHistory class.
   *
   * @param adminService the admin service
   */
  @Inject
  public GetExperimentHistory(AdminService adminService) {
    this.adminService = adminService;
  }

  /**
   * Handles the GET request to fetch the experiment history for a project with pagination support.
   *
   * <p>Pagination defaults to limit=20 and page=1 if not specified. Page numbers start at 1.
   *
   * @param request the request containing project key (header), experiment ID (path), limit, and
   *     page (query parameters)
   * @return a CompletionStage containing a ResponseEntity with the history payload and pagination
   *     metadata
   * @throws jakarta.validation.ConstraintViolationException if inputs are blank or invalid
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully fetched experiment history",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project key or experiment id",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<GetExperimentHistoryResponse>> handle(
      @BeanParam @Valid ExperimentHistoryRequest request) {

    return adminService
        .getExperimentHistory(
            request.getProjectKey(),
            request.getExperimentId(),
            request.getLimit(),
            request.getPage())
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
