package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * Utility class for merging experiment updates with existing state.
 *
 * <p>Implements PATCH semantics where new items are added and existing items are updated.
 */
@UtilityClass
public class ExperimentMergeUtil {

  /**
   * Merges the update request into the existing experiment.
   *
   * <p>Only non-null fields from request are applied. Immutable fields (experimentId, projectKey,
   * type, distributionStrategy, assignmentDomain, createdBy, createdAt) are preserved from
   * existing.
   *
   * @param existing the existing experiment
   * @param request the update request
   * @return merged experiment
   */
  public static Experiment merge(Experiment existing, UpdateExperimentRequest request) {
    return Experiment.builder()
        .experimentId(existing.getExperimentId())
        .projectKey(existing.getProjectKey())
        .type(existing.getType())
        .distributionStrategy(existing.getDistributionStrategy())
        .assignmentDomain(existing.getAssignmentDomain())
        .createdBy(existing.getCreatedBy())
        .createdAt(existing.getCreatedAt())
        .name(orDefault(request.getName(), existing.getName()))
        .experimentKey(orDefault(request.getExperimentKey(), existing.getExperimentKey()))
        .description(orDefault(request.getDescription(), existing.getDescription()))
        .hypothesis(orDefault(request.getHypothesis(), existing.getHypothesis()))
        .status(parseStatus(request.getStatus(), existing.getStatus()))
        .guardrailHealthStatus(
            parseHealthStatus(
                request.getGuardrailHealthStatus(), existing.getGuardrailHealthStatus()))
        .exposure(orDefault(request.getExposure(), existing.getExposure()))
        .threshold(orDefault(request.getThreshold(), existing.getThreshold()))
        .startTime(orDefault(request.getStartTime(), existing.getStartTime()))
        .endTime(orDefault(request.getEndTime(), existing.getEndTime()))
        .updatedBy(orDefault(request.getUpdatedBy(), Constants.SYSTEM))
        .winningVariant(orDefault(request.getWinningVariant(), existing.getWinningVariant()))
        .overrides(mergeOverrides(existing.getOverrides(), request.getOverrides()))
        .cohorts(orDefault(request.getCohorts(), existing.getCohorts()))
        .tags(orDefault(request.getTags(), existing.getTags()))
        .owners(orDefault(request.getOwners(), existing.getOwners()))
        .metrics(orDefault(request.getMetrics(), existing.getMetrics()))
        .variants(mergeVariants(existing.getVariants(), request.getVariants()))
        .variantWeights(
            mergeVariantWeights(existing.getVariantWeights(), request.getVariantWeights()))
        .ruleAttributes(
            mergeRuleAttributes(
                existing.getStatus(), existing.getRuleAttributes(), request.getRuleAttributes()))
        .build();
  }

  private static ExperimentStatus parseStatus(String status, ExperimentStatus defaultStatus) {
    return status != null ? ExperimentStatus.fromValue(status) : defaultStatus;
  }

  private static HealthStatus parseHealthStatus(String status, HealthStatus defaultStatus) {
    return status != null ? HealthStatus.valueOf(status) : defaultStatus;
  }

  private static <T> T orDefault(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }

  /**
   * Merges new rule attributes with existing rules.
   *
   * <p>In DRAFT mode: Override - use request rules directly (replace entire list). In LIVE/PAUSED
   * mode: No add/delete allowed - if same rules, use new values; otherwise throw error.
   *
   * @param status the current experiment status
   * @param existing existing rule attributes
   * @param newRules new rule attributes from request
   * @return merged or replaced rule attributes
   */
  public static List<RuleAttributes> mergeRuleAttributes(
      ExperimentStatus status, List<RuleAttributes> existing, List<RuleAttributes> newRules) {
    if (newRules == null) return existing;
    if (existing == null) return newRules;

    if (status == ExperimentStatus.DRAFT) {
      return newRules;
    }

    Set<String> existingNames = new HashSet<>();
    for (RuleAttributes rule : existing) {
      existingNames.add(rule.getName());
    }

    Set<String> newNames = new HashSet<>();
    for (RuleAttributes rule : newRules) {
      newNames.add(rule.getName());
    }

    for (String existingName : existingNames) {
      if (!newNames.contains(existingName)) {
        throw new RestException(ErrorEnum.RULE_REMOVAL_NOT_ALLOWED);
      }
    }

    for (String newName : newNames) {
      if (!existingNames.contains(newName)) {
        throw new RestException(ErrorEnum.RULE_ADDITION_NOT_ALLOWED);
      }
    }

    return newRules;
  }

  /**
   * Merges new variants with existing variants.
   *
   * @param existing the existing variants map
   * @param newVariants the new variants to merge
   * @return merged variants map, or existing if newVariants is null
   */
  public static Map<String, Variant> mergeVariants(
      Map<String, Variant> existing, Map<String, Variant> newVariants) {
    if (newVariants == null) return existing;
    if (existing == null) return newVariants;

    Map<String, Variant> merged = new HashMap<>(existing);
    merged.putAll(newVariants);
    return merged;
  }

  /**
   * Merges new variant weights with existing weights.
   *
   * @param existing the existing variant weights
   * @param newWeights the new weights to merge
   * @return merged variant weights, or existing if newWeights is null
   */
  public static VariantWeights mergeVariantWeights(
      VariantWeights existing, VariantWeights newWeights) {
    if (newWeights == null) return existing;
    if (existing == null) return newWeights;
    if (!existing.getClass().equals(newWeights.getClass())) return newWeights;

    if (existing instanceof CohortVariantWeights) {
      return mergeCohortWeights((CohortVariantWeights) existing, (CohortVariantWeights) newWeights);
    } else if (existing instanceof StratifiedVariantWeights) {
      return mergeStratifiedWeights(
          (StratifiedVariantWeights) existing, (StratifiedVariantWeights) newWeights);
    }
    return newWeights;
  }

  private static CohortVariantWeights mergeCohortWeights(
      CohortVariantWeights existing, CohortVariantWeights newWeights) {
    Map<String, Double> merged = new HashMap<>();
    if (existing.getWeights() != null) merged.putAll(existing.getWeights());
    if (newWeights.getWeights() != null) merged.putAll(newWeights.getWeights());
    return CohortVariantWeights.builder().weights(merged).build();
  }

  private static StratifiedVariantWeights mergeStratifiedWeights(
      StratifiedVariantWeights existing, StratifiedVariantWeights newWeights) {
    Map<String, List<String>> merged = new HashMap<>();
    if (existing.getWeights() != null) merged.putAll(existing.getWeights());
    if (newWeights.getWeights() != null) merged.putAll(newWeights.getWeights());
    return StratifiedVariantWeights.builder().weights(merged).build();
  }

  /**
   * Merges override updates with existing overrides.
   *
   * @param existing existing overrides map
   * @param requestOverrides new overrides from request
   * @return merged overrides map
   */
  private static Map<String, List<String>> mergeOverrides(
      Map<String, List<String>> existing, Overrides requestOverrides) {
    if (requestOverrides == null || requestOverrides.isEmpty()) {
      return existing;
    }
    // When overrides are provided in request, replace entirely with new values
    return requestOverrides.getOverrideIds();
  }
}
