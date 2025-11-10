package com.ascend.testlab.dao.queryBuilder;

/**
 * Interface for building filter queries for experiments.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public interface FilterQueryBuilder {

  /**
   * Builds and returns the SQL query string.
   *
   * @return the constructed SQL query string
   */
  String buildQuery();
}
