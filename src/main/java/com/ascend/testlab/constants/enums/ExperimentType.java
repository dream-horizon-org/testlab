package com.ascend.testlab.constants.enums;

public enum ExperimentType {
  A_B("A/B");

  private final String value;

  ExperimentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
