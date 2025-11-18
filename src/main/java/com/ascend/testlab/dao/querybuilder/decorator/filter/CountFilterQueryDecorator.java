package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;

public class CountFilterQueryDecorator extends FilterQueryDecorator {
  /**
   * Constructs a new FilterQueryDecorator with the given wrapped query builder.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   */
  public CountFilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery) {
    super(wrappedFilterQuery);
  }

  /**
   * Appends a name filter clause to the wrapped query using parameterized query.
   *
   * @return the ParameterizedQuery with the name filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();

    String query = ReadQuery.COUNT_FILTERED_EXPERIMENTS.apply(wrappedResult.query());
    return new ParameterizedQuery(query, wrappedResult.tuple());
  }
}
