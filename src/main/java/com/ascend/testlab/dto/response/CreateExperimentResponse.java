package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for experiment creation.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExperimentResponse {

  @JsonProperty("experiment_id")
  private UUID experimentId;

  @JsonProperty("status")
  private boolean status;

  @JsonProperty("message")
  private String message;
}
