package com.ascend.testlab.dto.entity;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.util.List;
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
  private JsonObject variantWeights;

  @JsonProperty("assignment_strategy")
  private AssignmentStrategy assignmentStrategy;

  @JsonProperty("overrides")
  private JsonObject overrides;

  @JsonProperty("rule_attributes")
  private JsonObject ruleAttributes;

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

  @JsonProperty("tags")
  private List<String> tags;

  @JsonProperty("owner")
  private String owner;

  @JsonProperty("variant_counts")
  private JsonObject variantCounts;
}
