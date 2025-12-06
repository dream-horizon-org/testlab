package com.ascend.testlab.dto.entity.allocation;

import com.ascend.testlab.constants.Constants;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReallocationLogEntry {

  private Long timestamp;

  private String oldVariant;

  private String newVariant;

  @Builder.Default private String reason = "No reason provided";

  @Builder.Default private String changedBy = Constants.ADMIN;
}
