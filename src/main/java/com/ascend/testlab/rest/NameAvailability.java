package com.ascend.testlab.rest;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.NameAvailabilityResponse;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.AdminService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

/**
 * REST endpoint for validating experiment name availability for a specific project.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminService
 * @see NameAvailabilityResponse
 */
@Path("/v1/experiments/name-availability")
public class NameAvailability {

  /** The admin service. */
  private final AdminService adminService;

  /**
   * Constructor for the NameAvailability class.
   *
   * @param adminService the admin service
   */
  @Inject
  public NameAvailability(AdminService adminService) {
    this.adminService = adminService;
  }

  /**
   * Handles the GET request to validate experiment name availability for a project.
   *
   * @param projectKey the project key provided in the x-project-key header
   * @param experimentName the experiment name provided in the name query parameter
   * @return a CompletionStage containing a ResponseEntity with availability and message
   * @throws jakarta.validation.ConstraintViolationException if inputs are blank
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully validated experiment name",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project key or experiment name",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<NameAvailabilityResponse>> handle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @QueryParam(WebConstants.NAME)
          @NotBlank(message = ErrorMessages.EXPERIMENT_NAME_MISSING)
          @Size(
              max = Constants.MAX_EXPERIMENT_NAME_LENGTH,
              message = ErrorMessages.EXPERIMENT_NAME_TOO_LONG)
          String experimentName) {

    return adminService
        .isExperimentNameAvailable(projectKey, experimentName)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
