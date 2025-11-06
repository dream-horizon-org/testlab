package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.ExperimentNameAvailabilityResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.ExperimentNameAvailabilityService;
import com.dream11.rest.util.ExceptionUtil;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
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
 * @see ExperimentNameAvailabilityService
 * @see ExperimentNameAvailabilityResponse
 */
@Path("/v1/experiments/name-availability")
public class ExperimentNameAvailability {

  /** The experiment name availability service. */
  private final ExperimentNameAvailabilityService experimentNameAvailabilityService;

  /**
   * Constructor for the ExperimentNameAvailability class.
   *
   * @param experimentNameAvailabilityService the service to check name availability
   */
  @Inject
  public ExperimentNameAvailability(
      ExperimentNameAvailabilityService experimentNameAvailabilityService) {
    this.experimentNameAvailabilityService = experimentNameAvailabilityService;
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
  public CompletionStage<ResponseEntity.Success<ExperimentNameAvailabilityResponse>> handle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @QueryParam(WebConstants.EXPERIMENT_NAME)
          @NotBlank(message = ErrorMessages.EXPERIMENT_NAME_MISSING)
          String experimentName) {

    validate(experimentName);

    return experimentNameAvailabilityService
        .isExperimentNameAvailable(projectKey, experimentName)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }

  private void validate(String name) {
    if (name.length() > 255) {
      throw ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NAME_TOO_LONG);
    }
  }
}
