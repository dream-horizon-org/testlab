package com.ascend.testlab.dto.request;

import com.ascend.testlab.exception.ErrorMessages;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReallocateRequest {

  @NotBlank(message = ErrorMessages.EXPERIMENT_ID_MISSING)
  private String experimentId;

  @NotBlank(message = ErrorMessages.VARIANT_NAME_MISSING)
  private String variantName;

  private String reason;

  @NotNull(message = ErrorMessages.USER_ID_MISSING)
  private String userId;
}
