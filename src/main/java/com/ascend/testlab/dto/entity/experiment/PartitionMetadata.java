package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.constants.enums.PartitionStatus;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity class representing partition metadata for a project.
 *
 * <p>Stores information about the partition creation status and metadata for a given project key.
 * Tracks the creation status, who created it, and timestamps.
 *
 * @author Nithya Sree
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class PartitionMetadata {
  private String projectKey;
  private PartitionStatus status;
  private String createdBy;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
