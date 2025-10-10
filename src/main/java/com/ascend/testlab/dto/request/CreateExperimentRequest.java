package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.ExperimentStatus;
import com.ascend.testlab.constants.ExperimentType;
import com.ascend.testlab.constants.HealthStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.sql.Timestamp;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateExperimentRequest {

  private byte[] tenantId;

  private byte[] experimentId;

  @NotBlank private String name;

  @NotBlank private String description;

  private String hypothesis;

  private Map<String, Object> rulesJson;

  private ExperimentStatus status;

  private String actuals;

  private long startTime;

  private long endTime;

  private String cohortId;

  private String distributionStrategy;

  private String assignmentDomain;

  private String percentageDistribution;

  private int exposure;

  private long createdBy;

  private long threshold;

  private Boolean isExclusive;

  private HealthStatus health;

  private ExperimentType type;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private String nameTokens;
}
