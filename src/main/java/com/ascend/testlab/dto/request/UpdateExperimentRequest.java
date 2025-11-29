package com.ascend.testlab.dto.request;

import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.experiment.Metrics;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.experiment.WinningVariant;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.exception.ErrorMessages;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateExperimentRequest {

  private String name;

  private String experimentKey;

  private Metrics metrics;

  private List<String> tags;

  private List<String> owner;

  @Size(max = 255, message = "Description must not exceed 255 characters")
  private String description;

  @Size(max = 1000, message = "Hypothesis must not exceed 1000 characters")
  private String hypothesis;

  @ValidEnumValue(
      enumClass = ExperimentStatus.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STATUS)
  private ExperimentStatus status;

  @ValidEnumValue(
      enumClass = ExperimentType.class,
      method = Constants.GET_TYPE,
      message = ErrorMessages.INVALID_EXPERIMENT_TYPE)
  private ExperimentType type;

  @ValidEnumValue(
      enumClass = AssignmentDomain.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_ASSIGNMENT_DOMAIN)
  private AssignmentDomain assignmentDomain;

  @ValidEnumValue(
      enumClass = DistributionStrategy.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STRATEGY)
  private DistributionStrategy distributionStrategy;

  @ValidEnumValue(
      enumClass = HealthStatus.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_HEALTH)
  private HealthStatus guardrailHealthStatus;

  @Size(min = 1, max = 20, message = "Number of cohorts must be between 1 and 20")
  private List<@NotBlank(message = "Cohort name cannot be blank") String> cohorts;

  @Valid private VariantWeights variantWeights;

  @Valid private Map<String, @Valid Variant> variants;

  @Valid
  @Size(max = 50, message = "Maximum 50 rule attributes allowed")
  private List<RuleAttributes> ruleAttributes;

  @Size(max = 100, message = "Maximum 100 overrides allowed")
  private List<@NotBlank(message = "Override cannot be blank") String> overrides;

  @Valid private WinningVariant winningVariant;

  @Min(value = 0, message = "Exposure must be at least 0")
  @Max(value = 100, message = "Exposure must not exceed 100")
  private Integer exposure;

  @Min(value = 0, message = "Threshold must be at least 0")
  private Long threshold;

  @Min(value = 0, message = "Start time must be a valid epoch timestamp")
  private Long startTime;

  @Min(value = 0, message = "End time must be a valid epoch timestamp")
  private Long endTime;

  @Size(max = 255, message = "Updated by must not exceed 255 characters")
  private String updatedBy;
}
