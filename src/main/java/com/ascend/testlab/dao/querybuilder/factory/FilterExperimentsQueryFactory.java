package com.ascend.testlab.dao.querybuilder.factory;

import com.ascend.testlab.dao.querybuilder.core.BaseQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.filter.*;
import com.ascend.testlab.dao.querybuilder.decorator.operation.CountFilterQueryDecorator;
import com.ascend.testlab.dao.querybuilder.decorator.operation.GroupAndOrderQueryDecorator;
import com.ascend.testlab.dao.querybuilder.decorator.operation.PaginationQueryDecorator;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.util.CommonUtil;
import lombok.experimental.UtilityClass;

/**
 * Factory class for building filter queries for experiments. Applies decorators based on the filter
 * criteria in the request. Returns a ParameterizedQuery containing both the parameterized query and
 * the parameters.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
@UtilityClass
public final class FilterExperimentsQueryFactory {

  /**
   * Builds a parameterized SQL query based on the filter criteria in the request. Applies
   * decorators for name, type, status, tag, and owner filters, followed by grouping, ordering, and
   * pagination.
   *
   * @param projectKey the project key to filter experiments by
   * @param request the filter experiments request containing filter criteria
   * @return the ParameterizedQuery containing the parameterized SQL query and parameters
   */
  public static ParameterizedQuery buildQuery(
      String projectKey, FilterExperimentsRequest request, Boolean isPaginationReq) {
    FilterQueryBuilder baseQuery = new BaseQueryBuilder(projectKey);

    if (request.hasNameFilter()) {
      baseQuery = new NameFilterQueryDecorator(baseQuery, request.getName());
    }

    if (request.hasTypeFilter()) {
      baseQuery = new TypeFilterQueryDecorator(baseQuery, request.getTypeFilters());
    }

    if (request.hasStatusFilter()) {
      baseQuery = new StatusFilterQueryDecorator(baseQuery, request.getStatusFilters());
    }

    if (request.hasTagFilter()) {
      baseQuery = new TagFilterQueryDecorator(baseQuery, request.getTag());
    }

    if (request.hasOwnerFilter()) {
      baseQuery = new OwnerFilterQueryDecorator(baseQuery, request.getOwner());
    }
    baseQuery = new GroupAndOrderQueryDecorator(baseQuery);

    baseQuery =
        (isPaginationReq)
            ? new PaginationQueryDecorator(
                baseQuery,
                request.getLimit(),
                CommonUtil.calculateOffset(request.getPage(), request.getLimit()))
            : new CountFilterQueryDecorator(baseQuery);

    return baseQuery.buildQuery();
  }
}
