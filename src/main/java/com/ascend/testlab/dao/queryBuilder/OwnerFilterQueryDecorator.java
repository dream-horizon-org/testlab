package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Decorator that adds an owner filter to the query. Supports multiple owners as a comma-separated
 * list.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class OwnerFilterQueryDecorator extends FilterQueryDecorator {
  /** The owner value(s) to filter by (comma-separated). */
  private final String owner;

  /**
   * Constructs a new OwnerFilterQueryDecorator with the given wrapped query builder and owner.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param owner the owner value(s) to filter by (comma-separated)
   */
  public OwnerFilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery, String owner) {
    super(wrappedFilterQuery);
    this.owner = owner;
  }

  /**
   * Appends an owner filter clause to the wrapped query. Formats multiple owners for use in an IN
   * clause.
   *
   * @return the SQL query string with the owner filter clause
   */
  @Override
  public String buildQuery() {
    String formattedOwner = formatValuesForInClause(owner);
    String query = ReadQuery.GET_EXPERIMENT_BY_OWNER_FILTER.replace("<<OWNER>>", formattedOwner);
    return wrappedFilterQuery.buildQuery() + query;
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
