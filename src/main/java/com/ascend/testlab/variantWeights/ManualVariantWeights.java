package com.ascend.testlab.variantWeights;

import com.ascend.testlab.entity.AssignmentDomain;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Manual variant weights where specific users are mapped to specific variants. Used when assignment
 * domain is MANUAL.
 *
 * <p>Format: Map of variant name to list of user IDs
 *
 * <p>Example: { "control": ["user1", "user2"], "treatment": ["user3", "user4"] }
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManualVariantWeights extends VariantWeights {

  @NotNull(message = "Weights map is required for manual variant weights")
  @NotEmpty(message = "At least one variant with user assignments must be specified")
  private Map<String, List<String>> weights;

  @Override
  public AssignmentDomain getType() {
    return AssignmentDomain.MANUAL;
  }

  /**
   * Gets the variant name for a specific user ID
   *
   * @param userId the user identifier
   * @return variant name if user is in manual mapping, null otherwise
   */
  public String getVariantForUser(String userId) {
    if (weights == null || userId == null) {
      return null;
    }

    for (Map.Entry<String, List<String>> entry : weights.entrySet()) {
      if (entry.getValue() != null && entry.getValue().contains(userId)) {
        return entry.getKey();
      }
    }
    return null;
  }
}
