package com.ascend.testlab.constants.postgresql;

import lombok.experimental.UtilityClass;

/**
 * Utility class for PostgreSQL reader queries.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class ReadQuery {

  /** The health check query to check if the PostgreSQL reader database is connected. */
  public static final String HEALTH_CHECK = "SELECT 1;";


    public static final String GET_EXPERIMENT =
            "SELECT e.project_id, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type, "
                    + "e.guardrail_health_status, e.cohorts, e.variant_weights, e.assignment_strategy, e.overrides, "
                    + "e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time, "
                    + "e.created_by, e.created_at, e.updated_at, e.name_tokens, "
                    + "string_agg(DISTINCT t.tag, ',') as tags, "
                    + "string_agg(DISTINCT o.owner, ',') as owners "
                    + "FROM experiments e "
                    + "LEFT JOIN tags t ON e.project_id = t.project_id AND e.experiment_id = t.experiment_id "
                    + "LEFT JOIN owners o ON e.project_id = o.project_id AND e.experiment_id = o.experiment_id "
                    + "WHERE e.project_id = $1::uuid AND e.experiment_id = $2::uuid "
                    + "GROUP BY e.project_id, e.experiment_id";

    public static final String NAME_FILTER = " AND e.name ILIKE %s";
    public static final String GROUP_BY = " GROUP BY e.project_id, e.experiment_id";
    public static final String ORDER_BY_CREATED_AT = " ORDER BY e.created_at DESC";

    public static final String STATUS_FILTER = " AND e.status in (<<STATUS>>)";

    public static final String TYPE_FILTER = " AND e.type in (<<TYPE>>)";

    public static final String GET_EXPERIMENT_BY_TAGS_FILTER =
            "SELECT DISTINCT experiment_id FROM tags WHERE project_id = $1::uuid AND tag in (<<TAG>>)";

    public static final String GET_EXPERIMENT_BY_OWNER_FILTER =
            "SELECT DISTINCT experiment_id FROM owners WHERE project_id = $1::uuid AND owner in (<<OWNER>>)";

    public static final String FILTER_EXPERIMENT =
            "SELECT e.project_id, e.experiment_id, e.name, e.description, e.hypothesis, e.status, e.type, "
                    + "e.guardrail_health_status, cohorts,  e.variant_weights, e.assignment_strategy, e.overrides, "
                    + "e.rule_attributes, e.winning_variant, e.exposure, e.threshold, e.start_time, e.end_time, "
                    + "e.created_by, e.created_at, e.updated_at, e.name_tokens, "
                    + "string_agg(DISTINCT t.tag, ',') as tags, "
                    + "string_agg(DISTINCT o.owner, ',') as owners "
                    + "FROM experiments e "
                    + "LEFT JOIN tags t ON e.project_id = t.project_id AND e.experiment_id = t.experiment_id "
                    + "LEFT JOIN owners o ON e.project_id = o.project_id AND e.experiment_id = o.experiment_id "
                    + "WHERE e.project_id = $1::uuid ";

}
