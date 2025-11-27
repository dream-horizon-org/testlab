package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing relational operators.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum RelationalOperator {
  /** Greater than. */
  GT(RelationalOperatorValue.GT),
  /** Greater than or equal. */
  GTE(RelationalOperatorValue.GTE),
  /** Less than. */
  LT(RelationalOperatorValue.LT),
  /** Less than or equal. */
  LTE(RelationalOperatorValue.LTE),
  /** Equal. */
  EQ(RelationalOperatorValue.EQ),
  /** Not equal. */
  NEQ(RelationalOperatorValue.NEQ),
  /** Contains. */
  CONTAINS(RelationalOperatorValue.CONTAINS),
  /** Not contains. */
  NOT_CONTAINS(RelationalOperatorValue.NOT_CONTAINS),
  /** Contains regex. */
  CONTAINS_REGEX(RelationalOperatorValue.CONTAINS_REGEX);

  private final String name;

  /**
   * Get the operator by name.
   *
   * @param name the name of the operator
   * @return the operator
   */
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

  /** Inner class for operator values. */
  public static final class RelationalOperatorValue {
    /** Greater than value. */
    public static final String GT = ">";

    /** Greater than or equal value. */
    public static final String GTE = ">=";

    /** Less than value. */
    public static final String LT = "<";

    /** Less than or equal value. */
    public static final String LTE = "<=";

    /** Equal value. */
    public static final String EQ = "=";

    /** Not equal value. */
    public static final String NEQ = "!=";

    /** Contains value. */
    public static final String CONTAINS = "contains";

    /** Not contains value. */
    public static final String NOT_CONTAINS = "not contains";

    /** Contains regex value. */
    public static final String CONTAINS_REGEX = "contains regex";

    private RelationalOperatorValue() {
      throw new UnsupportedOperationException("Constructor Invocation Unavailable for Constants");
    }
  }
}
