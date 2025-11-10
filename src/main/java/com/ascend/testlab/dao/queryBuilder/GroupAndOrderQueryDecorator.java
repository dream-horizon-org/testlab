package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;

/**
 * Decorator that adds GROUP BY and ORDER BY clauses to the query. Groups results and orders them by
 * created_at timestamp.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class GroupAndOrderQueryDecorator extends FilterQueryDecorator {
  /**
   * Constructs a new GroupAndOrderQueryDecorator with the given wrapped query builder.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   */
  public GroupAndOrderQueryDecorator(FilterQueryBuilder wrappedFilterQuery) {
    super(wrappedFilterQuery);
  }

  /**
   * Appends GROUP BY and ORDER BY clauses to the wrapped query.
   *
   * @return the SQL query string with GROUP BY and ORDER BY clauses
   */
  @Override
  public String buildQuery() {
    return wrappedFilterQuery.buildQuery() + ReadQuery.GROUP_BY + ReadQuery.ORDER_BY_CREATED_AT;
  }
}
