package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
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
public class Experiment {
  private UUID experimentId;
  private String projectKey;
  private String name;
  private String key;
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
  private JsonObject winningVariant;
  private Integer exposure;
  private Long threshold;
  private Long startTime;
  private Long endTime;
  private String createdBy;
  private Instant createdAt;
  private Instant updatedAt;
  private List<String> tags;
  private List<String> owners;
  private Map<String, Long> variantCounts;
}
