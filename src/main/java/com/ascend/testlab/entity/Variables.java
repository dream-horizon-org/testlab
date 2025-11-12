package com.ascend.testlab.entity;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Entity class representing a variables in an variant.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
public class Variables {

  String value;

  String key;

  @NotBlank
  @ValidEnumValue(
      enumClass = DataTypeEnum.class,
      method = Constants.GET_TYPE,
      message = ErrorMessages.INVALID_DATA_TYPE)
  private String dataType;
}
