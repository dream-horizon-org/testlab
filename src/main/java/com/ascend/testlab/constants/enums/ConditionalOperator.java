package com.ascend.testlab.constants.enums;

import java.util.function.BinaryOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionalOperator {
  AND("AND", Boolean.TRUE, Boolean::logicalAnd),
  OR("OR", Boolean.FALSE, Boolean::logicalOr);

  private final String name;
  private final Boolean identity;
  private final BinaryOperator<Boolean> accumulator;

  public static ConditionalOperator getOperator(String name) {
    return ConditionalOperator.valueOf(name);
  }
}
