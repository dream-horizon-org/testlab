package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;

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
    try {
      String formattedTag = CommonUtil.formatValuesForInClause(tag);
      String tagQuery = ReadQuery.TAGS_FILTER.apply(formattedTag);

      return wrappedFilterQuery.buildQuery() + tagQuery;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
