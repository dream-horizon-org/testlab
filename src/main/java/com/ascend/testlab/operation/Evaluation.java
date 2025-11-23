package com.ascend.testlab.operation;

import com.ascend.testlab.constants.enums.RelationalOperator;

@FunctionalInterface
public interface Evaluation {
  boolean evaluate(
      String operand1, String operand2, RelationalOperator operator, String operandValue);
}
