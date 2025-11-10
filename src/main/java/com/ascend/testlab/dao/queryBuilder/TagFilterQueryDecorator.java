package com.ascend.testlab.dao.queryBuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Decorator that adds a tag filter to the query. Supports multiple tags as a comma-separated list.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class TagFilterQueryDecorator extends FilterQueryDecorator {
  /** The tag value(s) to filter by (comma-separated). */
  private final String tag;

  /**
   * Constructs a new TagFilterQueryDecorator with the given wrapped query builder and tag.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param tag the tag value(s) to filter by (comma-separated)
   */
  public TagFilterQueryDecorator(FilterQueryBuilder wrappedFilterQuery, String tag) {
    super(wrappedFilterQuery);
    this.tag = tag;
  }

  /**
   * Appends a tag filter clause to the wrapped query. Formats multiple tags for use in an IN
   * clause.
   *
   * @return the SQL query string with the tag filter clause
   */
  @Override
  public String buildQuery() {
    String formattedTag = formatValuesForInClause(tag);
    String tagQuery = ReadQuery.TAGS_FILTER.replace("<<TAG>>", formattedTag);

    return wrappedFilterQuery.buildQuery() + tagQuery;
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
