package com.ascend.testlab.dto.request;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentStrategy;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.entity.RuleAttributes;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.validation.annotations.ValidExperimentTargeting;
import com.ascend.testlab.validation.annotations.ValidTimeRange;
import com.ascend.testlab.validation.annotations.ValidVariantKeys;
import com.ascend.testlab.validation.annotations.ValidVariantWeightKeys;
import com.ascend.testlab.validation.annotations.ValidVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
@ValidExperimentTargeting
@ValidTimeRange
@ValidVariantWeightKeys
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateExperimentRequest {

  @JsonProperty("project_key")
  private String projectKey;

  @JsonProperty("tenant_id")
  private UUID tenantId;

  @JsonProperty("experiment_id")
  private UUID experimentId;

  @JsonProperty("name")
  @NotBlank(message = "Experiment name is required")
  @Size(min = 1, max = 64, message = "Name must be between 1 and 64 characters")
  private String name;

  @JsonProperty("experiment_key")
  private String experimentKey;

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
  @ValidEnumValue(
      enumClass = ExperimentStatus.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STATUS)
  private ExperimentStatus status;

  @JsonProperty("type")
  @NotNull(message = "Experiment type is required")
  @ValidEnumValue(
      enumClass = ExperimentType.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_TYPE)
  private ExperimentType type;

  @JsonProperty("guardrail_health_status")
  @ValidEnumValue(
      enumClass = ExperimentHealth.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_HEALTH)
  private ExperimentHealth guardrailHealthStatus;

  @JsonProperty("metrics")
  private List<String> metrics;

  @JsonProperty("cohorts")
  private List<String> cohorts;

  @JsonProperty("tags")
  private List<@NotBlank(message = "Tag cannot be blank") String> tags;

  @JsonProperty("owner")
  @NotEmpty(message = "At least one owner is required")
  @Size(max = 10, message = "Maximum 10 owners allowed")
  private List<
          @NotBlank(message = "Owner cannot be blank")
          @Email(message = "Owner must be a valid email address") String>
      owner;

  @JsonProperty("variant_weights")
  @NotNull(message = "Variant weights are required")
  @Valid
  @ValidVariantWeights
  private VariantWeights variantWeights;

  @JsonProperty("rule_attributes")
  @Valid
  private List<RuleAttributes> ruleAttributes;

  @JsonProperty("winning_variant")
  @Valid
  @ValidVariantKeys
  private Map<String, @Valid Variant> winningVariant;

  @JsonProperty("variants")
  @Valid
  @NotNull(message = "Variant is required")
  @ValidVariantKeys
  private Map<String, @Valid Variant> variants;

  @JsonProperty("distribution_strategy")
  @NotNull(message = "ExperimentStrategy is required")
  @ValidEnumValue(
      enumClass = ExperimentStrategy.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STRATEGY)
  private ExperimentStrategy distributionStrategy;

  @JsonProperty("assignment_domain")
  @NotNull(message = "ExperimentStrategy is required")
  @ValidEnumValue(
      enumClass = AssignmentDomain.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_ASSIGNMENT_DOMAIN)
  private AssignmentDomain assignmentDomain;

  @JsonProperty("overrides")
  @Size(max = 100, message = "Maximum 100 overrides allowed")
  private List<@NotBlank(message = "Override cannot be blank") String> overrides;

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
  private long createdAt;

  @JsonProperty("updated_at")
  private long updatedAt;
}
