package com.ascend.testlab.constants.mysql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";

  public static final String GET_EXPERIMENT =
      "SELECT e.project_id, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type, "
          + "e.guardrail_health_status, e.cohorts, e.variant_weights, e.assignment_strategy, e.overrides, "
          + "e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time, "
          + "e.created_by, e.created_at, e.updated_at, e.name_tokens, "
          + "GROUP_CONCAT(DISTINCT t.tag) as tags, "
          + "GROUP_CONCAT(DISTINCT o.owner) as owners "
          + "FROM experiments e "
          + "LEFT JOIN tags t ON e.project_id = t.project_id AND e.experiment_id = t.experiment_id "
          + "LEFT JOIN owners o ON e.project_id = o.project_id AND e.experiment_id = o.experiment_id "
          + "WHERE e.project_id = ? AND e.experiment_id = ? "
          + "GROUP BY e.project_id, e.experiment_id";

  public static final String NAME_FILTER = " AND e.name LIKE ?";
  public static final String GROUP_BY = " GROUP BY e.project_id, e.experiment_id";
  public static final String ORDER_BY_CREATED_AT = " ORDER BY created_at DESC";

  public static final String STATUS_FILTER = " AND e.status in (<<STATUS>>)";

  public static final String TYPE_FILTER = " AND e.type in (<<TYPE>>)";

  public static final String GET_EXPERIMENT_BY_TAGS_FILTER =
      "SELECT DISTINCT experiment_id FROM tags WHERE project_id = ? AND tag in (<<TAG>>)";

  public static final String GET_EXPERIMENT_BY_OWNER_FILTER =
      "SELECT DISTINCT experiment_id FROM owners WHERE project_id = ? AND owner in (<<OWNER>>)";

  public static final String FILTER_EXPERIMENT =
      "SELECT e.project_id, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type, "
          + "e.guardrail_health_status,  e.variant_weights, e.assignment_strategy, e.overrides, "
          + "e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time, "
          + "e.created_by, e.created_at, e.updated_at, e.name_tokens, "
          + "GROUP_CONCAT(DISTINCT t.tag) as tags, "
          + "GROUP_CONCAT(DISTINCT o.owner) as owners "
          + "FROM experiments e "
          + "LEFT JOIN tags t ON e.project_id = t.project_id AND e.experiment_id = t.experiment_id "
          + "LEFT JOIN owners o ON e.project_id = o.project_id AND e.experiment_id = o.experiment_id "
          + "WHERE e.project_id = ? ";
}
