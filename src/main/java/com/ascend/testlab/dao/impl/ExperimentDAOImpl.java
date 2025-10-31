package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.mysql.MySQLWriterClient;
import com.ascend.testlab.constants.mysql.WriteQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExperimentDAOImpl implements ExperimentDAO {
  private final MySQLWriterClient mySQLWriterClient;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public Single<Long> create(UUID tenantId, CreateExperimentRequest request) {
    return mySQLWriterClient
        .executeWithTransaction(
            (SqlConnection conn) ->
                mySQLWriterClient
                    .executeAndGenerateId(
                        conn,
                        WriteQuery.INSERT_EXPERIMENT,
                        Tuple.tuple()
                            .addString(request.getTenantId().toString())
                            .addString(request.getExperimentId().toString())
                            .addString(request.getName())
                            .addString(request.getDescription())
                            .addString(null)
                            .addValue(request.getRulesJson())
                            .addString(request.getActuals())
                            .addString(
                                request.getStatus() == null ? null : request.getStatus().name())
                            .addLong(request.getStartTime())
                            .addLong(request.getEndTime())
                            .addString(request.getCohortId())
                            .addString(request.getDistributionStrategy())
                            .addString(request.getAssignmentDomain())
                            .addString(request.getPercentageDistribution())
                            .addInteger(request.getExposure())
                            .addLong(request.getCreatedBy())
                            .addLong(request.getThreshold())
                            .addBoolean(request.getIsExclusive())
                            .addString(
                                request.getHealth() == null ? null : request.getHealth().name())
                            .addString(request.getType() == null ? null : request.getType().name())
                            .addString(request.getNameTokens()))
                    .toMaybe())
        .toSingle();
  }

  @Override
  public Single<Boolean> updatePartial(
      SqlConnection connection, UUID tenantId, UUID experimentId, Map<String, Object> request) {
    Map<String, Object> requestMap =
        MAPPER.convertValue(request, new TypeReference<Map<String, Object>>() {});

    if (requestMap.get("end_time") == null && requestMap.get("end_date") != null) {
      try {
        long epoch = Instant.parse((String) requestMap.get("end_date")).getEpochSecond();
        requestMap.put("end_time", epoch);
      } catch (Exception ignored) {
        // ignore parsing errors; end_time remains null
      }
    }

    Set<String> allowedColumns =
        Set.of(
            "description",
            "end_time",
            "status",
            "exposure",
            "threshold",
            "assignment_domain",
            "distribution_strategy",
            "cohort_id",
            "actuals",
            "percentage_distribution",
            "name_tokens");

    Map<String, Object> updates = new LinkedHashMap<>();
    for (Map.Entry<String, Object> e : requestMap.entrySet()) {
      String key = e.getKey();
      Object value = e.getValue();
      if (value != null && allowedColumns.contains(key)) {
        updates.put(key, value instanceof Enum ? ((Enum<?>) value).name() : value);
      }
    }

    if (updates.isEmpty()) {
      return Single.just(true);
    }

    StringBuilder sb = new StringBuilder(WriteQuery.UPDATE_EXPERIMENT_PREFIX);
    Tuple params = Tuple.tuple();
    for (Map.Entry<String, Object> e : updates.entrySet()) {
      sb.append(e.getKey()).append(" = ?, ");
      params.addValue(e.getValue());
    }
    sb.setLength(sb.length() - 2);
    sb.append(WriteQuery.UPDATE_EXPERIMENT_SUFFIX);
    params.addString(tenantId.toString()).addString(experimentId.toString());
    return mySQLWriterClient.execute(connection, sb.toString(), params);
  }
}
