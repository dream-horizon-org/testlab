package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RelationalOperator {
  GT(RelationalOperatorValue.GT),
  GTE(RelationalOperatorValue.GTE),
  LT(RelationalOperatorValue.LT),
  LTE(RelationalOperatorValue.LTE),
  EQ(RelationalOperatorValue.EQ),
  NEQ(RelationalOperatorValue.NEQ),
  CONTAINS(RelationalOperatorValue.CONTAINS),
  NOT_CONTAINS(RelationalOperatorValue.NOT_CONTAINS),
  CONTAINS_REGEX(RelationalOperatorValue.CONTAINS_REGEX);

  private final String name;

  public static RelationalOperator getOperator(String name) {
    return switch (name) {
      case RelationalOperatorValue.GT -> GT;
      case RelationalOperatorValue.GTE -> GTE;
      case RelationalOperatorValue.LT -> LT;
      case RelationalOperatorValue.LTE -> LTE;
      case RelationalOperatorValue.EQ -> EQ;
      case RelationalOperatorValue.NEQ -> NEQ;
      case RelationalOperatorValue.CONTAINS -> CONTAINS;
      case RelationalOperatorValue.NOT_CONTAINS -> NOT_CONTAINS;
      case RelationalOperatorValue.CONTAINS_REGEX -> CONTAINS_REGEX;
      default -> throw ExceptionUtil.getException(ErrorEnum.INVALID_REQUEST_BODY);
    };
  }

  public static final class RelationalOperatorValue {
    public static final String GT = ">";
    public static final String GTE = ">=";
    public static final String LT = "<";
    public static final String LTE = "<=";
    public static final String EQ = "=";
    public static final String NEQ = "!=";
    public static final String CONTAINS = "contains";
    public static final String NOT_CONTAINS = "not contains";
    public static final String CONTAINS_REGEX = "contains regex";

    private RelationalOperatorValue() {
      throw new UnsupportedOperationException("Constructor Invocation Unavailable for Constants");
    }
  }
}
