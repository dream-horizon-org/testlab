package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentStrategy;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.entity.RuleAttributes;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.validation.ValidVariantKeys;
import com.ascend.testlab.validation.ValidVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * DTO for creating a new experiment with comprehensive field validation.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
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
  @NotBlank(message = "Experiment name is required")
  @Size(min = 1, max = 64, message = "Name must be between 1 and 64 characters")
  private String name;

  @JsonProperty("description")
  @NotBlank(message = "Description is required")
  @Size(max = 255, message = "Description must not exceed 255 characters")
  private String description;

  @JsonProperty("hypothesis")
  @NotBlank(message = "Hypothesis is required")
  @Size(max = 1000, message = "Hypothesis must not exceed 1000 characters")
  private String hypothesis;

  @JsonProperty("status")
  @NotNull(message = "Status is required")
  private ExperimentStatus status;

  @JsonProperty("type")
  @NotNull(message = "Experiment type is required")
  private ExperimentType type;

  @JsonProperty("guardrail_health_status")
  private ExperimentHealth guardrailHealthStatus;

  @JsonProperty("metrics")
  @Size(max = 10, message = "Maximum 10 metrics allowed")
  private List<@NotBlank(message = "Metric name cannot be blank") String> metrics;

  @JsonProperty("cohorts")
  @NotEmpty(message = "At least one cohort is required")
  @Size(min = 1, max = 20, message = "Number of cohorts must be between 1 and 20")
  private List<@NotBlank(message = "Cohort name cannot be blank") String> cohorts;

  @JsonProperty("tags")
  @Size(max = 20, message = "Maximum 20 tags allowed")
  private List<@NotBlank(message = "Tag cannot be blank") String> tags;

  @JsonProperty("owner")
  @NotBlank(message = "Owner is required")
  @Email(message = "Owner must be a valid email address")
  @Size(max = 255, message = "Owner email must not exceed 255 characters")
  private String owner;

  @JsonProperty("variant_weights")
  @NotNull(message = "Variant weights are required")
  @Valid
  @ValidVariantWeights
  private VariantWeights variantWeights;

  @JsonProperty("assignment_strategy")
  @NotNull(message = "Assignment strategy is required")
  private ExperimentStrategy assignmentStrategy;

  @JsonProperty("rule_attributes")
  @Valid
  @Size(max = 50, message = "Maximum 50 rule attributes allowed")
  private List<RuleAttributes> ruleAttributes;

  @JsonProperty("winning_variant")
  @Valid
  @ValidVariantWeights
  private VariantWeights winningVariant;

  @JsonProperty("variants")
  @Valid
  @ValidVariantKeys
  @Size(max = 50, message = "Maximum 50 variants allowed")
  private Map<String, @Valid Variant> variants;

  @JsonProperty("distribution_strategy")
  private ExperimentStrategy distributionStrategy;

  @JsonProperty("assignment_domain")
  private AssignmentDomain assignmentDomain;

  @JsonProperty("overrides")
  @Size(max = 255, message = "Overrides must not exceed 255 characters")
  private String overrides;

  @JsonProperty("exposure")
  @Min(value = 0, message = "Exposure must be at least 0")
  @Max(value = 100, message = "Exposure must not exceed 100")
  private int exposure;

  @JsonProperty("threshold")
  @Min(value = 0, message = "Threshold must be at least 0")
  private long threshold;

  @JsonProperty("start_time")
  @Min(value = 0, message = "Start time must be a valid epoch timestamp")
  private long startTime;

  @JsonProperty("end_time")
  @Min(value = 0, message = "End time must be a valid epoch timestamp")
  private long endTime;

  @JsonProperty("created_by")
  @NotBlank(message = "Created by is required")
  @Size(max = 255, message = "Created by must not exceed 255 characters")
  private String createdBy;

  @JsonProperty("created_at")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  private Timestamp updatedAt;
}
