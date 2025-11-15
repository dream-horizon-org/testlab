package com.ascend.testlab.constants.enums;

/**
 * Enum representing the experiment status.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
// TODO: enhance documentation to show allowed state transitions
public enum ExperimentStatus {
  /** The experiment is live and actively running. */
  LIVE,

  /** The experiment is paused but can be resumed. */
  PAUSED,

  /** The experiment is in draft state and not yet live. */
  DRAFT,

  /** The experiment has concluded and reached its end state. */
  CONCLUDED,

  /** The experiment has been terminated and is no longer active. */
  TERMINATED
}
