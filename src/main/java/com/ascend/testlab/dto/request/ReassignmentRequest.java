package com.ascend.testlab.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request object for experiment variant reassignment. Used when changing a user's variant for an
 * already assigned experiment.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignmentRequest {

  @NotBlank(message = "Experiment ID is required")
  @NotNull
  private String experimentId;

  private String variantName;

  private String reason;

  private boolean forceReassign;

  @NotNull private String userId;
}
