package com.ascend.testlab.constants;

import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * Utility class for constants.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class Constants {
  /** The name of the package. */
  public static final String PACKAGE_NAME = "com.ascend.testlab";

  /** The name of the main verticle. */
  public static final String MAIN_VERTICLE = "com.ascend.testlab.verticle.MainVerticle";

  /** The key for the application environment. */
  public static final String APP_ENV_KEY = "app.environment";

  /** The default application environment. */
  public static final String DEFAULT_APP_ENV = "dev";

  /** The maximum number of REST verticles. */
  public static final Integer MAX_NUM_REST_VERTICLES = 16;

  /* Delimiter Constants */

  /** The delimiter for comma. */
  public static final String COMMA = ",";

  /** The delimiter for colon. */
  public static final String COLON = ":";

  /** The delimiter for space. */
  public static final String SPACE = " ";

  public static final String GET_TYPE = "getType";

  public static final String GET_NAME = "getName";

  public static final String NAME = "name";

  /* Experiment Update Field Constants */

  /**
   * Set of fields that are allowed to be updated in an experiment.
   *
   * <p>Excludes primary keys, auto-managed fields, and immutable fields.
   */
  public static final Set<String> UPDATABLE_FIELDS =
      Set.of(
          "description",
          "hypothesis",
          "status",
          "type",
          "guardrail_health_status",
          "cohorts",
          "variant_weights",
          "variants",
          "distribution_strategy",
          "assignment_domain",
          "overrides",
          "rule_attributes",
          "winning_variant",
          "exposure",
          "threshold",
          "start_time",
          "end_time",
          "created_by",
          "tags",
          "owner",
          "metrics");

  /**
   * Set of fields that are NOT allowed to be updated in an experiment.
   *
   * <p>These fields are either immutable, auto-managed, or should only be set during creation.
   */
  public static final Set<String> NON_UPDATABLE_FIELDS =
      Set.of("name", "experiment_key", "project_key", "experiment_id", "created_at", "updated_at");
}
