package com.ascend.testlab.dao.querybuilder.core;

import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Represents a parameterized SQL query containing both the query string and the parameters tuple to
 * be used with it. This class encapsulates the query definition ready for execution with proper SQL
 * injection protection through parameterized values.
 *
 * @param query The SQL query string with parameter placeholders ($1, $2, etc.).
 * @param tuple Tuple of parameters to be bound to the query placeholders.
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
public record ParameterizedQuery(String query, Tuple tuple) {
  /**
   * Constructs a new ParameterizedQuery with the given query and empty tuple.
   *
   * @param query the SQL query string
   */
  public ParameterizedQuery(String query) {
    this(query, Tuple.tuple());
  }

  /**
   * Gets the current parameter count from the tuple.
   *
   * @return the number of parameters in the tuple
   */
  public int getParameterCount() {
    return tuple.size();
  }

  /**
   * Gets the next parameter placeholder number (1-based) for use in SQL query string.
   *
   * @return the next parameter placeholder number
   */
  public int getNextParameterIndex() {
    return tuple.size() + 1;
  }

  /**
   * Appends a query fragment to the existing query, keeping the same tuple.
   *
   * @param queryFragment the query fragment to append
   * @return a new ParameterizedQuery with the appended query and same tuple
   */
  public ParameterizedQuery append(String queryFragment) {
    return new ParameterizedQuery(this.query + queryFragment, this.tuple);
  }

  /**
   * Appends a query fragment with a new tuple that includes all existing parameters.
   *
   * @param queryFragment the query fragment to append
   * @param newTuple the new tuple with additional parameters
   * @return a new ParameterizedQuery with the appended query and new tuple
   */
  public ParameterizedQuery append(String queryFragment, Tuple newTuple) {
    return new ParameterizedQuery(this.query + queryFragment, newTuple);
  }
}
