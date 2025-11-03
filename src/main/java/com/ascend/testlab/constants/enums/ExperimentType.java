package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExperimentType {
  A_B("A/B");

  private final String value;

  ExperimentType(String value) {
    this.value = value;
  }

  @JsonValue
  public String toJson() {
    return value;
  }

  public static ExperimentType fromValue(String value) {
    for (ExperimentType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new RestException(ErrorEnum.VALID_EXPERIMENT_TYPE_FAILED);
  }
}
