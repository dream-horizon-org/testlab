package com.ascend.testlab.dto.response;

import com.ascend.testlab.entity.Variant;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing a user's experiment allocation mapping.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExperimentMap {
  @JsonProperty(value = "experiment_id")
  private UUID experimentId;

  @JsonProperty(value = "experiment_name")
  private String experimentName;

  private String status;
  private Variant variant;

  @JsonProperty(value = "variant_name")
  private String variantName;

  @JsonProperty(value = "assigned_at")
  private Long assignedAt;
}
