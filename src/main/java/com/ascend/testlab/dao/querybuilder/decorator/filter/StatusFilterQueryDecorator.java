package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;

/**
 * Decorator that adds a status filter to the query. Supports multiple statuses using parameterized
 * array.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class StatusFilterQueryDecorator extends FilterQueryDecorator {
  /** The status value(s) to filter by. */
  private final List<ExperimentStatus> status;

  /**
   * Constructs a new StatusFilterQueryDecorator with the given wrapped query builder and status.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param status the status value(s) to filter by
   */
  public StatusFilterQueryDecorator(
      FilterQueryBuilder wrappedFilterQuery, List<ExperimentStatus> status) {
    super(wrappedFilterQuery);
    this.status = status;
  }

  /**
   * Appends a status filter clause to the wrapped query using parameterized array.
   *
   * @return the ParameterizedQuery with the status filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int paramIndex = wrappedResult.getNextParameterIndex();

    // Build the parameterized query fragment using apply()
    String queryFragment = ReadQuery.STATUS_FILTER.apply(paramIndex);

    // Convert enum list to string array for PostgreSQL
    String[] statusArray = status.stream().map(ExperimentStatus::toString).toArray(String[]::new);

    // Add the status array parameter using type-safe addArrayOfString()
    Tuple newTuple = wrappedResult.tuple().addArrayOfString(statusArray);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
