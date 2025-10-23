package com.ascend.testlab.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionStrategy {
  private String strategyType; // RANDOM, ROUND_ROBIN
  private AssignmentDomain assignmentDomain;
}

