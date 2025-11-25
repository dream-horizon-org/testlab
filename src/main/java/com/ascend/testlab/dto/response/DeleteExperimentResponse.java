package com.ascend.testlab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for delete experiment operation.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteExperimentResponse {
  /** Indicates if the deletion was successful. */
  private boolean success;

  /** The ID of the deleted experiment. */
  private String experimentId;
}
