package com.ascend.testlab.dto.request;

import com.ascend.testlab.annotations.ValidCreateExperiment;
import com.ascend.testlab.annotations.ValidEnumValue;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.dto.entity.experiment.Metrics;
import com.ascend.testlab.dto.entity.experiment.Overrides;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.entity.experiment.Variant;
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
 * DTO for creating a new experiment with comprehensive field validation.
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Data
@ValidCreateExperiment
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateExperimentRequest {

  @NotBlank(message = "Experiment name is required")
  @Size(min = 1, max = 64, message = "Name must be between 1 and 64 characters")
  private String name;

  @Size(max = 255, message = "Description must not exceed 255 characters")
  private String description;

  @Size(max = 1000, message = "Hypothesis must not exceed 1000 characters")
  private String hypothesis;

  @NotNull(message = "Status is required")
  @ValidEnumValue(
      enumClass = ExperimentStatus.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STATUS)
  private String status;

  @NotNull(message = "Experiment type is required")
  private String type;

  @NotNull(message = "ExperimentStrategy is required")
  @ValidEnumValue(
      enumClass = DistributionStrategy.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_EXPERIMENT_STRATEGY)
  private String distributionStrategy;

  @NotNull(message = "assignmentDomain is required")
  @ValidEnumValue(
      enumClass = AssignmentDomain.class,
      method = Constants.NAME,
      message = ErrorMessages.INVALID_ASSIGNMENT_DOMAIN)
  private String assignmentDomain;

  private String experimentKey;

  private Metrics metrics;

  private List<String> cohorts;

  private List<String> owners;

  @Valid private VariantWeights variantWeights;

  @Valid
  @NotNull(message = "Variant is required")
  private Map<String, @Valid Variant> variants;

  @Valid private List<RuleAttributes> ruleAttributes;

  private Overrides overrides;

  private List<@NotBlank(message = "Tag cannot be blank") String> tags;

  @Min(value = 1, message = "Exposure must be at least 1")
  @Max(value = 100, message = "Exposure must not exceed 100")
  private int exposure = 100;

  @Min(value = 0, message = "Threshold must be at least 0")
  private long threshold;

  @Min(value = 0, message = "Start time must be a valid epoch timestamp")
  private Long startTime;

  private Long endTime = null;

  @NotBlank(message = "Created by is required")
  @Size(max = 255, message = "Created by must not exceed 255 characters")
  private String createdBy;

  private long createdAt;

  private long updatedAt;
}
