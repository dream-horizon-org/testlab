package com.ascend.testlab.dto.response;

import com.ascend.testlab.entity.Variant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing a user's experiment allocation mapping.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExperimentMap {
  private UUID experimentId;
  private String experimentName;
  private String status;
  private Variant variant;
  private String variantName;
  private Long assignedAt;
}
