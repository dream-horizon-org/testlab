package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.TagsDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of the TagsDAO interface for database operations on experiment tags.
 *
 * @author Nithya sree
 * @version 1.0
 * @since 1.0
 * @see TagsDAO
 * @see PgReaderClient
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagsDAOImpl implements TagsDAO {

  private final PgReaderClient pgReaderClient;

  /**
   * {@inheritDoc}
   *
   * @param projectKey the project key to fetch tags for
   * @return a Single containing a list of distinct tag strings
   * @throws com.dream11.rest.exception.RestException if the database operation fails
   */
  @Override
  public Single<List<String>> fetchTags(String projectKey) {
    return pgReaderClient.fetchAll(
        ReadQuery.FETCH_TAGS,
        Tuple.tuple().addString(projectKey),
        (row -> row.getString("tag")));
  }
}
