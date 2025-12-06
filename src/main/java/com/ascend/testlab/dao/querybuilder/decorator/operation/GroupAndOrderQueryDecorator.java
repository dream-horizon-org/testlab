package com.ascend.testlab.dao.querybuilder.decorator.operation;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;

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
   * @return the ParameterizedQuery with GROUP BY and ORDER BY clauses
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    String queryFragment = ReadQuery.GROUP_BY + ReadQuery.ORDER_BY_CREATED_AT;
    return wrappedResult.append(queryFragment);
  }
}
