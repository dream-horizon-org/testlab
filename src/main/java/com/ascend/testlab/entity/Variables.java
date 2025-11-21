package com.ascend.testlab.entity;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.validation.annotations.ValidVariableDataType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Entity class representing a variable in a variant.
 *
 * <p>Each variable has a key, value, and dataType. The dataType must match the actual type of the
 * value (e.g., if dataType is NUMBER, value must be a valid integer).
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@ValidVariableDataType
public class Variables {

  @JsonProperty("key")
  @NotBlank(message = "Variable key cannot be blank")
  private String key;

  @JsonProperty("value")
  @NotBlank(message = "Variable value cannot be blank")
  private String value;

  @JsonProperty("data_type")
  @NotBlank(message = "Variable dataType cannot be blank")
  @ValidEnumValue(
      enumClass = DataTypeEnum.class,
      method = Constants.GET_TYPE,
      message = ErrorMessages.INVALID_DATA_TYPE)
  private String dataType;
}
