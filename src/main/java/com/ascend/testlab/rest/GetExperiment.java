package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.ExperimentService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

/**
 * REST endpoint for retrieving experiment details by experiment ID. Handles GET requests to fetch a
 * specific experiment within a project.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentService
 */
@Path("/v1")
@Slf4j
public class GetExperiment {

  /** The experiment service for retrieving experiment data. */
  private final ExperimentService experimentService;

  /**
   * Constructor for GetExperiment.
   *
   * @param experimentService the experiment service to use for retrieving experiment data
   */
  @Inject
  public GetExperiment(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  /**
   * Handles GET request to retrieve experiment details by experiment ID.
   *
   * <p>Fetches a single experiment based on the provided project Key and experiment ID. The project
   * key is passed as a header parameter, while the experiment ID is passed as a path parameter.
   *
   * @param projectKey the project Key (required, passed as header parameter "x-project-key")
   * @param experimentId the experiment ID (required, passed as path parameter)
   * @return a CompletionStage containing a successful response with the experiment data, or a
   *     failure response if the experiment ID is invalid or the experiment is not found
   */
  @GET
  @Path("/experiments/{experimentId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successful Response",
      useReturnTypeSchema = true)
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "400",
      description = "Experiment Id is not valid")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "404",
      description = "Experiment not found")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "500",
      description = "Internal Server Error")
  public CompletionStage<ResponseEntity.Success<Experiment>> getExperimentHandle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @PathParam(WebConstants.EXPERIMENT_ID)
          @NotBlank(message = ErrorMessages.EXPERIMENT_ID_MISSING)
          String experimentId) {
    return experimentService
        .getExperiment(projectKey, experimentId)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
