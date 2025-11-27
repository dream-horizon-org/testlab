package com.ascend.testlab.operation;

import com.ascend.testlab.constants.enums.RelationalOperator;

/**
 * Functional interface for evaluation operations.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Evaluation {
  /**
   * Evaluate the operation.
   *
   * @param operand1 the first operand
   * @param operand2 the second operand
   * @param operator the operator
   * @param operandValue the operand value
   * @return the result of the evaluation
   */
  boolean evaluate(
      String operand1, String operand2, RelationalOperator operator, String operandValue);
}
