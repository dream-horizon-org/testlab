package com.ascend.testlab.dto.entity;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
  private Long experiment_id;
  private String tenantId;
  private String name;
  private String description;
  private String metrics;
  private String assignmentDomain;
  private Integer exposure;
  private Integer threshold;
  private ExperimentType type;
  private LocalDateTime endDate;
  private String tags;
  private String owner;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private ExperimentStatus status;
}
