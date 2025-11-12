package com.ascend.testlab.entity;

/**
 * Enum representing the assignment domain for experiments. Defines how variant assignment is
 * determined.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public enum AssignmentDomain {
  /** Manual variant assignment - specific users mapped to specific variants */
  MANUAL,

  /** Variant assignment based on cohort mapping */
  COHORT,

  /** Default assignment domain */
  DEFAULT
}
