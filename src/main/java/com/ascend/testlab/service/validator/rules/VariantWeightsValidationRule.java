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
import java.util.HashSet;
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
 *   <li>For STRATIFIED type, no cohort can appear in multiple variants
 *   <li>For STRATIFIED type, cohorts cannot be removed in LIVE/PAUSED state
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

    if (request.getVariantWeights() != null && status != ExperimentStatus.DRAFT) {
      validateNoVariantRemovalFromWeights(
          existing.getVariantWeights(), request.getVariantWeights());

      validateNoStratifiedCohortRemoval(existing, request, status);
    }

    Map<String, Variant> effectiveVariants = getEffectiveVariants(existing, request);
    VariantWeights effectiveWeights = getEffectiveWeights(existing, request);

    if (effectiveVariants == null || effectiveWeights == null) {
      return;
    }

    Set<String> variantKeys = effectiveVariants.keySet();

    if (effectiveWeights instanceof CohortVariantWeights cohortWeights) {
      validateCohortWeights(variantKeys, cohortWeights);
    } else if (effectiveWeights instanceof StratifiedVariantWeights stratifiedWeights) {
      validateStratifiedWeights(variantKeys, stratifiedWeights);
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

    if (!variantKeys.equals(weightKeys)) {
      throw new RestException(ErrorEnum.VARIANT_WEIGHT_KEYS_MISMATCH);
    }

    double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
    if (Math.abs(sum - 100.0) > 0.01) {
      throw new RestException(ErrorEnum.VARIANT_WEIGHTS_SUM_NOT_100);
    }
  }

  /**
   * Validates STRATIFIED type weights:
   *
   * <ul>
   *   <li>Keys must match variant keys
   *   <li>No cohort can appear in multiple variants
   * </ul>
   */
  private void validateStratifiedWeights(
      Set<String> variantKeys, StratifiedVariantWeights stratifiedWeights) {

    Map<String, List<String>> weights = stratifiedWeights.getWeights();
    if (weights == null || weights.isEmpty()) {
      return;
    }

    Set<String> weightKeys = weights.keySet();
    if (!variantKeys.equals(weightKeys)) {
      throw new RestException(ErrorEnum.VARIANT_WEIGHT_KEYS_MISMATCH);
    }

    validateNoDuplicateCohortAcrossVariants(weights);
  }

  /** Validates that no cohort appears in more than one variant's list. */
  private void validateNoDuplicateCohortAcrossVariants(Map<String, List<String>> weights) {
    Set<String> seenCohorts = new HashSet<>();
    Set<String> duplicates = new HashSet<>();

    for (List<String> cohorts : weights.values()) {
      if (cohorts == null) continue;
      for (String cohort : cohorts) {
        if (!seenCohorts.add(cohort)) {
          duplicates.add(cohort);
        }
      }
    }

    if (!duplicates.isEmpty()) {
      throw new RestException(
          ErrorEnum.DUPLICATE_COHORT_ACROSS_VARIANTS.getErrorCode(),
          String.format(ErrorEnum.DUPLICATE_COHORT_ACROSS_VARIANTS.getErrorMessage(), duplicates),
          ErrorEnum.DUPLICATE_COHORT_ACROSS_VARIANTS.getHttpStatusCode());
    }
  }

  /**
   * Validates that cohorts are not removed from stratified weights in LIVE/PAUSED state.
   *
   * @param existing the existing experiment
   * @param request the update request
   * @param status the current experiment status
   */
  private void validateNoStratifiedCohortRemoval(
      Experiment existing, UpdateExperimentRequest request, ExperimentStatus status) {

    if (status != ExperimentStatus.LIVE && status != ExperimentStatus.PAUSED) {
      return;
    }

    if (!(existing.getVariantWeights() instanceof StratifiedVariantWeights existingWeights)) {
      return;
    }
    if (!(request.getVariantWeights() instanceof StratifiedVariantWeights requestWeights)) {
      return;
    }

    Set<String> removedCohorts =
        findRemovedCohorts(existingWeights.getWeights(), requestWeights.getWeights());

    if (!removedCohorts.isEmpty()) {
      throw new RestException(
          ErrorEnum.STRATIFIED_COHORT_REMOVAL_NOT_ALLOWED.getErrorCode(),
          String.format(
              ErrorEnum.STRATIFIED_COHORT_REMOVAL_NOT_ALLOWED.getErrorMessage(), removedCohorts),
          ErrorEnum.STRATIFIED_COHORT_REMOVAL_NOT_ALLOWED.getHttpStatusCode());
    }
  }

  /** Finds cohorts that were removed from stratified weights during an update. */
  private Set<String> findRemovedCohorts(
      Map<String, List<String>> existingWeights, Map<String, List<String>> requestWeights) {

    Set<String> existingCohorts = collectAllCohorts(existingWeights);
    Set<String> requestCohorts = collectAllCohorts(requestWeights);

    existingCohorts.removeAll(requestCohorts);
    return existingCohorts;
  }

  /** Collects all cohorts from all variants into a single set. */
  private Set<String> collectAllCohorts(Map<String, List<String>> variantCohortMap) {
    Set<String> allCohorts = new HashSet<>();
    if (variantCohortMap == null) return allCohorts;

    for (List<String> cohorts : variantCohortMap.values()) {
      if (cohorts != null) {
        allCohorts.addAll(cohorts);
      }
    }
    return allCohorts;
  }
}
