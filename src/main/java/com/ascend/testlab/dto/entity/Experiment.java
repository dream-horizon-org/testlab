package com.ascend.testlab.dto.entity;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import io.vertx.core.json.JsonObject;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
  private UUID experimentId;
  private UUID projectId;
  private String name;
  private String description;
  private String hypothesis;
  private ExperimentStatus status;
  private ExperimentType type;
  private HealthStatus guardrailHealthStatus;
  private List<String> cohorts;
  private JsonObject variantWeights;
  private AssignmentStrategy assignmentStrategy;
  private JsonObject overrides;
  private JsonObject ruleAttributes;
  private JsonObject winningVariant;
  private Integer exposure;
  private Long threshold;
  private Long startTime;
  private Long endTime;
  private String createdBy;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private String tags;
  private String owner;
}
