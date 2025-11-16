package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Decorator that adds a name filter to the query using full-text search. Uses PostgreSQL's
 * plainto_tsquery for text search functionality.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class NameFilterQueryDecorator extends FilterQueryDecorator {
  /** The name value to filter by. */
  private final String name;

  /**
   * Constructs a new NameFilterQueryDecorator with the given wrapped query builder and name.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param name the name value to filter by
   */
  public NameFilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery, String name) {
    super(wrappedFilterQuery);
    this.name = name;
  }

  /**
   * Appends a name filter clause to the wrapped query using parameterized query.
   *
   * @return the ParameterizedQuery with the name filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int paramIndex = wrappedResult.getNextParameterIndex();

    // Build the parameterized query fragment using apply()
    String queryFragment = ReadQuery.NAME_FILTER.apply(paramIndex);

    // Add the name parameter using type-safe addString()
    Tuple newTuple = wrappedResult.tuple().addString(name);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
