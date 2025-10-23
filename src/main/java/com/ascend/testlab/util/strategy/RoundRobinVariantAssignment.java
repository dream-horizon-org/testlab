package com.ascend.testlab.util.strategy;

import com.ascend.testlab.entity.Variant;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Round-robin variant assignment strategy
 * Assigns variants based on their current count to maintain balance according to percentage distribution
 */
@Slf4j
public class RoundRobinVariantAssignment implements VariantAssignmentStrategy {

  @Override
  public Variant selectVariant(List<Variant> variants, String userId) {
    if (variants == null || variants.isEmpty()) {
      log.warn("No variants available for assignment");
      return null;
    }

    // Calculate target ratio for each variant
    long totalCount = variants.stream()
        .mapToLong(v -> v.getCurrentCount() != null ? v.getCurrentCount() : 0L)
        .sum();

    Variant selected = variants.stream()
        .min(Comparator.comparingDouble(v -> calculateVariantRatio(v, totalCount)))
        .orElse(variants.get(0));

    log.debug("Selected variant {} for user {} using round-robin strategy", selected.getVariantName(), userId);
    return selected;
  }

  private double calculateVariantRatio(Variant variant, long totalCount) {
    long currentCount = variant.getCurrentCount() != null ? variant.getCurrentCount() : 0L;
    double targetPercentage = variant.getPercentage() / 100.0;

    if (totalCount == 0) {
      return 0.0;
    }

    double actualPercentage = (double) currentCount / totalCount;
    return actualPercentage / targetPercentage;
  }
}

