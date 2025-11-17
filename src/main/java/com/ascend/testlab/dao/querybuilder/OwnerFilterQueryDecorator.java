package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;

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
    try {
      String formattedOwner = CommonUtil.formatValuesForInClause(owner);
      String query = ReadQuery.OWNER_FILTER.apply(formattedOwner);
      return wrappedFilterQuery.buildQuery() + query;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
