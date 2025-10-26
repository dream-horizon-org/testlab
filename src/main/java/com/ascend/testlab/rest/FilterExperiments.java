package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.service.ExperimentService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

@Path("/v1/experiment")
@Slf4j
public class FilterExperiments {

  @Inject private ExperimentService experimentService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<ResponseEntity.Success<List<Experiment>>> handle(
      @HeaderParam("x-project-id") @NotNull String projectId,
      @QueryParam("status") String status,
      @QueryParam("tag") String tag,
      @QueryParam("owner") String owner,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("limit") Integer limit,
      @QueryParam("page") Integer page) {

    FilterExperimentsRequest request = new FilterExperimentsRequest();
    request.buildRequest(status, tag, owner, name, type, limit, page);
    
    return experimentService
        .filterExperiments(projectId, request)
        .map(experiments -> {
          log.info("Successfully fetched {} experiments for projectId: {}", experiments.size(), projectId);
          return new ResponseEntity.Success<>(experiments);
        })
        .toCompletionStage();
  }
}
