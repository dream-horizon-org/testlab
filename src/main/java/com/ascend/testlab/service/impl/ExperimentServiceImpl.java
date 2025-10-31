package com.ascend.testlab.service.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentServiceImpl implements ExperimentService {

  @Inject private ExperimentDAO experimentDAO;

  @Inject private TagService tagService;

  @Inject private PgWriterClient pgWriterClient;

  @Override
  public Single<CreateExperimentResponse> create(UUID tenantId, CreateExperimentRequest request) {
    log.info("Creating experiment request {} ", request);

    // Set project_key and experiment_id - project_key comes from header (tenant_id)
    request.setProjectKey(tenantId);
    UUID experimentId = UUID.randomUUID();
    UUID projectKey = UUID.randomUUID();
    request.setExperimentId(experimentId);
    request.setProjectKey(projectKey);

    return experimentDAO
        .create(tenantId, request)
        .map(id -> new CreateExperimentResponse(id, true, "created"));
  }

  @Override
  public Single<Boolean> update(UUID tenantId, UUID experimentId, Map<String, Object> request) {
    String tenant = tenantId.toString();
    String exp = experimentId.toString();
    return pgWriterClient
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
