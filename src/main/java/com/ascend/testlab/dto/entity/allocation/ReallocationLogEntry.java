package com.ascend.testlab.dto.entity.allocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a reallocation log entry in Aerospike. Stored in reallocationLog set with
 * composite key (userId_experimentId) and appended to entries list.
 *
 * <p>Immutable when created via builder, includes timestamp of reallocation and tracks variant
 * change with reason for audit trail.
 *
 * @author NishantParmar0026
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationLogEntry {

  @JsonProperty(value = "timestamp")
  private Long timestamp;

  @JsonProperty(value = "old_variant")
  private String oldVariant;

  @JsonProperty(value = "new_variant")
  private String newVariant;

  @JsonProperty(value = "reason")
  @Builder.Default
  private String reason = "No reason provided";

  @JsonProperty(value = "changed_by")
  @Builder.Default
  private String changedBy = "system";
}
