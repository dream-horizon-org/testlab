package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.dto.request.FilterExperimentsRequest;

/**
 * Factory class for building filter queries for experiments. Applies decorators based on the filter
 * criteria in the request.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public final class FilterExperimentsQueryFactory {

  /**
   * Builds a SQL query based on the filter criteria in the request. Applies decorators for name,
   * type, status, tag, and owner filters, followed by grouping, ordering, and pagination.
   *
   * @param request the filter experiments request containing filter criteria
   * @return the constructed SQL query string
   */
  public static String buildQuery(FilterExperimentsRequest request) {
    FilterQueryBuilder baseQuery = new BaseQueryBuilder();

    if (request.hasNameFilter()) {
      baseQuery = new NameFilterQueryDecorator(baseQuery, request.getName());
    }

    if (request.hasTypeFilter()) {
      baseQuery = new TypeFilterQueryDecorator(baseQuery, request.getType());
    }

    if (request.hasStatusFilter()) {
      baseQuery = new StatusFilterQueryDecorator(baseQuery, request.getStatus());
    }

    if (request.hasTagFilter()) {
      baseQuery = new TagFilterQueryDecorator(baseQuery, request.getTag());
    }

    if (request.hasOwnerFilter()) {
      baseQuery = new OwnerFilterQueryDecorator(baseQuery, request.getOwner());
    }
    baseQuery = new GroupAndOrderQueryDecorator(baseQuery);

    // Apply pagination at the end
    int offset = (request.getPage() - 1) * request.getLimit();
    baseQuery = new PaginationQueryDecorator(baseQuery, request.getLimit(), offset);

    return baseQuery.buildQuery();
  }
}
