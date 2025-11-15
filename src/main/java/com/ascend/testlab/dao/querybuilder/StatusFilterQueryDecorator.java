package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;

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
      String formattedStatus = CommonUtil.formatValuesForInClause(status);
      String query = ReadQuery.STATUS_FILTER.apply(formattedStatus);
      return wrappedFilterQuery.buildQuery() + query;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
