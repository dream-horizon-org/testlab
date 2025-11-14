package com.ascend.testlab.constants.enums;

/**
 * Enum representing the health status of the experiment.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public enum HealthStatus {
  /** Indicates a warning status for the experiment health check. */
  WARNING,

  /** Indicates the experiment health check is passing. */
  PASSING,

  /** Indicates no health checks are available for the experiment. */
  NO_CHECKS_AVAILABLE,

  /** Indicates the experiment health check has failed. */
  FAILED
}
