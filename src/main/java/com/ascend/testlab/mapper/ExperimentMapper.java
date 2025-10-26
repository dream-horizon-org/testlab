package com.ascend.testlab.mapper;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.dto.entity.Experiment;
import io.vertx.rxjava3.sqlclient.Row;

public class ExperimentMapper {

    public static Experiment mapRowToExperiment(Row row) {
        return Experiment.builder()
                .experiment_id(row.getLong("experiment_id"))
                .tenantId(row.getString("project_id"))
                .name(row.getString("name"))
                .description(row.getString("description"))
                .metrics(row.getString("metrics"))
                .assignmentDomain(row.getString("assignment_domain"))
                .exposure(row.getInteger("exposure"))
                .threshold(row.getInteger("threshold"))
                .type(ExperimentType.valueOf(row.getString("type")))
                .endDate(row.getLocalDateTime("end_date"))
                .tags(row.getString("tags")) // This will be JSON tags
                .owner(row.getString("owners")) // This will be JSON owners
                .createdAt(row.getLocalDateTime("created_at"))
                .updatedAt(row.getLocalDateTime("updated_at"))
                .status(ExperimentStatus.valueOf(row.getString("status")))
                .build();
    }

  public static String mapRowToTags(Row row) {
    return row.getString("tag");
  }

  public static String mapRowToOwner(Row row) {
    return row.getString("owner");
  }
}
