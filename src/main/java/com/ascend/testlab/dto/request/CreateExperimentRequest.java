package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.ExperimentHealth;
import com.ascend.testlab.constants.ExperimentStatus;
import com.ascend.testlab.constants.ExperimentStrategy;
import com.ascend.testlab.constants.ExperimentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateExperimentRequest {

  @JsonProperty("project_key")
  private UUID projectKey;

  @JsonProperty("tenant_id")
  private UUID tenantId;

  @JsonProperty("experiment_id")
  private UUID experimentId;

  @JsonProperty("name")
  @NotBlank
  private String name;

  @JsonProperty("description")
  @NotBlank
  private String description;

  @JsonProperty("hypothesis")
  @NotBlank
  private String hypothesis;

  @JsonProperty("status")
  private ExperimentStatus status;

  @JsonProperty("type")
  private ExperimentType type;

  @JsonProperty("guardrail_health_status")
  private ExperimentHealth guardrailHealthStatus;

  @JsonProperty("metrics")
  private List<String> metrics;

  @JsonProperty("cohorts")
  private List<String> cohorts;

  @JsonProperty("variant_weights")
  private Map<String, Object> variantWeights;

  @JsonProperty("assignment_strategy")
  private ExperimentStrategy assignmentStrategy;

  @JsonProperty("overrides")
  private Map<String, Object> overrides;

  @JsonProperty("rule_attributes")
  private Map<String, Object> ruleAttributes;

  @JsonProperty("winning_variant")
  private Map<String, Object> winningVariant;

  @JsonProperty("exposure")
  private int exposure;

  @JsonProperty("threshold")
  private long threshold;

  @JsonProperty("start_time")
  private long startTime;

  @JsonProperty("end_time")
  private long endTime;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("name_tsvector")
  private String nameTsvector;

  @JsonProperty("created_at")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  private Timestamp updatedAt;
}
