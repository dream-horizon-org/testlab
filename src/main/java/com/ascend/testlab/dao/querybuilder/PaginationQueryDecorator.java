package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;

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
    try {
      String pagination = ReadQuery.PAGINATION.apply(limit, offset);
      return wrappedFilterQuery.buildQuery() + pagination;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
