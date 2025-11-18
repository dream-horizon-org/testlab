package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.ExperimentService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Path(WebConstants.DELETE_EXPERIMENT_PATH)
@Slf4j
public class DeleteExperiment {

  private final ExperimentService experimentService;

  @Inject
  public DeleteExperiment(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(
          content = @Content(schema = @Schema(implementation = ResponseEntity.Success.class)),
          responseCode = "200",
          description = "Successful Response")
  @ApiResponse(
          content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
          responseCode = "400",
          description = "Project id is missing")
  @ApiResponse(
          content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
          responseCode = "404",
          description = "Experiment does not exist")
  @ApiResponse(
          content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
          responseCode = "500",
          description = "Internal Server Error")
  public CompletionStage<ResponseEntity.Success<Boolean>> handle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @QueryParam(WebConstants.EXPERIMENT_ID) String experimentId) {
    return experimentService
        .deleteExperiment(projectKey, experimentId)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
