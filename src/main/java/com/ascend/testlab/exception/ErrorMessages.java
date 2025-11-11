package com.ascend.testlab.exception;

import lombok.experimental.UtilityClass;

/**
 * This class contains constant error message strings used throughout the application.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class ErrorMessages {
    /** The error message for the project key missing. */
    public static final String PROJECT_KEY_MISSING = "x-project-key header is missing";
  public static final String INVALID_DATA_TYPE = "invalid dataType present";

  public static final String INVALID_CONDITION_VALUE = "invalid condition value";

  public static final String INVALID_ENUM_VALUE = "invalid enum value";

  public static final String INVALID_OPERATOR = "invalid operator value";

  public static final String BLANK_RULE_NAME = "rule name cannot be blank";

  public static final String INVALID_OPERAND = "invalid operand value";

  public static final String INVALID_OPERAND_DATA_TYPE = "invalid operand data type";


}
