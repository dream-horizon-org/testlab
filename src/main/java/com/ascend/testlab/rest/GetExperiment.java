package com.ascend.testlab.rest;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.service.ExperimentService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
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
@Path(Constants.GET_EXPERIMENT_PATH)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GetExperiment {

  /** The experiment service for retrieving experiment data. */
  private final ExperimentService experimentService;

  /**
   * Handles GET request to retrieve experiment details by experiment ID.
   *
   * <p>Fetches a single experiment based on the provided project ID and experiment ID. The project
   * ID is passed as a header parameter, while the experiment ID is passed as a path parameter.
   *
   * @param projectId the project ID (required, passed as header parameter "x-project-id")
   * @param experimentId the experiment ID (required, passed as path parameter)
   * @return a CompletionStage containing a successful response with the experiment data, or a
   *     failure response if the experiment ID is invalid or the experiment is not found
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)),
      responseCode = "200",
      description = "Successful Response")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "400",
      description = "Experiment Id is not valid")
  public CompletionStage<ResponseEntity.Success<Experiment>> getExperimentHandler(
      @HeaderParam(Constants.PROJECT_ID) @NotBlank(message = "project id is mandatory")
          String projectId,
      @PathParam(Constants.EXPERIMENT_ID) String experimentId) {
    return experimentService
        .getExperiment(projectId, experimentId)
        .map(
            experiment -> {
              log.info("Successfully fetched experiment for experimentId: {}", experimentId);
              return new ResponseEntity.Success<>(experiment);
            })
        .toCompletionStage();
  }
}
