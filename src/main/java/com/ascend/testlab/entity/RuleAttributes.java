package com.ascend.testlab.entity;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.attributes.Operand;
import com.ascend.testlab.constants.attributes.RelationalOperator;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing rule attributes for experiment filtering.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleAttributes {

  @NotBlank(message = ErrorMessages.BLANK_RULE_NAME)
  private String name;

  List<Condition> conditions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Condition {
    @NotBlank
    @ValidEnumValue(
        enumClass = Operand.class,
        method = Constants.GET_NAME,
        message = ErrorMessages.INVALID_OPERAND)
    private String operand;

    @NotEmpty
    @ValidEnumValue(
        enumClass = DataTypeEnum.class,
        method = Constants.GET_TYPE,
        message = ErrorMessages.INVALID_OPERAND_DATA_TYPE)
    private String operandDataType;

    @NotEmpty
    @ValidEnumValue(
        enumClass = RelationalOperator.class,
        method = Constants.GET_NAME,
        message = ErrorMessages.INVALID_OPERATOR)
    private String operator;

    @NotBlank(message = ErrorMessages.INVALID_CONDITION_VALUE)
    private String value;
  }
}
