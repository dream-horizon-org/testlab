package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.ascend.testlab.dto.response.CreateExperimentResponse;
import com.ascend.testlab.service.ExperimentService;
import com.ascend.testlab.service.TagService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentServiceImpl implements ExperimentService {
  private final ExperimentDAO experimentDAO;
  private final TagService tagService;
  private final MySQLWriterClient mySQLWriterClient;

  @Override
  public Single<CreateExperimentResponse> create(UUID tenantId, CreateExperimentRequest request) {
    log.info("Creating experiment request {} ", request);
    String tenant = request.getTenantId().toString();
    UUID experimentId = UUID.randomUUID();
    request.setExperimentId(experimentId);

    return mySQLWriterClient
        .executeWithTransaction(
            (SqlConnection conn) ->
                experimentDAO
                    .create(tenantId, request)
                    .flatMap(
                        id ->
                            tagService
                                .insertTags(
                                    conn, tenant, experimentId.toString(), request.getTags())
                                .map(__ -> id))
                    .toMaybe())
        .toSingle()
        .map(id -> new CreateExperimentResponse(id, true, "created"));
  }

  @Override
  public Single<Boolean> update(UUID tenantId, UUID experimentId, Map<String, Object> request) {
    String tenant = tenantId.toString();
    String exp = experimentId.toString();
    return mySQLWriterClient
        .executeWithTransaction(
            (SqlConnection conn) ->
                experimentDAO
                    .updatePartial(conn, tenantId, experimentId, request)
                    .flatMap(
                        ok -> {
                          Object tags = request.get("tag");
                          if (tags instanceof java.util.List<?> listTags) {
                            @SuppressWarnings("unchecked")
                            java.util.List<String> tagList = (java.util.List<String>) listTags;
                            return tagService
                                .deleteAllTags(conn, tenant, exp)
                                .flatMap(__ -> tagService.insertTags(conn, tenant, exp, tagList));
                          }
                          return Single.just(ok);
                        })
                    .toMaybe())
        .toSingle();
  }
}
