package com.ascend.testlab.mapper;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.Experiment;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;

import java.sql.Timestamp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentMapper {

  public static Experiment mapRowToExperiment(Row row) {
    Experiment.ExperimentBuilder builder =
        Experiment.builder()
            .projectId(row.getUUID("project_key"))
            .experimentId(row.getUUID("experiment_id"))
            .name(row.getString("name"))
            .description(row.getString("description"))
            .hypothesis(row.getString("hypothesis"))
            .status(
                row.getString("status") != null
                    ? ExperimentStatus.valueOf(row.getString("status"))
                    : null)
            .type(row.getString("type") != null ? mapExperimentType(row.getString("type")) : null)
            .guardrailHealthStatus(
                row.getString("guardrail_health_status") != null
                    ? HealthStatus.valueOf(row.getString("guardrail_health_status"))
                    : null)
            .cohorts(row.getString("cohorts"))
            .variantWeights((JsonObject) row.getJson("variant_weights"))
            .assignmentStrategy(
                row.getString("assignment_strategy") != null
                    ? AssignmentStrategy.valueOf(row.getString("assignment_strategy"))
                    : null)
            .overrides((JsonObject) row.getJson("overrides"))
            .ruleAttributes((JsonObject) row.getJson("rule_attributes"))
            .winningVariant((JsonObject) row.getJson("winning_variant"))
            .exposure(row.getInteger("exposure"))
            .threshold(row.getLong("threshold"))
            .startTime(row.getLong("start_time"))
            .endTime(row.getLong("end_time"))
            .createdBy(row.getString("created_by"))
            .createdAt(Timestamp.valueOf(row.getLocalDateTime("created_at")))
            .updatedAt(Timestamp.valueOf(row.getLocalDateTime("updated_at")))
            .nameTokens(row.getString("name_tokens"))
            .tags(row.getString("tags"))
            .owner(row.getString("owners"));

    return builder.build();
  }


  /** Map MySQL ENUM value 'A/B' to Java enum A_B */
  private static ExperimentType mapExperimentType(String dbValue) {
    if (dbValue == null || dbValue.isEmpty()) {
      return null;
    }
    // MySQL stores 'A/B' but Java enum is A_B
    String normalizedValue = dbValue.replace("/", "_");
    return ExperimentType.valueOf(normalizedValue);
  }

}
