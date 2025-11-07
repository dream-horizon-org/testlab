package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AdminDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;

/**
 * Implementation of the AdminDAO interface for database operations.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see AdminDAO
 * @see PgReaderClient
 */
public class AdminDAOImpl implements AdminDAO {

  /** The PostgreSQL reader client. */
  private final PgReaderClient pgReaderClient;

  /**
   * Constructor for the AdminDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public AdminDAOImpl(PgReaderClient pgReaderClient) {
    this.pgReaderClient = pgReaderClient;
  }

  /** {@inheritDoc} */
  @Override
  public Single<List<String>> fetchTags(String projectKey) {
    return pgReaderClient.fetchAll(
        ReadQuery.FETCH_TAGS, Tuple.tuple().addString(projectKey), (row -> row.getString("tag")));
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> isExperimentNameAvailable(String projectKey, String experimentName) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.CHECK_EXPERIMENT_NAME,
            Tuple.tuple().addString(projectKey).addString(experimentName),
            row -> row.getBoolean(0))
        .map(list -> list.isEmpty() ? true : !list.get(0));
  }
}
