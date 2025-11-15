package com.ascend.testlab.util.strategy.assignmentStrategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Random variant assignment strategy. Assigns variants with equal probability using random
 * selection.
 *
 * <p>This strategy provides:
 *
 * <ul>
 *   <li>True randomization: Each selection is independent
 *   <li>Equal probability: All variants have equal chance (1/N)
 *   <li>Non-deterministic: Same user may get different variants over time
 * </ul>
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see VariantAssignmentStrategy
 */
@Slf4j
public class RandomVariantAssignment implements VariantAssignmentStrategy {

  @Override
  public Variant selectVariant(List<Variant> variants, String userId) {
    if (Objects.isNull(variants) || variants.isEmpty()) {
      log.warn("No variants available for random assignment");
      return null;
    }

    int randomIndex = (int) (Math.random() * variants.size());
    Variant selected = variants.get(randomIndex);

    log.debug(
        "Random selected variant {} for user {} from {} variants",
        selected.getDisplayName(),
        userId,
        variants.size());

    return selected;
  }
}
