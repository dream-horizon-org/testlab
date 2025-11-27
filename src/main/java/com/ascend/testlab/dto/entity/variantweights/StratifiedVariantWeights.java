package com.ascend.testlab.dto.entity.variantweights;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Manual/Stratified variant weights where variants are mapped to cohorts. Used when assignment
 * domain is STRATIFIED.
 *
 * <p>Format: Map of variant name to list of cohorts
 *
 * <p>Example: { "control": ["premium_users", "beta_testers"], "treatment": ["free_users"] }
 *
 * <p>If a user belongs to any cohort mapped to a variant, they get assigned that variant.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StratifiedVariantWeights extends VariantWeights {

  private Map<String, List<String>> weights;

  @Override
  public AssignmentDomain getType() {
    return AssignmentDomain.STRATIFIED;
  }

  /**
   * Gets the variant name for a user based on their cohorts. Returns the first variant that matches
   * any of the user's cohorts.
   *
   * @param userCohorts list of cohorts the user belongs to
   * @return variant name if user belongs to any cohort mapped to a variant, null otherwise
   */
  public String getVariantForCohorts(List<String> userCohorts) {
    if (weights == null || userCohorts == null || userCohorts.isEmpty()) {
      return null;
    }

    for (Map.Entry<String, List<String>> entry : weights.entrySet()) {
      String variantName = entry.getKey();
      List<String> variantCohorts = entry.getValue();

      if (!variantCohorts.isEmpty()) {
        for (String userCohort : userCohorts) {
          if (variantCohorts.contains(userCohort)) {
            return variantName;
          }
        }
      }
    }
    return null;
  }
}
