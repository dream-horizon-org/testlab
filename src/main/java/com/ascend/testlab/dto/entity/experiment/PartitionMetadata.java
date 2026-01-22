package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.constants.enums.PartitionStatus;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartitionMetadata {
  private String projectKey;
  private PartitionStatus status;
  private String createdBy;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
