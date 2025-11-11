package com.ascend.testlab.operation;

import com.ascend.testlab.constants.attributes.RelationalOperator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.semver4j.Semver;

@Slf4j
public class TypeComparison {

  public static boolean compareBoolean(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    if (Objects.isNull(operand1)) return false;
    Boolean boolOperand1 = Boolean.valueOf(operand1);
    Boolean boolOperand2 = Boolean.valueOf(operand2);

    return switch (operator) {
      case EQ -> RelationalOperation.isEqual(boolOperand1, boolOperand2);
      case NEQ -> RelationalOperation.isNotEqual(boolOperand1, boolOperand2);
      default -> false;
    };
  }

  public static boolean compareDouble(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    if (Objects.isNull(operand1)) return false;
    Double doubleOperand1 = Double.valueOf(operand1);
    Double doubleOperand2 = Double.valueOf(operand2);

    return compareComparable(doubleOperand1, doubleOperand2, operator, operandValue);
  }

  public static boolean compareList(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    JsonArray jsonOperand2 = new JsonArray(operand2);
    return switch (operator) {
      case EQ -> RelationalOperation.isEqual(new JsonArray(operand1), jsonOperand2);
      case NEQ -> RelationalOperation.isNotEqual(new JsonArray(operand1), jsonOperand2);
      case CONTAINS -> RelationalOperation.contains(operand1, jsonOperand2.stream().toList());
      case NOT_CONTAINS -> RelationalOperation.doesNotContain(
          operand1, jsonOperand2.stream().toList());
      default -> false;
    };
  }

  public static boolean compareList(
      List<String> operand1, List<String> operand2, RelationalOperator operator) {
    return switch (operator) {
      case CONTAINS -> RelationalOperation.contains(operand1, operand2);
      case NOT_CONTAINS -> RelationalOperation.doesNotContain(operand1, operand2);
      default -> false;
    };
  }

  public static boolean compareNumber(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    if (Objects.isNull(operand1)) return false;
    Long longOperand1 = Long.valueOf(operand1);
    Long longOperand2 = Long.valueOf(operand2);

    return compareComparable(longOperand1, longOperand2, operator, operandValue);
  }

  public static boolean compareObject(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    if (Objects.isNull(operand1)) return false;
    JsonObject jsonOperand1 = new JsonObject(operand1);
    JsonObject jsonOperand2 = new JsonObject(operand2);

    return switch (operator) {
      case EQ -> RelationalOperation.isEqual(jsonOperand1, jsonOperand2);
      case NEQ -> RelationalOperation.isNotEqual(jsonOperand1, jsonOperand2);
      default -> false;
    };
  }

  public static boolean compareSemVer(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    Semver semverOperand1 = Semver.coerce(operand1);
    Semver semverOperand2 = Semver.coerce(operand2);
    if (Objects.isNull(semverOperand1)) return false;

    return compareComparable(semverOperand1, semverOperand2, operator, operandValue);
  }

  public static boolean compareString(
      String operand1, String operand2, RelationalOperator operator, String operandValue) {
    if (Objects.isNull(operand1)) return false;
    return switch (operator) {
      case CONTAINS -> RelationalOperation.contains(operand1, operand2);
      case NOT_CONTAINS -> RelationalOperation.doesNotContain(operand1, operand2);
      case CONTAINS_REGEX -> RelationalOperation.containsRegex(operand1, operand2);
      default -> compareComparable(operand1, operand2, operator, operandValue);
    };
  }

  public static <T> boolean compareComparable(
      Comparable<T> operand1, T operand2, RelationalOperator operator, String operandValue) {
    return switch (operator) {
      case GT -> RelationalOperation.isGreaterThan(operand1, operand2);
      case GTE -> RelationalOperation.isGreaterThanOrEqual(operand1, operand2);
      case LT -> RelationalOperation.isLessThan(operand1, operand2);
      case LTE -> RelationalOperation.isLessThanOrEqual(operand1, operand2);
      case EQ -> RelationalOperation.isEqual(operand1, operand2);
      case NEQ -> RelationalOperation.isNotEqual(operand1, operand2);
      default -> false;
    };
  }

  private TypeComparison() {
    throw new UnsupportedOperationException("Constructor Invocation Unavailable");
  }
}
