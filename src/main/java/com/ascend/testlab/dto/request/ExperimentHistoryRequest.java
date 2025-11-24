package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for fetching experiment history with pagination support.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentHistoryRequest {

  @HeaderParam(WebConstants.PROJECT_KEY_HEADER)
  @NotBlank(message = ErrorMessages.PROJECT_KEY_MISSING)
  private String projectKey;

  @PathParam(WebConstants.EXPERIMENT_ID)
  @NotBlank(message = ErrorMessages.EXPERIMENT_ID_MISSING)
  private String experimentId;

  @QueryParam(WebConstants.LIMIT)
  @DefaultValue(WebConstants.DEFAULT_LIMIT)
  @Positive(message = ErrorMessages.INVALID_LIMIT_VALUE)
  @Builder.Default
  private Integer limit = Integer.valueOf(WebConstants.DEFAULT_LIMIT);

  @QueryParam(WebConstants.PAGE)
  @DefaultValue(WebConstants.DEFAULT_PAGE)
  @Positive(message = ErrorMessages.INVALID_PAGE_VALUE)
  @Builder.Default
  private Integer page = Integer.valueOf(WebConstants.DEFAULT_PAGE);
}
