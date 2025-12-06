package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the experiment type.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public enum ExperimentType {

  /** A/A testing experiment type. Also used in testing Null Hypothesis */
  A_A("A/A"),

  /** A/B testing experiment type. */
  A_B("A/B");

  private final String value;

  ExperimentType(String value) {
    this.value = value;
  }

  /**
   * Returns the string value of the experiment type.
   *
   * @return the experiment type value
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the String representation of the experiment type.
   *
   * @return the string value of the experiment type
   */
  @Override
  @JsonValue
  public String toString() {
    return value;
  }

  /**
   * Converts a string value to the corresponding ExperimentType enum.
   *
   * <p>This method is used by Jackson for JSON deserialization.
   *
   * @param value the string value to convert (e.g., "A/B" or "A_B")
   * @return the corresponding ExperimentType enum
   * @throws RestException if the value does not match any experiment type
   */
  @JsonCreator
  public static ExperimentType fromValue(String value) {
    for (ExperimentType type : values()) {
      // Accept both "A/B" and "A_B" formats, and also the enum name
      if (type.value.equalsIgnoreCase(value)
          || type.name().equalsIgnoreCase(value)
          || type.name().replace("_", "/").equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new RestException(ErrorEnum.INVALID_EXPERIMENT_TYPE);
  }
}
