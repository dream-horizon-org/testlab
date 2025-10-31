package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.TagDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;

public class TagDAOImpl implements TagDAO {

  @Inject private PgWriterClient pgWriterClient;

  @Override
  public Single<Boolean> insertTags(
      SqlConnection connection, String tenantId, String experimentId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return Single.just(true);
    }
    List<Tuple> params = new ArrayList<>();
    for (String tag : tags) {
      params.add(Tuple.tuple().addString(tenantId).addString(experimentId).addString(tag));
    }
    return pgWriterClient.executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_TAG, params);
  }

  @Override
  public Single<Boolean> deleteAllTags(
      SqlConnection connection, String tenantId, String experimentId) {
    return pgWriterClient.execute(
        connection,
        WriteQuery.DELETE_EXPERIMENT_TAGS,
        Tuple.tuple().addString(tenantId).addString(experimentId));
  }
}
