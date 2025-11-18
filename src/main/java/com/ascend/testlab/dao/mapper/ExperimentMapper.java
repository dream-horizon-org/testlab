package com.ascend.testlab.dao.mapper;

import com.ascend.testlab.constants.enums.AssignmentStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.dto.entity.Experiment;
import com.ascend.testlab.util.CommonUtil;
import io.vertx.rxjava3.sqlclient.Row;
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
            .projectKey(row.getString(Columns.PROJECT_KEY))
            .experimentId(UUID.fromString(row.getString(Columns.EXPERIMENT_ID)))
            .name(row.getString(Columns.NAME))
            .description(row.getString(Columns.DESCRIPTION))
            .hypothesis(row.getString(Columns.HYPOTHESIS))
            .status(
                row.getString(Columns.STATUS) != null
                    ? ExperimentStatus.valueOf(row.getString(Columns.STATUS))
                    : null)
            .type(
                row.getString(Columns.TYPE) != null
                    ? ExperimentType.fromValue(row.getString(Columns.TYPE))
                    : null)
            .guardrailHealthStatus(
                row.getString(Columns.GUARDRAIL_HEALTH_STATUS) != null
                    ? HealthStatus.valueOf(row.getString(Columns.GUARDRAIL_HEALTH_STATUS))
                    : null)
            .cohorts(List.of(row.getArrayOfStrings(Columns.COHORTS)))
            .variantWeights(row.getJsonObject(Columns.VARIANT_WEIGHTS))
            .assignmentStrategy(
                row.getString(Columns.ASSIGNMENT_STRATEGY) != null
                    ? AssignmentStrategy.valueOf(row.getString(Columns.ASSIGNMENT_STRATEGY))
                    : null)
            .overrides(row.getJsonObject(Columns.OVERRIDES))
            .ruleAttributes(row.getJsonObject(Columns.RULE_ATTRIBUTES))
            .winningVariant(row.getJsonObject(Columns.WINNING_VARIANT))
            .exposure(row.getInteger(Columns.EXPOSURE))
            .threshold(row.getLong(Columns.THRESHOLD))
            .startTime(row.getLong(Columns.START_TIME))
            .endTime(row.getLong(Columns.END_TIME))
            .createdBy(row.getString(Columns.CREATED_BY))
            .createdAt(row.getOffsetDateTime(Columns.CREATED_AT).toInstant())
            .updatedAt(row.getOffsetDateTime(Columns.UPDATED_AT).toInstant())
            .tags(CommonUtil.separateCommaSeparatedString(row.getString(Columns.TAGS)))
            .owner(row.getString(Columns.OWNERS));

    return builder.build();
  }
}
