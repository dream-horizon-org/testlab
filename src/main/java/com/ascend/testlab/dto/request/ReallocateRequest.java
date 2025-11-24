package com.ascend.testlab.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ReallocateRequest {

  @NotBlank(message = "experiment_id cannot be null")
  @JsonProperty(value = "experiment_id")
  private String experimentId;

  @NotBlank(message = "variant_name cannot be null")
  @JsonProperty(value = "variant_name")
  private String variantName;

  private String reason;

  @NotNull(message = "user_id cannot be null")
  @JsonProperty(value = "user_id")
  private String userId;
}
