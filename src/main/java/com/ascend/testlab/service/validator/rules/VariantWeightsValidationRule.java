package com.ascend.testlab.service.validator.rules;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates variant weights constraints.
 *
 * <ul>
 *   <li>Variant weights keys must match variant keys
 *   <li>Existing variants cannot be removed from weights (except in DRAFT)
 *   <li>For COHORT type, weights must sum to 100
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
public class VariantWeightsValidationRule implements UpdateValidationRule {

  /** {@inheritDoc} */
  @Override
  public boolean appliesTo(Experiment existing, UpdateExperimentRequest request) {
    return request.getVariantWeights() != null || request.getVariants() != null;
  }

  /** {@inheritDoc} */
  @Override
  public void validate(Experiment existing, UpdateExperimentRequest request) {
    ExperimentStatus status = existing.getStatus();

    // Validate no variant removal from weights (except in DRAFT)
    if (request.getVariantWeights() != null && status != ExperimentStatus.DRAFT) {
      validateNoVariantRemovalFromWeights(
          existing.getVariantWeights(), request.getVariantWeights());
    }

    // Get effective variants and weights for key matching validation
    Map<String, Variant> effectiveVariants = getEffectiveVariants(existing, request);
    VariantWeights effectiveWeights = getEffectiveWeights(existing, request);

    if (effectiveVariants == null || effectiveWeights == null) {
      return;
    }

    Set<String> variantKeys = effectiveVariants.keySet();

    if (effectiveWeights instanceof CohortVariantWeights cohortWeights) {
      validateCohortWeights(variantKeys, cohortWeights);
    } else if (effectiveWeights instanceof StratifiedVariantWeights stratifiedWeights) {
      validateStratifiedWeightsKeys(variantKeys, stratifiedWeights);
    }
  }

  /**
   * Gets effective variants considering the request. If request has variants, use request variants.
   * Otherwise use existing variants.
   */
  private Map<String, Variant> getEffectiveVariants(
      Experiment existing, UpdateExperimentRequest request) {
    if (request.getVariants() != null && !request.getVariants().isEmpty()) {
      return request.getVariants();
    }
    return existing.getVariants();
  }

  /**
   * Gets effective weights considering the request. If request has weights, use request weights.
   * Otherwise use existing weights.
   */
  private VariantWeights getEffectiveWeights(Experiment existing, UpdateExperimentRequest request) {
    if (request.getVariantWeights() != null) {
      return request.getVariantWeights();
    }
    return existing.getVariantWeights();
  }

  /** Validates that no variants are removed from variant_weights. */
  private void validateNoVariantRemovalFromWeights(
      VariantWeights existingWeights, VariantWeights requestWeights) {

    if (existingWeights == null || requestWeights == null) {
      return;
    }

    Set<String> existingKeys = getVariantWeightKeys(existingWeights);
    Set<String> requestKeys = getVariantWeightKeys(requestWeights);

    for (String existingKey : existingKeys) {
      if (!requestKeys.contains(existingKey)) {
        throw new RestException(ErrorEnum.VARIANT_WEIGHT_REMOVAL_NOT_ALLOWED);
      }
    }
  }

  /** Gets the variant keys from variant weights. */
  private Set<String> getVariantWeightKeys(VariantWeights weights) {
    if (weights instanceof CohortVariantWeights cohortWeights) {
      return cohortWeights.getWeights() != null ? cohortWeights.getWeights().keySet() : Set.of();
    } else if (weights instanceof StratifiedVariantWeights stratifiedWeights) {
      return stratifiedWeights.getWeights() != null
          ? stratifiedWeights.getWeights().keySet()
          : Set.of();
    }
    return Set.of();
  }

  /** Validates COHORT type weights: - Keys must match variant keys - Weights must sum to 100 */
  private void validateCohortWeights(Set<String> variantKeys, CohortVariantWeights cohortWeights) {

    Map<String, Double> weights = cohortWeights.getWeights();
    if (weights == null || weights.isEmpty()) {
      return;
    }

    Set<String> weightKeys = weights.keySet();

    // Validate keys match
    if (!variantKeys.equals(weightKeys)) {
      throw new RestException(ErrorEnum.VARIANT_WEIGHT_KEYS_MISMATCH);
    }

    // Validate sum equals 100
    double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
    if (Math.abs(sum - 100.0) > 0.01) { // Allow small floating point tolerance
      throw new RestException(ErrorEnum.VARIANT_WEIGHTS_SUM_NOT_100);
    }
  }

  /** Validates STRATIFIED type weights: - Keys must match variant keys */
  private void validateStratifiedWeightsKeys(
      Set<String> variantKeys, StratifiedVariantWeights stratifiedWeights) {

    Map<String, List<String>> weights = stratifiedWeights.getWeights();
    if (weights == null || weights.isEmpty()) {
      return;
    }

    Set<String> weightKeys = weights.keySet();

    if (!variantKeys.equals(weightKeys)) {
      throw new RestException(ErrorEnum.VARIANT_WEIGHT_KEYS_MISMATCH);
    }
  }
}
