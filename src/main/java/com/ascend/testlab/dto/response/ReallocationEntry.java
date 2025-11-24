package com.ascend.testlab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single reallocation log entry for a user-experiment variant change.
 *
 * @author NishantParmar0026
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationEntry {

  @JsonProperty(value = "timestamp")
  private Long timestamp;

  @JsonProperty(value = "old_variant")
  private String oldVariant;

  @JsonProperty(value = "new_variant")
  private String newVariant;

  @JsonProperty(value = "reason")
  private String reason;

  @JsonProperty(value = "changed_by")
  private String changedBy;
}
