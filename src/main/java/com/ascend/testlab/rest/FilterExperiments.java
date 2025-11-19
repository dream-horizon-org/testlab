package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.ExperimentService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
@Path(WebConstants.FILTER_EXPERIMENTS_PATH)
@Slf4j
public class FilterExperiments {

  private final ExperimentService experimentService;

  /**
   * Constructor for FilterExperiments.
   *
   * @param experimentService the experiment service to use for filtering experiments
   */
  @Inject
  public FilterExperiments(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  /**
   * Supports filtering by multiple criteria including status, tag, owner, name, and type. Also
   * supports pagination through limit and page parameters. All filters are optional, allowing for
   * flexible querying of experiments.
   *
   * <p>Multiple filter values can be provided as comma-separated strings for status, type, tag, and
   * owner parameters. The name filter supports text search and does not support comma-separated
   * values.
   *
   * <p>Pagination defaults to limit=20 and page=1 if not specified. Page numbers start at 1.
   *
   * @param projectKey the project Key (required, passed as header parameter "x-project-key")
   * @param request the filter request containing optional query parameters for status, tag, owner,
   *     name, type, limit, and page
   * @return a CompletionStage containing a successful response with paginated experiment data
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
      description = "Project Key is missing")
  @ApiResponse(
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)),
      responseCode = "500",
      description = "Internal Server Error")
  public CompletionStage<ResponseEntity.Success<FilterExperimentsResponse>> filterExperimentsHandle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @BeanParam @Valid FilterExperimentsRequest request) {

    return experimentService
        .filterExperiments(projectKey, request)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
