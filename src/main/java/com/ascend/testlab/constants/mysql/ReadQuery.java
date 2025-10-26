package com.ascend.testlab.constants.mysql;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReadQuery {
  public static final String HEALTH_CHECK = "SELECT 1;";

  public static final String GET_EXPERIMENT =
      "SELECT experiment_id, name, description, metrics, assignment_domain, exposure, threshold, type, status, end_date, tags, created_at, updated_at "
          + "FROM experiments WHERE tenant_id = ? AND experiment_id = ?";

  public static final String NAME_FILTER = " AND name LIKE ?";
  public static final String ORDER_BY_CREATED_AT = " ORDER BY created_at DESC";

  public static final String STATUS_FILTER = " AND status in (<<STATUS>>)";

  public static final String TYPE_FILTER = " AND type in (<<TYPE>>)";

  public static final String GET_TAGS_BY_EXPERIMENT_ID =
      "SELECT tag FROM tags WHERE tenant_id = ? AND experiment_id = ? AND tags in (<<TAG>>)";

  public static final String GET_OWNER_BY_EXPERIMENT_ID =
      "SELECT owner FROM owners WHERE tenant_id = ? AND experiment_id = ?";

  public static final String FILTER_EXPERIMENT =
      "SELECT experiment_id, name, description, metrics, assignment_domain, exposure, threshold, type, status, end_date, tags, created_at, updated_at FROM experiments WHERE tenant_id = ? ";
}
