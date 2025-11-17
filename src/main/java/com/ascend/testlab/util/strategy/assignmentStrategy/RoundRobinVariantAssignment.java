package com.ascend.testlab.util.strategy.assignmentStrategy;

import com.ascend.testlab.entity.Variant;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Round-robin variant assignment strategy. Uses deterministic hash-based approach to ensure same
 * user always gets same variant while maintaining even distribution across users.
 *
 * <p>This strategy guarantees:
 *
 * <ul>
 *   <li>Deterministic assignment: Same user always gets same variant
 *   <li>Even distribution: All variants get approximately equal number of users
 *   <li>Stateless operation: No need to track counters or state
 * </ul>
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see VariantAssignmentStrategy
 */
@Slf4j
public class RoundRobinVariantAssignment implements VariantAssignmentStrategy {

  @Override
  public Variant selectVariant(List<Variant> variants, String userId) {
    if (Objects.isNull(variants) || variants.isEmpty()) {
      log.warn("No variants available for round-robin assignment");
      return null;
    }
    // todo -> change logic
    int hash = userId.hashCode();
    int index = Math.abs(hash % variants.size());

    Variant selected = variants.get(index);

    log.debug(
        "Round-robin selected variant {} for user {} (index: {}/{})",
        selected.getDisplayName(),
        userId,
        index,
        variants.size());

    return selected;
  }
}
