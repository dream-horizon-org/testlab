package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.service.ExperimentService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

@Path("/v1/experiment/{experimentId}")
@Slf4j
public class GetExperiment {

  @Inject private ExperimentService experimentService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<ResponseEntity.Success<Experiment>> getExperimentHandler(
      @HeaderParam("x-project-id") @NotNull String projectId,
      @PathParam("experimentId") String experimentId) {
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
