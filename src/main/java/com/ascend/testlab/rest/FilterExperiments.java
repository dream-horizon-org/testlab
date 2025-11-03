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

/**
 * REST endpoint for filtering and retrieving experiments with pagination support. Handles GET
 * requests to fetch a list of experiments based on various filter criteria such as status, tag,
 * owner, name, and type.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentService
 */
@Path(Constants.FILTER_EXPERIMENTS_PATH)
@Slf4j
public class FilterExperiments {

  @Inject private ExperimentService experimentService;

  /**
   * Supports filtering by multiple criteria including status, tag, owner, name, and type. Also
   * supports pagination through limit and page parameters. All filters are optional, allowing for
   * flexible querying of experiments.
   *
   * @param projectId the project ID (required, passed as header parameter "x-project-id")
   * @param status optional filter for experiment status (e.g., LIVE, PAUSED, DRAFT, CONCLUDED,
   *     TERMINATED)
   * @param tag optional filter for experiment tags
   * @param owner optional filter for experiment owner
   * @param name optional filter for experiment name (supports text search)
   * @param type optional filter for experiment type (e.g., A/B)
   * @param limit optional parameter to limit the number of results per page
   * @param page optional parameter to specify the page number for pagination
   * @return a CompletionStage containing a successful response with paginated experiment data
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<ResponseEntity.Success<FilterExperimentsResponse>> handle(
      @HeaderParam(Constants.PROJECT_ID) @NotBlank(message = "Project id is mandatory")
          String projectId,
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
        .map(
            paginatedResponse -> {
              log.info(
                  "Successfully fetched {} experiments (page {}) for projectId: {}",
                  paginatedResponse.getExperimentList().size(),
                  paginatedResponse.getPagination().getCurrentPage(),
                  projectId);
              return new ResponseEntity.Success<>(paginatedResponse);
            })
        .toCompletionStage();
  }
}
