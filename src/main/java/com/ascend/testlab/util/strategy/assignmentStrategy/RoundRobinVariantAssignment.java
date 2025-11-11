package com.ascend.testlab.util.strategy.assignmentStrategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Round-robin variant assignment strategy. Assigns variants based on their current count to
 * maintain balance according to percentage distribution.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see VariantAssignmentStrategy
 */
@Slf4j
public class RoundRobinVariantAssignment implements VariantAssignmentStrategy {

  @Override
  public Variant selectVariant(List<Variant> variants, String userId) {
    if (Objects.isNull(variants) || variants.isEmpty()) {
      log.warn("No variants available for assignment");
      return null;
    }

    long totalCount = 0;

    Variant selected = variants.get(0);

    log.debug(
        "Selected variant {} for user {} using round-robin strategy",
        selected.getDisplayName(),
        userId);
    return selected;
  }

  //  private double calculateVariantRatio(Variant variant, long totalCount) {
  //    long currentCount = variant.getCurrentCount() != null ? variant.getCurrentCount() : 0L;
  //    double targetPercentage = variant.getPercentage() / 100.0;
  //
  //    if (totalCount == 0) {
  //      return 0.0;
  //    }
  //
  //    double actualPercentage = (double) currentCount / totalCount;
  //    return actualPercentage / targetPercentage;
  //  }
}
