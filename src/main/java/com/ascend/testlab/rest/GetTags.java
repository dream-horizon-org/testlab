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

@Path("/v1/tags")
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
