package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import com.ascend.testlab.util.CommonUtil;
import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Decorator that adds an owner filter to the query. Supports multiple owners using parameterized
 * array.
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
   * Appends an owner filter clause to the wrapped query using parameterized array.
   *
   * @return the ParameterizedQuery with the owner filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int paramIndex = wrappedResult.getNextParameterIndex();

    // Build the parameterized query fragment using apply()
    String queryFragment = ReadQuery.OWNER_FILTER.apply(paramIndex);

    // Parse comma-separated owners and convert to array
    String[] ownerArray = CommonUtil.separateCommaSeparatedString(owner).toArray(String[]::new);

    // Add the owner array parameter using type-safe addArrayOfString()
    Tuple newTuple = wrappedResult.tuple().addArrayOfString(ownerArray);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
