package com.ascend.testlab.constants.enums;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the experiment status.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public enum ExperimentStatus {

  /** The experiment is test and actively running for test users. */
  TEST,

  /** The experiment is live and actively running. */
  LIVE,

  /** The experiment is paused but can be resumed. */
  PAUSED,

  /** The experiment is in draft state and not yet live. */
  DRAFT,

  /** The experiment has concluded and reached its end state. */
  CONCLUDED,

  /** The experiment has been terminated and is no longer active. */
  TERMINATED;

  /**
   * Returns the String representation of the experiment status.
   *
   * @return the string value of the experiment status
   */
  @Override
  @JsonValue
  public String toString() {
    return name();
  }

  /**
   * Converts a string value to the corresponding ExperimentStatus enum.
   *
   * <p>This method is used by Jackson for JSON deserialization.
   *
   * @param value the string value to convert (e.g., "DRAFT", "LIVE")
   * @return the corresponding ExperimentStatus enum
   * @throws RestException if the value does not match any experiment status
   */
  @JsonCreator
  public static ExperimentStatus fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (ExperimentStatus status : values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new RestException(ErrorEnum.INVALID_EXPERIMENT_STATUS);
  }
}
