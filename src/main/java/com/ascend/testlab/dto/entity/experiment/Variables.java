package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.exception.ErrorMessages;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Entity class representing a variables in an variant.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
