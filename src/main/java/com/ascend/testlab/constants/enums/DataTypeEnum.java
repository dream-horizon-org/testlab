package com.ascend.testlab.constants.enums;

import com.ascend.testlab.operation.Evaluation;
import com.ascend.testlab.operation.TypeComparison;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {
  BOOL("BOOL", TypeComparison::compareBoolean),
  NUMBER("NUMBER", TypeComparison::compareNumber),
  DECIMAL("DECIMAL", TypeComparison::compareDouble),
  STRING("STRING", TypeComparison::compareString),
  SEMVER_STRING("SEMVER_STRING", TypeComparison::compareSemVer),
  OBJECT("OBJECT", TypeComparison::compareObject),
  LIST("LIST", TypeComparison::compareList);

  private final String type;
  private final Evaluation evaluation;

  public static DataTypeEnum getDataType(String type) {
    return DataTypeEnum.valueOf(type);
  }
}
