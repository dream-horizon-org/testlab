package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentDAOImpl implements ExperimentDAO {

  private final PgWriterClient pgWriterClient;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Inject
  public ExperimentDAOImpl(PgWriterClient pgWriterClient) {
    this.pgWriterClient = pgWriterClient;
  }

  @Override
  public Single<Long> create(UUID tenantId, UUID projectKey, CreateExperimentRequest request) {
    log.info(
        "DAO: Creating experiment in database, tenantId: {}, projectKey: {}, experimentId: {}, name: {}",
        tenantId,
        projectKey,
        request.getExperimentId(),
        request.getName());

    try {
      // Convert cohorts list to array
      String[] cohortsArray = null;
      if (request.getCohorts() != null && !request.getCohorts().isEmpty()) {
        cohortsArray = request.getCohorts().toArray(new String[0]);
        log.debug("Converted cohorts to array: {}", (Object) cohortsArray);
      }

      // Convert JSONB fields to JSON strings
      String variantWeightsJson = null;
      String overridesJson = null;
      String ruleAttributesJson = null;
      String winningVariantJson = null;

      try {
        if (request.getVariantWeights() != null) {
          variantWeightsJson = MAPPER.writeValueAsString(request.getVariantWeights());
        }
        if (request.getOverrides() != null) {
          overridesJson = MAPPER.writeValueAsString(request.getOverrides());
        }
        if (request.getRuleAttributes() != null) {
          ruleAttributesJson = MAPPER.writeValueAsString(request.getRuleAttributes());
        }
        if (request.getWinningVariant() != null) {
          winningVariantJson = MAPPER.writeValueAsString(request.getWinningVariant());
        }
      } catch (Exception e) {
        log.error("Failed to serialize JSONB fields: {}", e.getMessage());
        throw new RuntimeException("Failed to serialize JSONB fields", e);
      }

      log.debug(
          "Executing INSERT query for experiment: {}, projectKey: {}",
          request.getName(),
          request.getProjectKey());

      return pgWriterClient
          .execute(
              WriteQuery.INSERT_EXPERIMENT,
              Tuple.tuple()
                  .addValue(request.getProjectKey().toString()) // project_key
                  .addValue(request.getExperimentId().toString()) // experiment_id
                  .addValue(request.getName()) // name
                  .addValue(request.getDescription()) // description
                  .addValue(request.getHypothesis()) // hypothesis
                  .addValue(
                      request.getStatus() == null ? null : request.getStatus().name()) // status
                  .addValue(request.getType() == null ? null : request.getType().getValue()) // type
                  .addValue(
                      request.getGuardrailHealthStatus() == null
                          ? null
                          : request.getGuardrailHealthStatus().name()) // guardrail_health_status
                  .addValue(cohortsArray) // cohorts as varchar array
                  .addValue(variantWeightsJson) // variant_weights as jsonb string
                  .addValue(
                      request.getAssignmentStrategy() == null
                          ? null
                          : request.getAssignmentStrategy().name()) // assignment_strategy
                  .addValue(overridesJson) // overrides as jsonb string
                  .addValue(ruleAttributesJson) // rule_attributes as jsonb string
                  .addValue(winningVariantJson) // winning_variant as jsonb string
                  .addValue(request.getExposure()) // exposure
                  .addValue(request.getThreshold()) // threshold
                  .addValue(request.getStartTime()) // start_time
                  .addValue(request.getEndTime()) // end_time
                  .addValue(request.getCreatedBy())) // created_by
          .doOnSuccess(
              success ->
                  log.info(
                      "DAO: Successfully inserted experiment, projectKey: {}, experimentId: {}, rowsAffected: {}",
                      request.getProjectKey(),
                      request.getExperimentId(),
                      success))
          .doOnError(
              error ->
                  log.error(
                      "DAO: Failed to insert experiment, projectKey: {}, experimentId: {}, error: {}",
                      request.getProjectKey(),
                      request.getExperimentId(),
                      error.getMessage(),
                      error))
          .flatMap(
              success -> {
                if (success) {
                  log.info("DAO: Experiment inserted successfully, returning success indicator");
                  return Single.just(1L);
                } else {
                  log.error("DAO: Insert returned false, no rows affected");
                  return Single.error(
                      new RuntimeException("Failed to insert experiment - no rows affected"));
                }
              })
          .onErrorReturn(
              error -> {
                log.error(
                    "DAO: Error during experiment creation, projectKey: {}, experimentId: {}, error: {}",
                    request.getProjectKey(),
                    request.getExperimentId(),
                    error.getMessage());
                return 0L;
              });
    } catch (Exception e) {
      log.error(
          "DAO: Exception in create experiment, projectKey: {}, experimentId: {}, error: {}",
          request.getProjectKey(),
          request.getExperimentId(),
          e.getMessage(),
          e);
      return Single.just(0L);
    }
  }

  @Override
  public Single<Boolean> updatePartial(
      UUID projectKey, UUID experimentId, Map<String, Object> request) {
    log.info(
        "DAO: Updating experiment, projectKey: {}, experimentId: {}, fields: {}",
        projectKey,
        experimentId,
        request != null ? request.keySet() : "null");

    try {
      Map<String, Object> requestMap =
          MAPPER.convertValue(request, new TypeReference<Map<String, Object>>() {});

      // Handle end_date to end_time conversion
      if (requestMap.get("end_time") == null && requestMap.get("end_date") != null) {
        try {
          long epoch = Instant.parse((String) requestMap.get("end_date")).getEpochSecond();
          requestMap.put("end_time", epoch);
          log.debug("Converted end_date to end_time: {}", epoch);
        } catch (Exception e) {
          log.warn(
              "Failed to parse end_date: {}, error: {}",
              requestMap.get("end_date"),
              e.getMessage());
          // ignore parsing errors; end_time remains null
        }
      }

      // Define allowed columns based on new schema (excluding primary keys and auto-managed fields)
      Set<String> allowedColumns =
          Set.of(
              "name",
              "description",
              "hypothesis",
              "status",
              "type",
              "guardrail_health_status",
              "cohorts",
              "variant_weights",
              "assignment_strategy",
              "overrides",
              "rule_attributes",
              "winning_variant",
              "exposure",
              "threshold",
              "start_time",
              "end_time",
              "created_by");

      Map<String, Object> updates = new LinkedHashMap<>();
      for (Map.Entry<String, Object> e : requestMap.entrySet()) {
        String key = e.getKey();
        Object value = e.getValue();
        if (value != null && allowedColumns.contains(key)) {
          // Handle special cases for different data types
          if (key.equals("cohorts") && value instanceof java.util.List) {
            // Convert list to array
            @SuppressWarnings("unchecked")
            java.util.List<String> cohortList = (java.util.List<String>) value;
            updates.put(key, cohortList.toArray(new String[0]));
          } else if (key.equals("status")
              || key.equals("type")
              || key.equals("guardrail_health_status")
              || key.equals("assignment_strategy")) {
            // Handle enum fields - cast to appropriate enum type
            updates.put(key, value instanceof Enum ? ((Enum<?>) value).name() : value.toString());
          } else if (key.equals("variant_weights")
              || key.equals("overrides")
              || key.equals("rule_attributes")
              || key.equals("winning_variant")) {
            // JSONB fields - serialize to JSON string
            try {
              updates.put(key, MAPPER.writeValueAsString(value));
            } catch (Exception ex) {
              log.error("Failed to serialize JSONB field {}: {}", key, ex.getMessage());
              throw new RuntimeException("Failed to serialize JSONB field: " + key, ex);
            }
          } else {
            // Other fields (strings, numbers, etc.)
            updates.put(key, value);
          }
        }
      }

      if (updates.isEmpty()) {
        log.info(
            "DAO: No valid fields to update for projectKey: {}, experimentId: {}",
            projectKey,
            experimentId);
        return Single.just(true);
      }

      log.debug("DAO: Fields to update: {}", updates.keySet());

      // Build dynamic UPDATE query
      StringBuilder sb = new StringBuilder(WriteQuery.UPDATE_EXPERIMENT_PREFIX);
      Tuple params = Tuple.tuple();
      int paramIndex = 1; // Start with $1 for positional parameters
      for (Map.Entry<String, Object> e : updates.entrySet()) {
        String key = e.getKey();
        Object value = e.getValue();

        // Add type casting for enum and JSONB fields
        if (key.equals("status")) {
          sb.append(key).append(" = $").append(paramIndex).append("::experiment_status, ");
        } else if (key.equals("type")) {
          sb.append(key).append(" = $").append(paramIndex).append("::experiment_type, ");
        } else if (key.equals("guardrail_health_status")) {
          sb.append(key).append(" = $").append(paramIndex).append("::experiment_health, ");
        } else if (key.equals("assignment_strategy")) {
          sb.append(key).append(" = $").append(paramIndex).append("::experiment_strategy, ");
        } else if (key.equals("cohorts")) {
          sb.append(key).append(" = $").append(paramIndex).append("::varchar[], ");
        } else if (key.equals("variant_weights")
            || key.equals("overrides")
            || key.equals("rule_attributes")
            || key.equals("winning_variant")) {
          sb.append(key).append(" = $").append(paramIndex).append("::jsonb, ");
        } else {
          sb.append(key).append(" = $").append(paramIndex).append(", ");
        }
        params.addValue(value);
        paramIndex++;
      }

      sb.setLength(sb.length() - 2); // Remove trailing ", "
      // Add WHERE clause with positional parameters continuing from SET clause
      sb.append(" WHERE project_key = $")
          .append(paramIndex)
          .append(" AND experiment_id = $")
          .append(paramIndex + 1);
      params
          .addString(projectKey.toString())
          .addString(experimentId.toString()); // project_key, experiment_id

      log.debug("DAO: Executing UPDATE query: {}", sb.toString());

      return pgWriterClient
          .execute(sb.toString(), params)
          .doOnSuccess(
              success ->
                  log.info(
                      "DAO: Successfully updated experiment, projectKey: {}, experimentId: {}, success: {}",
                      projectKey,
                      experimentId,
                      success))
          .doOnError(
              error ->
                  log.error(
                      "DAO: Failed to update experiment, projectKey: {}, experimentId: {}, error: {}",
                      projectKey,
                      experimentId,
                      error.getMessage(),
                      error))
          .onErrorReturn(
              error -> {
                log.error(
                    "DAO: Returning false for failed experiment update, projectKey: {}, experimentId: {}, error: {}",
                    projectKey,
                    experimentId,
                    error.getMessage());
                return false;
              });
    } catch (Exception e) {
      log.error(
          "DAO: Exception in update experiment, projectKey: {}, experimentId: {}, error: {}",
          projectKey,
          experimentId,
          e.getMessage(),
          e);
      return Single.just(false);
    }
  }
}
