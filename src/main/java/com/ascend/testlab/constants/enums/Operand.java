package com.ascend.testlab.constants.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing operands used in rule conditions.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum Operand {
  /** User ID operand. */
  USER_ID(OperandValue.USER_ID),
  /** Guest ID operand. */
  GUEST_ID(OperandValue.GUEST_ID),
  /** Model operand. */
  MODEL(OperandValue.MODEL),
  /** Device operand. */
  DEVICE(OperandValue.DEVICE),
  /** App name operand. */
  APP_NAME(OperandValue.APP_NAME),
  /** Platform operand. */
  PLATFORM(OperandValue.PLATFORM),
  /** OS version operand. */
  OS_VERSION(OperandValue.OS_VERSION),
  /** App version operand. */
  APP_VERSION(OperandValue.APP_VERSION),
  /** Build number operand. */
  BUILD_NUMBER(OperandValue.BUILD_NUMBER),
  /** Current location city operand. */
  CURRENT_LOCATION_CITY(OperandValue.CURRENT_LOCATION_CITY),
  /** Current location state operand. */
  CURRENT_LOCATION_STATE(OperandValue.CURRENT_LOCATION_STATE),
  /** Current location country operand. */
  CURRENT_LOCATION_COUNTRY(OperandValue.CURRENT_LOCATION_COUNTRY);

  private final String name;

  /** Inner class for operand values. */
  public static final class OperandValue {
    /** User ID value. */
    public static final String USER_ID = "user_id";

    /** Guest ID value. */
    public static final String GUEST_ID = "guest_id";

    /** Model value. */
    public static final String MODEL = "model";

    /** Device value. */
    public static final String DEVICE = "device";

    /** App name value. */
    public static final String APP_NAME = "app_name";

    /** Platform value. */
    public static final String PLATFORM = "platform";

    /** OS version value. */
    public static final String OS_VERSION = "os_version";

    /** App version value. */
    public static final String APP_VERSION = "app_version";

    /** Build number value. */
    public static final String BUILD_NUMBER = "build_number";

    /** Current location city value. */
    public static final String CURRENT_LOCATION_CITY = "current_location_city";

    /** Current location state value. */
    public static final String CURRENT_LOCATION_STATE = "current_location_state";

    /** Current location country value. */
    public static final String CURRENT_LOCATION_COUNTRY = "current_location_country";

    private OperandValue() {
      throw new UnsupportedOperationException("Constructor Invocation Unavailable for Constants");
    }
  }
}
