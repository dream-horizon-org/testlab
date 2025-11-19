package com.ascend.testlab.dto.request;

import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.util.ExceptionUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Request object for experiment variant reallocation.
 *
 * @author NishantParmar0026
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocateRequest {

  @JsonProperty(value = "experiment_id")
  private String experimentId;

  @JsonProperty(value = "variant_name")
  private String variantName;

  private String reason;

    @JsonProperty(value = "user_id")
  private String userId;

    public void validate() {
        if(Objects.isNull(experimentId) || Objects.isNull(variantName)){
            throw ExceptionUtil.getException(ErrorEnum.INVALID_REQUEST_BODY);
        }
        if (StringUtils.isBlank(userId)) {
            throw ExceptionUtil.getException(ErrorEnum.MISSING_USER_IDENTIFIER);
        }
    }
}
