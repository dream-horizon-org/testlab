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

 /**
  * The query to fetch distinct tags for a given project key.
  * @param projectKey the project key to fetch tags for
  * @return the distinct tags for the given project key
  */
  public static final String FETCH_TAGS =
      "SELECT distinct tag FROM experiment.tags WHERE project_key = $1;";
}
