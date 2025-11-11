package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Decorator that adds a status filter to the query. Supports multiple statuses as a comma-separated
 * list.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class StatusFilterQueryDecorator extends FilterQueryDecorator {
  /** The status value(s) to filter by (comma-separated). */
  private final String status;

  /**
   * Constructs a new StatusFilterQueryDecorator with the given wrapped query builder and status.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param status the status value(s) to filter by (comma-separated)
   */
  public StatusFilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery, String status) {
    super(wrappedFilterQuery);
    this.status = status;
  }

  /**
   * Appends a status filter clause to the wrapped query. Formats multiple statuses for use in an IN
   * clause.
   *
   * @return the SQL query string with the status filter clause
   */
  @Override
  public String buildQuery() {
    try {
      String formattedStatus = formatValuesForInClause(status);
      String query = ReadQuery.STATUS_FILTER.apply(formattedStatus);
      return wrappedFilterQuery.buildQuery() + query;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }

  /**
   * Formats comma-separated values for use in a SQL IN clause. Trims whitespace, filters empty
   * values, and wraps each value in single quotes.
   *
   * @param values comma-separated string of values
   * @return formatted string for SQL IN clause (e.g., "'value1', 'value2'")
   */
  private String formatValuesForInClause(String values) {
    return Arrays.stream(values.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> "'" + s + "'")
        .collect(Collectors.joining(", "));
  }
}
