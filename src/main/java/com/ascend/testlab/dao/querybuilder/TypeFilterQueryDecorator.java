package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.exception.RestException;
import java.util.List;

/**
 * Decorator that adds a type filter to the query. Supports multiple types as a comma-separated
 * list.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class TypeFilterQueryDecorator extends FilterQueryDecorator {
  /** The type value(s) to filter by (comma-separated). */
  private final List<ExperimentType> type;

  /**
   * Constructs a new TypeFilterQueryDecorator with the given wrapped query builder and type.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param type the type value(s) to filter by (comma-separated)
   */
  public TypeFilterQueryDecorator(
      FilterQueryBuilder wrappedFilterQuery, List<ExperimentType> type) {
    super(wrappedFilterQuery);
    this.type = type;
  }

  /**
   * Appends a type filter clause to the wrapped query.
   *
   * @return the SQL query string with the type filter clause
   */
  @Override
  public String buildQuery() {
    try {
      String formattedType = CommonUtil.formatValuesForInClause(type);
      String query = ReadQuery.TYPE_FILTER.apply(formattedType);
      return wrappedFilterQuery.buildQuery() + query;
    } catch (Throwable e) {
      throw ErrorEnum.handleException(
          e, new RestException(ErrorEnum.REST_FILTER_EXPERIMENTS_FAILED, e));
    }
  }
}
