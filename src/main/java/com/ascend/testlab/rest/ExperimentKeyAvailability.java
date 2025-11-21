package com.ascend.testlab.rest;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.ExperimentKeyAvailabilityResponse;
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
 * REST endpoint for validating experiment key availability for a specific project.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminService
 * @see ExperimentKeyAvailabilityResponse
 */
@Path("/v1/experiments/key-availability")
public class ExperimentKeyAvailability {

  /** The admin service. */
  private final AdminService adminService;

  /**
   * Constructor for the ExperimentKeyAvailability class.
   *
   * @param adminService the admin service
   */
  @Inject
  public ExperimentKeyAvailability(AdminService adminService) {
    this.adminService = adminService;
  }

  /**
   * Handles the GET request to validate experiment key availability for a project.
   *
   * @param projectKey the project key provided in the x-project-key header
   * @param experimentKey the experiment key provided in the experimentKey query parameter
   * @return a CompletionStage containing a ResponseEntity with availability and message
   * @throws jakarta.validation.ConstraintViolationException if inputs are blank
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully validated experiment key",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project key or experiment key",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<ExperimentKeyAvailabilityResponse>>
      experimentKeyAvailabilityHandle(
          @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
              @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
              String projectKey,
          @QueryParam(WebConstants.EXPERIMENT_KEY)
              @NotBlank(message = ErrorMessages.EXPERIMENT_KEY_MISSING)
              @Size(
                  max = Constants.MAX_EXPERIMENT_KEY_LENGTH,
                  message = ErrorMessages.EXPERIMENT_KEY_TOO_LONG)
              String experimentKey) {

    return adminService
        .isExperimentKeyAvailable(projectKey, experimentKey)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
