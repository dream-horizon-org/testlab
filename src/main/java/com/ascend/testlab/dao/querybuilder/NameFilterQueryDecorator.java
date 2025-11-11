package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;

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
   * Appends a name filter clause to the wrapped query. Escapes single quotes in the name to prevent
   * SQL injection.
   *
   * @return the SQL query string with the name filter clause
   */
  @Override
  public String buildQuery() {
    try {
      // plainto_tsquery expects a quoted string, so we need to escape single quotes in the name
      String query = ReadQuery.NAME_FILTER.apply(name);
      return wrappedFilterQuery.buildQuery() + query;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
