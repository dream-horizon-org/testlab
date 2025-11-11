package com.ascend.testlab.entity;

import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.entity.variantWeights.VariantWeights;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing an experiment in the testlab application.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
  private UUID experimentId;
  private String projectKey;
  private String name;
  private String description;
  private String status;
  private List<String> cohorts;
  private VariantWeights variantWeights;
  private Map<String, Variant> variant;
  private List<RuleAttributes> ruleAttributes;
  private Long startTime;
  private List<String> overrides;
  private Long endTime;
  private int exposure;
  private Long threshold;
  private DistributionStrategy distributionStrategy;
  private AssignmentDomain assignmentDomain;
  private String winningVariant;
}
