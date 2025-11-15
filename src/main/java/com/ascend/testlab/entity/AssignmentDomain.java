package com.ascend.testlab.entity;

/**
 * Enum representing the allocation domain for experiments. Defines how variant allocation is
 * determined.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 */
public enum AssignmentDomain {
  // todo: change these names
  /** Manual variant allocation - specific users mapped to specific variants */
  MANUAL,

  /** Variant allocation based on cohort mapping */
  COHORT
}
