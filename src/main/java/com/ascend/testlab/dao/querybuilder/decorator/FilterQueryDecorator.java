package com.ascend.testlab.dao.querybuilder.decorator;

import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;

/**
 * Base decorator class that implements the FilterQueryBuilder interface. This class follows the
 * Decorator pattern to add functionality to query builders.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class FilterQueryDecorator implements FilterQueryBuilder {
  /** The wrapped FilterQueryBuilder instance. */
  protected final FilterQueryBuilder wrappedFilterQuery;

  /**
   * Constructs a new FilterQueryDecorator with the given wrapped query builder.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   */
  public FilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery) {
    this.wrappedFilterQuery = wrappedFilterQuery;
  }

  /**
   * Delegates the query building to the wrapped FilterQueryBuilder.
   *
   * @return the ParameterizedQuery from the wrapped builder
   */
  @Override
  public ParameterizedQuery buildQuery() {
    return wrappedFilterQuery.buildQuery();
  }
}
