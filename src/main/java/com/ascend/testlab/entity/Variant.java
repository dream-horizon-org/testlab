package com.ascend.testlab.entity;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.DataTypeEnum;
import com.ascend.testlab.exception.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a variant in an experiment.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
  private String displayName;
  private Map<String, Object> variables;

  @NotBlank
  @ValidEnumValue(
      enumClass = DataTypeEnum.class,
      method = Constants.GET_TYPE,
      message = ErrorMessages.INVALID_DATA_TYPE)
  private String dataType;
}
