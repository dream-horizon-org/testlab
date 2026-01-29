package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.PartitionResponse;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.service.PartitionService;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

/**
 * REST endpoint for creating database partitions for a project. Handles POST requests to create
 * PostgreSQL partitions for all partitioned tables.
 *
 * <p>Creates PostgreSQL list partitions for the following tables: experiments, owners, tags,
 * experiment_update_log, and experiment_analysis. The operation is idempotent - calling it multiple
 * times with the same project key will return success without creating duplicate partitions.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see PartitionService
 */
@Path("/v1")
public class ProjectPartition {

  private final PartitionService partitionService;

  /**
   * Constructor for ProjectPartition.
   *
   * @param partitionService the partition service
   */
  @Inject
  public ProjectPartition(PartitionService partitionService) {
    this.partitionService = partitionService;
  }

  /**
   * Handles POST request to create partitions for all experiment tables for a given project key.
   *
   * <p>This endpoint creates PostgreSQL list partitions for the following tables:
   *
   * <ul>
   *   <li>experiments
   *   <li>owners
   *   <li>tags
   *   <li>experiment_update_log
   *   <li>experiment_analysis
   * </ul>
   *
   * <p>The operation is idempotent. If partitions already exist for the project key, the endpoint
   * returns success without creating duplicate partitions.
   *
   * @param projectKey the project key from x-project-key header
   * @param userId the user id from x-user-id header
   * @return a CompletionStage containing a ResponseEntity with partition creation status
   */
  @POST
  @Path("/partitions")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully created partitions for experiment tables",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Bad Request due to invalid/missing header",
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
      @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
          @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
          String projectKey,
      @HeaderParam(WebConstants.USER_ID_HEADER) String userId) {

    return partitionService
        .createProjectPartition(projectKey, userId)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }
}
