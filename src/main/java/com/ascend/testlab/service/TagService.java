package com.ascend.testlab.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;

public interface TagService {
  Single<Boolean> insertTags(
      SqlConnection connection, String tenantId, String experimentId, List<String> tags);

  Single<Boolean> deleteAllTags(SqlConnection connection, String tenantId, String experimentId);
}
