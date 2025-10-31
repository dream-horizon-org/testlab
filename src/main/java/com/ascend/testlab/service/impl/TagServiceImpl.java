package com.ascend.testlab.service.impl;

import com.ascend.testlab.dao.TagDAO;
import com.ascend.testlab.service.TagService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagServiceImpl implements TagService {

  private final TagDAO tagDAO;

  @Override
  public Single<Boolean> insertTags(
      SqlConnection connection, String tenantId, String experimentId, List<String> tags) {
    return tagDAO.insertTags(connection, tenantId, experimentId, tags);
  }

  @Override
  public Single<Boolean> deleteAllTags(
      SqlConnection connection, String tenantId, String experimentId) {
    return tagDAO.deleteAllTags(connection, tenantId, experimentId);
  }
}
