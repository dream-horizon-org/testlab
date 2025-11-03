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

  public static final String GET_EXPERIMENTS = "SELECT COUNT(1) FROM health_check;";
}
