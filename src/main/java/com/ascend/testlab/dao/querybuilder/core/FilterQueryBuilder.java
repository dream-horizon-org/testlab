package com.ascend.testlab.dao.querybuilder.core;

/**
 * Interface for building filter queries for experiments.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface FilterQueryBuilder {

  /**
   * Builds and returns the SQL query result containing the query string and parameters.
   *
   * @return the constructed ParameterizedQuery with query string and parameters
   */
  ParameterizedQuery buildQuery();
}
