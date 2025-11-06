package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.TagsResponse;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.TagsService;
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
import java.util.concurrent.CompletionStage;

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
public class Tags {

  /** The tags service. */
  private final TagsService tagsService;

  /**
   * Constructor for the Tags class.
   *
   * @param tagsService the tags service
   */
  @Inject
  public Tags(TagsService tagsService) {
    this.tagsService = tagsService;
  }

  /**
   * Handles the GET request to retrieve all distinct tags for a specific project.
   *
   * @param projectKey the project key provided in the x-project-key header
   * @return a CompletionStage containing a ResponseEntity with the list of tags
   * @throws jakarta.validation.ConstraintViolationException if projectKey is blank
   * @throws com.dream11.rest.exception.RestException if projectKey is invalid or other errors occur
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully retrieved tags",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project Key",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<TagsResponse>> getTagsHandle(
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(
              message =
                  ErrorMessages
                      .PROJECT_KEY_MISSING) // TODO: use default here & in compose, remove check
          String projectKey) {

    return tagsService.getTags(projectKey).map(ResponseEntity.Success::new).toCompletionStage();
  }
}
