package com.ascend.testlab.dto.entity;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
  private String experimentId;
  private String projectId;
  private String name;
  private String description;
  private String hypothesis;
  private ExperimentStatus status;
  private ExperimentType type;
  private HealthStatus guardrailHealthStatus;
  private String cohorts;
  private String variantWeights;
  private AssignmentStrategy assignmentStrategy;
  private String overrides;
  private String ruleAttributes;
  private String winningVariant;
  private Integer exposure;
  private Long threshold;
  private Long startTime;
  private Long endTime;
  private String createdBy;
  private String createdAt;
  private String updatedAt;
  private String nameTokens;
  private String tags;
  private String owners;
}
