package com.ascend.testlab.dto.request;

import com.ascend.testlab.constants.ExperimentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateExperimentRequest {

  @JsonProperty("description")
  private String description;

  // Prefer end_time (epoch seconds). If provided, it will be used.
  @JsonProperty("end_time")
  private Long endTime;

  @JsonProperty("status")
  private ExperimentStatus status;

  @JsonProperty("exposure")
  private Integer exposure;

  @JsonProperty("threshold")
  private Long threshold;

  @JsonProperty("assignment_domain")
  private String assignmentDomain;

  @JsonProperty("distribution_strategy")
  private String distributionStrategy;

  @JsonProperty("cohort_id")
  private String cohortId;

  @JsonProperty("actuals")
  private String actuals;

  @JsonProperty("percentage_distribution")
  private String percentageDistribution;

  @JsonProperty("name_tokens")
  private String nameTokens;

  // Tags replace existing tags transactionally
  @JsonProperty("tag")
  private List<String> tags;

  // Accept but currently ignored (no metrics column in schema)
  @JsonProperty("metrics")
  private List<String> metrics;

  // Accept ISO end_date string; if present and end_time is null, service may parse it.
  @JsonProperty("end_date")
  private String endDate;
}
