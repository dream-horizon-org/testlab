package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the experiment type.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public enum ExperimentType {
  A_B("A/B");

  private final String value;

  ExperimentType(String value) {
    this.value = value;
  }

  /**
   * Returns the JSON representation of the experiment type.
   *
   * @return the string value of the experiment type
   */
  @JsonValue
  public String toJson() {
    return value;
  }

  /**
   * Converts a string value to the corresponding ExperimentType enum.
   *
   * @param value the string value to convert
   * @return the corresponding ExperimentType enum
   * @throws RestException if the value does not match any experiment type
   */
  public static ExperimentType fromValue(String value) {
    for (ExperimentType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new RestException(ErrorEnum.INVALID_EXPERIMENT_TYPE);
  }
}
