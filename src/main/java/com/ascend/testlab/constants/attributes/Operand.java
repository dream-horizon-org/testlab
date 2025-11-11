package com.ascend.testlab.constants.attributes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Operand {
  USER_ID(OperandValue.USER_ID),
  GUEST_ID(OperandValue.GUEST_ID),
  MODEL(OperandValue.MODEL),
  DEVICE(OperandValue.DEVICE),
  APP_NAME(OperandValue.APP_NAME),
  PLATFORM(OperandValue.PLATFORM),
  OS_VERSION(OperandValue.OS_VERSION),
  APP_VERSION(OperandValue.APP_VERSION),
  BUILD_NUMBER(OperandValue.BUILD_NUMBER),
  CURRENT_LOCATION_CITY(OperandValue.CURRENT_LOCATION_CITY),
  CURRENT_LOCATION_STATE(OperandValue.CURRENT_LOCATION_STATE),
  CURRENT_LOCATION_COUNTRY(OperandValue.CURRENT_LOCATION_COUNTRY);

  private final String name;

  public static final class OperandValue {
    public static final String USER_ID = "user_id";
    public static final String GUEST_ID = "guest_id";
    public static final String MODEL = "model";
    public static final String DEVICE = "device";
    public static final String APP_NAME = "app_name";
    public static final String PLATFORM = "platform";
    public static final String OS_VERSION = "os_version";
    public static final String APP_VERSION = "app_version";
    public static final String BUILD_NUMBER = "build_number";
    public static final String CURRENT_LOCATION_CITY = "current_location_city";
    public static final String CURRENT_LOCATION_STATE = "current_location_state";
    public static final String CURRENT_LOCATION_COUNTRY = "current_location_country";

    private OperandValue() {
      throw new UnsupportedOperationException("Constructor Invocation Unavailable for Constants");
    }
  }
}
