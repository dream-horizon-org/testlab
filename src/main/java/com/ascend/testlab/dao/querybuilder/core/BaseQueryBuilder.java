package com.ascend.testlab.dao.querybuilder.core;

import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Base implementation of FilterQueryBuilder that provides the initial filter experiment query with
 * the projectKey parameter.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 */
public class BaseQueryBuilder implements FilterQueryBuilder {
  /** The base filter experiment query. */
  protected final String query;

  /** The project key to filter experiments by. */
  protected final String projectKey;

  /**
   * Constructs a new BaseQueryBuilder with the base filter experiment query and projectKey.
   *
   * @param projectKey the project key to filter experiments by
   */
  public BaseQueryBuilder(String projectKey, String query) {
    this.query = query;
    this.projectKey = projectKey;
  }

  /**
   * Returns the base filter experiment query as a ParameterizedQuery with projectKey as the first
   * parameter.
   *
   * @return the ParameterizedQuery containing the base SQL query string and projectKey parameter
   */
  @Override
  public ParameterizedQuery buildQuery() {
    Tuple tuple = Tuple.tuple().addString(projectKey);
    return new ParameterizedQuery(query, tuple);
  }
}
