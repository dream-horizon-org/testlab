package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;

/**
 * Base implementation of FilterQueryBuilder that provides the initial filter experiment query.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class BaseQueryBuilder implements FilterQueryBuilder {
  /** The base filter experiment query. */
  protected final String query;

  /** Constructs a new BaseQueryBuilder with the base filter experiment query. */
  public BaseQueryBuilder() {
    this.query = ReadQuery.FILTER_EXPERIMENT;
  }

  /**
   * Returns the base filter experiment query.
   *
   * @return the base SQL query string
   */
  @Override
  public String buildQuery() {
    return query;
  }
}
