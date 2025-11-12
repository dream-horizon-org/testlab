package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.enums.ExperimentHealth;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentStrategy;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.entity.RuleAttributes;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.validation.annotations.ValidVariantKeys;
import com.ascend.testlab.validation.annotations.ValidVariantWeights;
import com.ascend.testlab.variantWeights.VariantWeights;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * DTO for updating an experiment with comprehensive field validation.
 *
 * <p>All fields are optional for partial updates, but if provided, they must meet validation
 * constraints.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateExperimentRequest {

  @JsonProperty("name")
  @Size(min = 1, max = 64, message = "Name must be between 1 and 64 characters")
  private String name;

  @JsonProperty("description")
  @Size(max = 255, message = "Description must not exceed 255 characters")
  private String description;

  @JsonProperty("hypothesis")
  @Size(max = 1000, message = "Hypothesis must not exceed 1000 characters")
  private String hypothesis;

  @JsonProperty("status")
  private ExperimentStatus status;

  @JsonProperty("type")
  private ExperimentType type;

  @JsonProperty("guardrail_health_status")
  private ExperimentHealth guardrailHealthStatus;

  @JsonProperty("cohorts")
  @Size(min = 1, max = 20, message = "Number of cohorts must be between 1 and 20")
  private List<@NotBlank(message = "Cohort name cannot be blank") String> cohorts;

  @JsonProperty("variant_weights")
  @Valid
  @ValidVariantWeights
  private VariantWeights variantWeights;

  @JsonProperty("assignment_strategy")
  private ExperimentStrategy assignmentStrategy;

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

  @JsonProperty("rule_attributes")
  @Valid
  @Size(max = 50, message = "Maximum 50 rule attributes allowed")
  private List<RuleAttributes> ruleAttributes;

  @JsonProperty("winning_variant")
  @Valid
  @ValidVariantWeights
  private VariantWeights winningVariant;

  @JsonProperty("exposure")
  @Min(value = 0, message = "Exposure must be at least 0")
  @Max(value = 100, message = "Exposure must not exceed 100")
  private Integer exposure;

  @JsonProperty("threshold")
  @Min(value = 0, message = "Threshold must be at least 0")
  private Long threshold;

  @JsonProperty("start_time")
  @Min(value = 0, message = "Start time must be a valid epoch timestamp")
  private Long startTime;

  @JsonProperty("end_time")
  @Min(value = 0, message = "End time must be a valid epoch timestamp")
  private Long endTime;

  // Tags replace existing tags transactionally
  @JsonProperty("tag")
  @Size(max = 20, message = "Maximum 20 tags allowed")
  private List<@NotBlank(message = "Tag cannot be blank") String> tags;

  @JsonProperty("updated_by")
  @Size(max = 255, message = "Updated by must not exceed 255 characters")
  private String updatedBy;

  // Accept ISO end_date string; if present and end_time is null, service may parse it.
  @JsonProperty("end_date")
  private String endDate;
}
