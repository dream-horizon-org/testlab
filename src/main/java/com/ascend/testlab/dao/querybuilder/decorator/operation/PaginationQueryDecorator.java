package com.ascend.testlab.dao.querybuilder.decorator.operation;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Decorator that adds pagination (LIMIT and OFFSET) to the query using parameterized values.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class PaginationQueryDecorator extends FilterQueryDecorator {
  /** The maximum number of results to return. */
  private final int limit;

  /** The number of results to skip. */
  private final int offset;

  /**
   * Constructs a new PaginationQueryDecorator with the given wrapped query builder, limit, and
   * offset.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param limit the maximum number of results to return
   * @param offset the number of results to skip
   */
  public PaginationQueryDecorator(FilterQueryBuilder wrappedFilterQuery, int limit, int offset) {
    super(wrappedFilterQuery);
    this.limit = limit;
    this.offset = offset;
  }

  /**
   * Appends LIMIT and OFFSET clauses to the wrapped query using parameterized values.
   *
   * @return the ParameterizedQuery with pagination clauses and parameters
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int limitParamIndex = wrappedResult.getNextParameterIndex();
    int offsetParamIndex = limitParamIndex + 1;

    // Build the parameterized pagination fragment using apply()
    String queryFragment = ReadQuery.PAGINATION.apply(limitParamIndex, offsetParamIndex);

    // Add the limit and offset parameters using type-safe addInteger()
    Tuple newTuple = wrappedResult.tuple().addInteger(limit).addInteger(offset);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
