package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.NameAvailabilityDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;

/**
 * Implementation of the NameAvailabilityDAO interface.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 */
public class NameAvailabilityDAOImpl implements NameAvailabilityDAO {

  /** The PostgreSQL reader client. */
  private final PgReaderClient pgReaderClient;

  /**
   * Constructor for the NameAvailabilityDAOImpl.
   *
   * @param pgReaderClient the PostgreSQL reader client
   */
  @Inject
  public NameAvailabilityDAOImpl(PgReaderClient pgReaderClient) {
    this.pgReaderClient = pgReaderClient;
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
