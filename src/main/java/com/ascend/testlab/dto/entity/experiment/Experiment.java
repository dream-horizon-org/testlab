package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

/**
 * Entity class representing an experiment in the system.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Experiment {
  private UUID experimentId;
  private String projectKey;
  private String name;
  private String experimentKey;
  private String description;
  private String hypothesis;
  private ExperimentStatus status;
  private ExperimentType type;
  private HealthStatus guardrailHealthStatus;
  private List<String> cohorts;
  private VariantWeights variantWeights;
  private Map<String, Variant> variants;
  private List<RuleAttributes> ruleAttributes;
  private DistributionStrategy distributionStrategy;
  private AssignmentDomain assignmentDomain;
  private List<String> overrides;
  private WinningVariant winningVariant;
  private Integer exposure;
  private Long threshold;
  private Long startTime;
  private Long endTime;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private List<String> tags;
  private List<String> owners;
  private Metrics metrics;

  /**
   * Creates an Experiment entity from a CreateExperimentRequest.
   *
   * @param request the create experiment request
   * @return the experiment entity
   */
  public static Experiment fromRequest(CreateExperimentRequest request) {
    return Experiment.builder()
        .name(request.getName())
        .description(request.getDescription())
        .hypothesis(request.getHypothesis())
        .experimentKey(request.getExperimentKey())
        .status(ExperimentStatus.valueOf(request.getStatus()))
        .type(ExperimentType.fromValue(request.getType()))
        .cohorts(request.getCohorts())
        .variantWeights(request.getVariantWeights())
        .variants(request.getVariants())
        .overrides(request.getOverrides())
        .distributionStrategy(DistributionStrategy.valueOf(request.getDistributionStrategy()))
        .assignmentDomain(AssignmentDomain.valueOf(request.getAssignmentDomain()))
        .threshold(request.getThreshold())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .createdBy(request.getCreatedBy())
        .ruleAttributes(request.getRuleAttributes())
        .exposure(request.getExposure())
        .tags(request.getTags())
        .owners(request.getOwners())
        .metrics(request.getMetrics())
        .build();
  }
}
