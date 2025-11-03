package com.ascend.testlab.entity;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
  private UUID experimentId;
  private UUID projectId;
  private String name;
  private String description;
  private String status;
  private String cohorts;
  private VariantWeights variantWeights;
  private RuleAttributes ruleAttributes;
  private Long startTime;
  private List<String> overrides;
  private Long endTime;
  private List<String> entities;
  private int exposure;
  private Long threshold;
  private DistributionStrategy distributionStrategy;
  private AssignmentDomain assignmentDomain;
}
