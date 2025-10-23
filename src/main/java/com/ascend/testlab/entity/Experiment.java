package com.ascend.testlab.entity;

import java.util.List;
import java.util.Map;
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
  private UUID tenantId;
  private String name;
  private String description;
  private String status;
  private Map<String, ApiPathVariants> apiPaths;
  private Long startTime;
  private Long endTime;
  private Boolean isStatic;
  private Boolean isExclusive;
  private List<String> entities;
  private Long threshold;
  private DistributionStrategy distributionStrategy;
  private AssignmentDomain assignmentDomain;
}

