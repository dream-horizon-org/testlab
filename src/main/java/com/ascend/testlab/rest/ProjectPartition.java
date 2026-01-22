package com.ascend.testlab.rest;

import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.request.PartitionRequest;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.ascend.testlab.service.PartitionService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectPartition {

  private final PartitionService partitionService;

  @Inject
  public ProjectPartition(PartitionService partitionService) {
    this.partitionService = partitionService;
  }

  @POST
  @Path("/partitions")
  @ApiResponse(
      responseCode = "200",
      description = "Successfully created partitions for experiment tables",
      useReturnTypeSchema = true)
  @ApiResponse(responseCode = "304", description = "Partitions already exist (idempotent)")
  @ApiResponse(
      responseCode = "400",
      description = "Bad Request due to invalid/missing body params / header",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid project-key",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<PartitionResponse>> createProjectPartitions(
      @HeaderParam("x-project-key") String projectKey, @Valid @NotNull PartitionRequest request) {

    return partitionService
        .createProjectPartition(request)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
