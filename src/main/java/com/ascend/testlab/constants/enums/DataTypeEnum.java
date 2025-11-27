package com.ascend.testlab.constants.enums;

import com.ascend.testlab.operation.Evaluation;
import com.ascend.testlab.operation.TypeComparison;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing supported data types for operations.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum DataTypeEnum {
  /** Boolean type. */
  BOOL("BOOL", TypeComparison::compareBoolean),
  /** Number type. */
  NUMBER("NUMBER", TypeComparison::compareNumber),
  /** Decimal type. */
  DECIMAL("DECIMAL", TypeComparison::compareDouble),
  /** String type. */
  STRING("STRING", TypeComparison::compareString),
  /** Semantic version string type. */
  SEMVER_STRING("SEMVER_STRING", TypeComparison::compareSemVer),
  /** Object type. */
  OBJECT("OBJECT", TypeComparison::compareObject),
  /** List type. */
  LIST("LIST", TypeComparison::compareList);

  private final String type;
  private final Evaluation evaluation;

  /**
   * Get the data type by name.
   *
   * @param type the name of the data type
   * @return the data type
   */
  public static DataTypeEnum getDataType(String type) {
    return DataTypeEnum.valueOf(type);
  }
}
