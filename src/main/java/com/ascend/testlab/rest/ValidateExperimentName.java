package com.ascend.testlab.rest;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.dto.ResponseEntity;
import com.ascend.testlab.dto.response.ValidateExperimentNameResponse;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.service.ExperimentNameValidationService;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.util.ExceptionUtil;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;

@Path("/v1/experiment/validate-name")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ValidateExperimentName {

  private final ExperimentNameValidationService experimentNameValidationService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
      responseCode = "200",
      description = "Successfully validated experiment name",
      useReturnTypeSchema = true)
  @ApiResponse(
      responseCode = "400",
      description = "Invalid project ID or experiment name",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content = @Content(schema = @Schema(implementation = ResponseEntity.Failure.class)))
  public CompletionStage<ResponseEntity.Success<ValidateExperimentNameResponse>> handle(
      @HeaderParam(WebConstants.PROJECT_ID_HEADER) String projectId,
      @QueryParam("name") @NotBlank(message = "name query parameter is required") String name) {

    CommonUtil.validateProjectId(projectId);
    validate(name);

    return experimentNameValidationService
        .validateExperimentName(UUID.fromString(projectId), name)
        .map(ResponseEntity.Success::new)
        .toCompletionStage();
  }

  private void validate(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw ExceptionUtil.getException(ErrorEnum.INVALID_EXPERIMENT_NAME);
    }
    if (name.length() > 64) {
      throw ExceptionUtil.getException(ErrorEnum.EXPERIMENT_NAME_TOO_LONG);
    }
  }
}
