package com.ascend.testlab.variantWeights;

import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.validation.annotations.ValidWeightSum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Cohort-based variant weights where cohorts are mapped to specific variants with weights. Used
 * when assignment domain is COHORT.
 *
 * <p>Format: Map of cohort name to weight (percentage)
 *
 * <p>Example: { "premium_users": 70, "free_users": 30 }
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
@ValidWeightSum
public class CohortVariantWeights extends VariantWeights {

  @NotNull(message = "Weights map is required for cohort variant weights")
  @NotEmpty(message = "At least one cohort weight must be specified")
  private Map<String, Double> weights;

  @Override
  public AssignmentDomain getType() {
    return AssignmentDomain.COHORT;
  }
}
