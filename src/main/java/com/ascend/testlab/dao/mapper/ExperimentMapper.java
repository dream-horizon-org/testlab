package com.ascend.testlab.dao.mapper;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.dto.entity.Experiment;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper class for converting database rows to Experiment entities. Provides utility methods to
 * transform database query results into Java domain objects.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see Experiment
 */
@Slf4j
public class ExperimentMapper {

  /**
   * Maps a database row to an Experiment entity.
   *
   * <p>Extracts all experiment-related fields from the database row and constructs an Experiment
   * object. Handles type conversions including enum mapping for status, type, health status, and
   * assignment strategy. Also processes JSON fields and aggregates tags and owners from the
   * database result.
   *
   * @param row the database row containing experiment data
   * @return an Experiment object populated with data from the row
   */
  public static Experiment mapRowToExperiment(Row row) {
    Experiment.ExperimentBuilder builder =
        Experiment.builder()
            .projectId(UUID.fromString(row.getString("project_key")))
            .experimentId(UUID.fromString(row.getString("experiment_id")))
            .name(row.getString("name"))
            .description(row.getString("description"))
            .hypothesis(row.getString("hypothesis"))
            .status(
                row.getString("status") != null
                    ? ExperimentStatus.valueOf(row.getString("status"))
                    : null)
            .type(
                row.getString("type") != null
                    ? ExperimentType.fromValue(row.getString("type"))
                    : null)
            .guardrailHealthStatus(
                row.getString("guardrail_health_status") != null
                    ? HealthStatus.valueOf(row.getString("guardrail_health_status"))
                    : null)
            .cohorts(List.of(row.getArrayOfStrings("cohorts")))
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
            .tags(row.getString("tags"))
            .owner(row.getString("owners"));

    return builder.build();
  }
}
