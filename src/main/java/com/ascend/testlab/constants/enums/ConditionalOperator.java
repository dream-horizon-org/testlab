package com.ascend.testlab.constants.enums;

import java.util.function.BinaryOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing conditional operators (AND, OR).
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ConditionalOperator {
  /** The AND operator. */
  AND("AND", Boolean.TRUE, Boolean::logicalAnd),
  /** The OR operator. */
  OR("OR", Boolean.FALSE, Boolean::logicalOr);

  /** The name of the operator. */
  private final String name;

  /** The identity value for the operator. */
  private final Boolean identity;

  /** The accumulator function for the operator. */
  private final BinaryOperator<Boolean> accumulator;

  /**
   * Get the operator by name.
   *
   * @param name the name of the operator
   * @return the operator
   */
  public static ConditionalOperator getOperator(String name) {
    return ConditionalOperator.valueOf(name);
  }
}
