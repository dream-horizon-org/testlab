package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;

/**
 * Decorator that adds pagination (LIMIT and OFFSET) to the query.
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
   * Appends LIMIT and OFFSET clauses to the wrapped query.
   *
   * @return the SQL query string with pagination clauses
   */
  @Override
  public String buildQuery() {
    String pagination =
        ReadQuery.PAGINATION
            .replace("<<LIMIT>>", String.valueOf(limit))
            .replace("<<OFFSET>>", String.valueOf(offset));
    return wrappedFilterQuery.buildQuery() + pagination;
  }
}
