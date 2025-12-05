package com.ascend.testlab.dto.entity.allocation;

import com.ascend.testlab.dto.entity.experiment.Variant;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserExperimentMap {
  private UUID experimentId;

  private String experimentName;

  private String experimentKey;

  private String status;
  private Variant variant;

  private String variantName;

  private Long assignedAt;
}
