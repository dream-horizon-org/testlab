package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
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

public class ExperimentDAOImpl implements ExperimentDAO {

  @Inject private PgWriterClient pgWriterClient;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public Single<Long> create(UUID tenantId, CreateExperimentRequest request) {
    // Convert cohorts list to PostgreSQL array format
    String cohortsArray = null;
    if (request.getCohorts() != null && !request.getCohorts().isEmpty()) {
      cohortsArray = "{" + String.join(",", request.getCohorts()) + "}";
    }

    return pgWriterClient
        .execute(
            WriteQuery.INSERT_EXPERIMENT,
            Tuple.tuple()
                .addString(request.getProjectKey().toString()) // project_key
                .addString(request.getExperimentId().toString()) // experiment_id
                .addString(request.getName()) // name
                .addString(request.getDescription()) // description
                .addString(request.getHypothesis()) // hypothesis
                .addString(
                    request.getStatus() == null ? null : request.getStatus().name()) // status
                .addString(request.getType() == null ? null : request.getType().name()) // type
                .addString(
                    request.getGuardrailHealthStatus() == null
                        ? null
                        : request.getGuardrailHealthStatus().name()) // guardrail_health_status
                .addValue(cohortsArray) // cohorts as varchar array
                .addValue(request.getVariantWeights()) // variant_weights as jsonb
                .addString(
                    request.getAssignmentStrategy() == null
                        ? null
                        : request.getAssignmentStrategy().name()) // assignment_strategy
                .addValue(request.getOverrides()) // overrides as jsonb
                .addValue(request.getRuleAttributes()) // rule_attributes as jsonb
                .addValue(request.getWinningVariant()) // winning_variant as jsonb
                .addInteger(request.getExposure()) // exposure
                .addLong(request.getThreshold()) // threshold
                .addLong(request.getStartTime()) // start_time
                .addLong(request.getEndTime()) // end_time
                .addString(request.getCreatedBy()) // created_by
                .addString(request.getName())) // name_tsvector (using name for tsvector generation)
        .map(success -> success ? 1L : 0L); // Return 1L on success, 0L on failure
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
    params
        .addString(tenantId.toString())
        .addString(experimentId.toString()); // project_key, experiment_id
    return pgWriterClient.execute(connection, sb.toString(), params);
  }
}
