package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
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
 * REST endpoint for deleting experiments. Handles DELETE requests to remove an experiment from the
 * system.
 *
 * <p>Deletes an experiment by its ID within a specified project. The deletion process removes the
 * experiment record, associated tags, and owner mappings, and logs the deletion in the experiment
 * log table for audit purposes.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentService
 */
@Path("/v1")
@Slf4j
public class DeleteExperiment {

  private final ExperimentService experimentService;

  /**
   * Constructor for DeleteExperiment.
   *
   * @param experimentService the experiment service to use for deleting experiment data
   */
  @Inject
  public DeleteExperiment(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  /**
   * Handles DELETE request to delete an experiment by experiment ID.
   *
   * <p>Deletes a single experiment based on the provided project Key and experiment ID. The project
   * key is passed as a header parameter, while the experiment ID is passed as a query parameter.
   *
   * <p>The deletion process performs the following operations in a transaction:
   *
   * <ul>
   *   <li>Deletes the experiment record from the experiments table
   *   <li>Removes associated tags from the experiment_tags table
   *   <li>Removes owner mappings from the experiment_owners table
   *   <li>Logs the deletion with previous experiment data in the experiment_update_log table
   * </ul>
   *
   * @param projectKey the project Key (required, passed as header parameter "x-project-key")
   * @param experimentId the experiment ID (required, passed as query parameter "experimentId")
   * @return a CompletionStage containing a successful response with DeleteExperimentResponse
   *     containing deletion details, or a failure response if the project key is missing, the
   *     experiment ID is invalid, or the experiment does not exist
   */
  @DELETE
  @Path("/experiments/{experiment_id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)),
      responseCode = "200",
      description = "Successful Response")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "400",
      description = "Project id is missing or Experiment id is invalid")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "404",
      description = "Experiment not found")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "500",
      description = "Internal Server Error")
  public CompletionStage<ResponseEntity.Success<Boolean>> handle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @PathParam(WebConstants.EXPERIMENT_ID)
          @NotBlank(message = ErrorMessages.EXPERIMENT_ID_MISSING)
          String experimentId) {
    return experimentService
        .deleteExperiment(projectKey, experimentId)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
