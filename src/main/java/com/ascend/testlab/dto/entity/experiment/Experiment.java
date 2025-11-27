package com.ascend.testlab.dto.entity.experiment;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Experiment {
  @JsonProperty("experiment_id")
  private UUID experimentId;

  @JsonProperty("project_key")
  private String projectKey;

  @JsonProperty("name")
  private String name;

  @JsonProperty("experiment_key")
  private String key;

  @JsonProperty("description")
  private String description;

  @JsonProperty("hypothesis")
  private String hypothesis;

  @JsonProperty("experiment_status")
  private ExperimentStatus status;

  @JsonProperty("experiment_type")
  private ExperimentType type;

  @JsonProperty("guardrail_health_status")
  private HealthStatus guardrailHealthStatus;

  @JsonProperty("cohorts")
  private List<String> cohorts;

  @JsonProperty("variant_weights")
  private VariantWeights variantWeights;

  @JsonProperty("variants")
  private Map<String, Variant> variants;

  private List<RuleAttributes> ruleAttributes;
  private DistributionStrategy distributionStrategy;
  private AssignmentDomain assignmentDomain;
  private List<String> overrides;

  @JsonProperty("winning_variant")
  private JsonObject winningVariant;

  @JsonProperty("exposure")
  private Integer exposure;

  @JsonProperty("threshold")
  private Long threshold;

  @JsonProperty("start_time")
  private Long startTime;

  @JsonProperty("end_time")
  private Long endTime;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;

  private List<String> tags;
  private List<String> owners;

  @JsonProperty("variant_counts")
  private JsonObject variantCounts;
}
