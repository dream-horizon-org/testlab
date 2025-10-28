package com.ascend.testlab.rest;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.service.ExperimentService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

@Path(Constants.FILTER_EXPERIMENTS_PATH)
@Slf4j
public class FilterExperiments {

  @Inject private ExperimentService experimentService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<ResponseEntity.Success<FilterExperimentsResponse>> handle(
      @HeaderParam(Constants.PROJECT_ID) @NotBlank(message = "Project id is mandatory") String projectId,
      @QueryParam(Constants.EXPERIMENT_STATUS) String status,
      @QueryParam(Constants.TAG) String tag,
      @QueryParam(Constants.OWNER) String owner,
      @QueryParam(Constants.NAME) String name,
      @QueryParam(Constants.EXPERIMENT_TYPE) String type,
      @QueryParam(Constants.LIMIT) Integer limit,
      @QueryParam(Constants.OFFSET) Integer page) {

    FilterExperimentsRequest request = new FilterExperimentsRequest();
    request.buildRequest(status, tag, owner, name, type, limit, page);
    
    return experimentService
        .filterExperiments(projectId, request)
        .map(paginatedResponse -> {
          log.info("Successfully fetched {} experiments (page {}) for projectId: {}",
              paginatedResponse.getExperimentList().size(),
              paginatedResponse.getPagination().getCurrentPage(),
              projectId);
          return new ResponseEntity.Success<>(paginatedResponse);
        })
        .toCompletionStage();
  }
}
