package com.ascend.testlab.util.strategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * Random variant assignment strategy
 * Assigns variants based on their percentage distribution randomly
 */
@Slf4j
public class RandomVariantAssignment implements VariantAssignmentStrategy {

  private final Random random = new Random();

  @Override
  public Variant selectVariant(List<Variant> variants, String userId) {
    if (variants == null || variants.isEmpty()) {
      log.warn("No variants available for assignment");
      return null;
    }

    int totalPercentage = variants.stream()
        .mapToInt(Variant::getPercentage)
        .sum();

    int randomValue = random.nextInt(totalPercentage);
    int cumulativePercentage = 0;

    for (Variant variant : variants) {
      cumulativePercentage += variant.getPercentage();
      if (randomValue < cumulativePercentage) {
        log.debug("Selected variant {} for user {} using random strategy", variant.getVariantName(), userId);
        return variant;
      }
    }

    // Fallback to first variant
    log.warn("Fallback to first variant for user {}", userId);
    return variants.get(0);
  }
}

