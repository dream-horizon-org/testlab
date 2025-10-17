package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.ExperimentStatus;
import com.ascend.testlab.constants.ExperimentType;
import com.ascend.testlab.constants.HealthStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.sql.Timestamp;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateExperimentRequest {

  @JsonProperty("tenant_id")
  private byte[] tenantId;

  @JsonProperty("experiment_id")
  private byte[] experimentId;

  @JsonProperty("name")
  @NotBlank
  private String name;

  @JsonProperty("description")
  @NotBlank
  private String description;

  @JsonProperty("hypothesis")
  private String hypothesis;

  @JsonProperty("rules_json")
  private Map<String, Object> rulesJson;

  @JsonProperty("status")
  private ExperimentStatus status;

  @JsonProperty("actuals")
  private String actuals;

  @JsonProperty("start_time")
  private long startTime;

  @JsonProperty("end_time")
  private long endTime;

  @JsonProperty("cohort_id")
  private String cohortId;

  @JsonProperty("distribution_strategy")
  private String distributionStrategy;

  @JsonProperty("assignment_domain")
  private String assignmentDomain;

  @JsonProperty("percentage_distribution")
  private String percentageDistribution;

  @JsonProperty("exposure")
  private int exposure;

  @JsonProperty("created_by")
  private long createdBy;

  @JsonProperty("threshold")
  private long threshold;

  @JsonProperty("is_exclusive")
  private Boolean isExclusive;

  @JsonProperty("health")
  private HealthStatus health;

  @JsonProperty("type")
  private ExperimentType type;

  @JsonProperty("created_at")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  @JsonProperty("name_tokens")
  private String nameTokens;
}
