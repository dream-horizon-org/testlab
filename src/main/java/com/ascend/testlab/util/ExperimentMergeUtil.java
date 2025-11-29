package com.ascend.testlab.util;

import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
   * Merges new cohorts with existing cohorts.
   *
   * @param existing existing cohorts
   * @param newCohorts new cohorts to add
   * @return merged list of cohorts
   */
  public static List<String> mergeCohorts(List<String> existing, List<String> newCohorts) {
    if (newCohorts == null) {
      return existing;
    }
    if (existing == null) {
      return newCohorts;
    }

    Set<String> merged = new HashSet<>(existing);
    merged.addAll(newCohorts);
    return new ArrayList<>(merged);
  }

  /**
   * Merges new rule attributes with existing rules.
   *
   * <p>If a rule with the same name exists, it is replaced by the new rule (update). New rules are
   * added. Existing rules not present in the update are preserved.
   *
   * @param existing existing rules
   * @param newRules new rules to merge
   * @return merged list of rules
   */
  public static List<RuleAttributes> mergeRuleAttributes(
      List<RuleAttributes> existing, List<RuleAttributes> newRules) {
    if (newRules == null) {
      return existing;
    }
    if (existing == null) {
      return newRules;
    }

    Map<String, RuleAttributes> ruleMap = new LinkedHashMap<>();
    existing.forEach(r -> ruleMap.put(r.getName(), r));
    newRules.forEach(r -> ruleMap.put(r.getName(), r));

    return new ArrayList<>(ruleMap.values());
  }

  /**
   * Merges new variants with existing variants.
   *
   * @param existing existing variants map
   * @param newVariants new variants map
   * @return merged map of variants
   */
  public static Map<String, Variant> mergeVariants(
      Map<String, Variant> existing, Map<String, Variant> newVariants) {
    if (newVariants == null) {
      return existing;
    }
    if (existing == null) {
      return newVariants;
    }

    Map<String, Variant> merged = new HashMap<>(existing);
    merged.putAll(newVariants);
    return merged;
  }

  /**
   * Merges new variant weights with existing weights.
   *
   * @param existing existing weights
   * @param newWeights new weights
   * @return merged variant weights
   */
  public static VariantWeights mergeVariantWeights(
      VariantWeights existing, VariantWeights newWeights) {
    if (newWeights == null) {
      return existing;
    }
    if (existing == null) {
      return newWeights;
    }

    if (!existing.getClass().equals(newWeights.getClass())) {
      return newWeights;
    }

    if (existing instanceof CohortVariantWeights) {
      return mergeCohortVariantWeights(
          (CohortVariantWeights) existing, (CohortVariantWeights) newWeights);
    } else if (existing instanceof StratifiedVariantWeights) {
      return mergeStratifiedVariantWeights(
          (StratifiedVariantWeights) existing, (StratifiedVariantWeights) newWeights);
    }

    return newWeights;
  }

  private static CohortVariantWeights mergeCohortVariantWeights(
      CohortVariantWeights existing, CohortVariantWeights newWeights) {
    Map<String, Double> mergedMap = new HashMap<>();
    if (existing.getWeights() != null) {
      mergedMap.putAll(existing.getWeights());
    }
    if (newWeights.getWeights() != null) {
      mergedMap.putAll(newWeights.getWeights());
    }
    return CohortVariantWeights.builder().weights(mergedMap).build();
  }

  private static StratifiedVariantWeights mergeStratifiedVariantWeights(
      StratifiedVariantWeights existing, StratifiedVariantWeights newWeights) {
    Map<String, List<String>> mergedMap = new HashMap<>();
    if (existing.getWeights() != null) {
      mergedMap.putAll(existing.getWeights());
    }
    if (newWeights.getWeights() != null) {
      mergedMap.putAll(newWeights.getWeights());
    }
    return StratifiedVariantWeights.builder().weights(mergedMap).build();
  }
}
