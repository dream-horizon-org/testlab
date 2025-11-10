package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;

/**
 * Decorator that adds a name filter to the query using full-text search.
 * Uses PostgreSQL's plainto_tsquery for text search functionality.
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
   * Appends a name filter clause to the wrapped query.
   * Escapes single quotes in the name to prevent SQL injection.
   *
   * @return the SQL query string with the name filter clause
   */
  @Override
  public String buildQuery() {
    // plainto_tsquery expects a quoted string, so we need to escape single quotes in the name
    String escapedName = name.replace("'", "''");
    String query = ReadQuery.NAME_FILTER.replace("<<NAME>>", "'" + escapedName + "'");
    return wrappedFilterQuery.buildQuery() + query;
  }
}
