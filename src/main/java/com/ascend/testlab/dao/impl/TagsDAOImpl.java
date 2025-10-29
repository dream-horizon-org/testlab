package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.TagsDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of the TagsDAO interface for database operations on experiment tags.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsDAO
 * @see MySQLReaderClient
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagsDAOImpl implements TagsDAO {

  private final MySQLReaderClient mySQLReaderClient;

  /**
   * {@inheritDoc}
   *
   * @param projectId the UUID of the project to fetch tags for
   * @return a Single containing a list of distinct tag strings
   * @throws com.dream11.rest.exception.RestException if the database operation fails
   */
  @Override
  public Single<List<String>> fetchTags(UUID projectId) {
    return mySQLReaderClient.fetchAll(
        ReadQuery.FETCH_TAGS,
        Tuple.tuple().addString(projectId.toString()),
        (row -> row.getString("tag")));
  }
}
