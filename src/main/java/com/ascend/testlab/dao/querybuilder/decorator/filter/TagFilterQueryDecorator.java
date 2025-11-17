package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import com.ascend.testlab.util.CommonUtil;
import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Decorator that adds a tag filter to the query. Supports multiple tags using parameterized array.
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
   * Appends a tag filter clause to the wrapped query using parameterized array.
   *
   * @return the ParameterizedQuery with the tag filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int paramIndex = wrappedResult.getNextParameterIndex();

    // Build the parameterized query fragment using apply()
    String queryFragment = ReadQuery.TAGS_FILTER.apply(paramIndex);

    // Parse comma-separated tags and convert to array
    String[] tagArray = CommonUtil.separateCommaSeparatedString(tag).toArray(String[]::new);

    // Add the tag array parameter using type-safe addArrayOfString()
    Tuple newTuple = wrappedResult.tuple().addArrayOfString(tagArray);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
