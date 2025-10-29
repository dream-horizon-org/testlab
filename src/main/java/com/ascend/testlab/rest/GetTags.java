package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.service.TagsService;
import com.ascend.testlab.util.CommonUtil;
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
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoint for retrieving experiment tags for a specific project.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsService
 * @see TagsResponse
 */
@Path("/v1/experiments/tags")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GetTags {

  private final TagsService tagsService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully retrieved tags",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project ID",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  /**
   * Handles the GET request to retrieve all distinct tags for a specific project.
   *
   * @param projectId the project ID provided in the x-project-id header
   * @return a CompletionStage containing a ResponseEntity with the list of tags
   * @throws jakarta.validation.ConstraintViolationException if projectId is blank
   * @throws com.dream11.rest.exception.RestException if projectId is invalid or other errors occur
   */
  public CompletionStage<ResponseEntity.Success<TagsResponse>> handle(
      @HeaderParam("x-project-id") @NotBlank(message = "x-project-id header is required")
          String projectId) {

    CommonUtil.validateProjectId(projectId);
    return tagsService
        .getTags(UUID.fromString(projectId))
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
