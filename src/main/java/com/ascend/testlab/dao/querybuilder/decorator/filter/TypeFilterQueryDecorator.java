package com.ascend.testlab.dao.querybuilder.decorator.filter;

import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.querybuilder.core.FilterQueryBuilder;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.decorator.FilterQueryDecorator;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;

/**
 * Decorator that adds a type filter to the query. Supports multiple types using parameterized
 * array.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class TypeFilterQueryDecorator extends FilterQueryDecorator {
  /** The type value(s) to filter by. */
  private final List<ExperimentType> type;

  /**
   * Constructs a new TypeFilterQueryDecorator with the given wrapped query builder and type.
   *
   * @param wrappedFilterQuery the FilterQueryBuilder to wrap
   * @param type the type value(s) to filter by
   */
  public TypeFilterQueryDecorator(
      FilterQueryBuilder wrappedFilterQuery, List<ExperimentType> type) {
    super(wrappedFilterQuery);
    this.type = type;
  }

  /**
   * Appends a type filter clause to the wrapped query using parameterized array.
   *
   * @return the ParameterizedQuery with the type filter clause and parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    ParameterizedQuery wrappedResult = wrappedFilterQuery.buildQuery();
    int paramIndex = wrappedResult.getNextParameterIndex();

    // Build the parameterized query fragment using apply()
    String queryFragment = ReadQuery.TYPE_FILTER.apply(paramIndex);

    // Convert enum list to string array for PostgreSQL
    String[] typeArray = type.stream().map(ExperimentType::toString).toArray(String[]::new);

    // Add the type array parameter using type-safe addArrayOfString()
    Tuple newTuple = wrappedResult.tuple().addArrayOfString(typeArray);

    return wrappedResult.append(queryFragment, newTuple);
  }
}
