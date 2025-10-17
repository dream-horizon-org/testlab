package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.mysql.MySQLColumn;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.TagsDAO;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagsDAOImpl implements TagsDAO {

  private final MySQLReaderClient mySQLReaderClient;

  @Override
  public Single<List<String>> fetchTags(UUID projectId) {
    return mySQLReaderClient.fetchAll(
        ReadQuery.FETCH_TAGS,
        Tuple.tuple().addUUID(projectId),
        (row -> row.getString(MySQLColumn.TAG.getColumn())));
  }
}
