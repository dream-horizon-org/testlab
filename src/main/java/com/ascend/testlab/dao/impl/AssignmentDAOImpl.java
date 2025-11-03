package com.ascend.testlab.dao.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.policy.WritePolicy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.mysql.MySQLReaderClient;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.mysql.ReadQuery;
import com.ascend.testlab.dao.AssignmentDAO;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.RuleAttributes;
import com.ascend.testlab.entity.VariantWeights;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AssignmentDAOImpl implements AssignmentDAO {

  private final MySQLReaderClient mySQLReaderClient;
  private final AerospikeClient aerospikeClient;
  private final ObjectMapper objectMapper;

  private static final String AEROSPIKE_NAMESPACE = "testlab";
  private static final String USER_ASSIGNMENTS_SET = "user_assignments";
  private static final String VARIANT_COUNTS_SET = "variant_counts";
  private static final String USER_LOCKS_SET = "user_locks";
  private static final int LOCK_EXPIRY_SECONDS = 10;

  @Override
  public Single<List<Experiment>> fetchActiveExperiments(UUID tenantId) {

    long currentTime = System.currentTimeMillis();
    Tuple params = Tuple.of(tenantId.toString(), currentTime, currentTime);

    return mySQLReaderClient
        .fetchAll(ReadQuery.GET_EXPERIMENTS, params, this::mapRowToExperiment)
        .doOnSuccess(
            experiments ->
                log.debug(
                    "Fetched {} active experiments for tenant {}", experiments.size(), tenantId))
        .onErrorReturn(
            error -> {
              log.error("Error fetching active experiments for tenant {}", tenantId, error);
              return new ArrayList<>();
            });
  }

  @Override
  public Single<List<UserExperimentMap>> getUserAssignments(String userId, UUID tenantId) {

    return getUserAssignmentsFromAerospike(userId, tenantId)
        .doOnError(
            error -> {
              log.warn(
                  "Error fetching from Aerospike, falling back to MySQL: {}", error.getMessage());
            });
  }

  @Override
  public Single<Boolean> insertUserAssignments(
      String userId, UUID tenantId, List<UserExperimentMap> assignments) {

    if (assignments == null || assignments.isEmpty()) {
      log.debug("No assignments to insert for user {}", userId);
      return Single.just(true);
    }

    return insertUserAssignmentsToAerospike(userId, tenantId, assignments).onErrorReturnItem(false);
  }

  @Override
  public Single<Long> incrementVariantCount(UUID experimentId, String variantName) {
    String asKey = experimentId.toString() + ":" + variantName;
    Key key = new Key(AEROSPIKE_NAMESPACE, VARIANT_COUNTS_SET, asKey);

    WritePolicy policy = new WritePolicy();
    policy.expiration = -1;

    Operation incrementOp = Operation.add(new Bin("count", 1));
    Operation getOp = Operation.get("count");

    return aerospikeClient
        .operate(policy, key, incrementOp, getOp)
        .map(
            record -> {
              Long count = record.getLong("count");
              log.debug(
                  "Incremented variant count for {}:{} to {}", experimentId, variantName, count);
              return count != null ? count : 1L;
            })
        .onErrorReturnItem(1L);
  }

  @Override
  public Single<Boolean> checkThreshold(Experiment experiment) {

    // Get current total count from Aerospike
    return getTotalVariantCount(experiment)
        .map(
            currentCount -> {
              boolean underThreshold = currentCount < experiment.getThreshold();
              log.debug(
                  "Experiment {} - current: {}, threshold: {}, under: {}",
                  experiment.getExperimentId(),
                  currentCount,
                  experiment.getThreshold(),
                  underThreshold);
              return underThreshold;
            })
        .onErrorReturnItem(true); // On error, allow assignment
  }

  @Override
  public Single<Boolean> acquireUserLock(String userId, UUID tenantId) {
    String lockKey = tenantId.toString() + ":" + userId;
    Key key = new Key(AEROSPIKE_NAMESPACE, USER_LOCKS_SET, lockKey);

    WritePolicy policy = new WritePolicy();
    policy.expiration = LOCK_EXPIRY_SECONDS;

    Bin lockBin = new Bin("locked", System.currentTimeMillis());

    return aerospikeClient
        .put(policy, key, lockBin)
        .map(
            result -> {
              log.debug("Acquired lock for user {} in tenant {}", userId, tenantId);
              return true;
            })
        .onErrorReturnItem(false);
  }

  @Override
  public Single<Boolean> releaseUserLock(String userId, UUID tenantId) {
    String lockKey = tenantId.toString() + ":" + userId;
    Key key = new Key(AEROSPIKE_NAMESPACE, USER_LOCKS_SET, lockKey);

    WritePolicy policy = new WritePolicy();

    return aerospikeClient
        .delete(policy, key)
        .doOnSuccess(
            result -> log.debug("Released lock for user {} in tenant {}", userId, tenantId))
        .onErrorReturnItem(true); // Even if delete fails, consider it released
  }

  @Override
  public Single<List<UserExperimentMap>> fetchConcludedExperiments(
      UUID tenantId, String apiPath, List<String> entities) {

    return Single.just(new ArrayList<>());
  }

  private Single<List<UserExperimentMap>> getUserAssignmentsFromAerospike(
      String userId, UUID tenantId) {

    String asKey = tenantId.toString() + ":" + userId;
    Key key = new Key(AEROSPIKE_NAMESPACE, USER_ASSIGNMENTS_SET, asKey);

    return aerospikeClient
        .get(null, key, "assignments")
        .map(
            record -> {
              String assignmentsJson = record.getString("assignments");
              if (assignmentsJson == null || assignmentsJson.isEmpty()) {
                return new ArrayList<UserExperimentMap>();
              }

              List<UserExperimentMap> assignments =
                  objectMapper.readValue(
                      assignmentsJson, new TypeReference<List<UserExperimentMap>>() {});

              log.debug(
                  "Fetched {} assignments from Aerospike for user {}", assignments.size(), userId);
              return assignments;
            })
        .onErrorReturn(
            error -> {
              log.debug("No assignments found in Aerospike for user {}", userId);
              return new ArrayList<>();
            });
  }

  private Single<Boolean> insertUserAssignmentsToAerospike(
      String userId, UUID tenantId, List<UserExperimentMap> assignments) {

    return getUserAssignmentsFromAerospike(userId, tenantId)
        .map(
            existing -> {
              List<UserExperimentMap> merged = new ArrayList<>(existing);
              merged.addAll(assignments);
              return merged;
            })
        .onErrorReturn(error -> new ArrayList<>(assignments))
        .flatMap(
            allAssignments -> {
              try {
                String asKey = tenantId.toString() + ":" + userId;
                Key key = new Key(AEROSPIKE_NAMESPACE, USER_ASSIGNMENTS_SET, asKey);

                String assignmentsJson = objectMapper.writeValueAsString(allAssignments);

                WritePolicy policy = new WritePolicy();
                policy.expiration = -1; // Never expire

                Bin bin = new Bin("assignments", assignmentsJson);

                return aerospikeClient.put(policy, key, bin).map(result -> true);
              } catch (Exception e) {
                log.error("Error serializing assignments for Aerospike", e);
                return Single.just(false);
              }
            });
  }

  private Single<Long> getTotalVariantCount(Experiment experiment) {

    // TODO
    List<Single<Long>> countSingles = new ArrayList<>();
    for (String variantName : experiment.getVariantWeights().getVariants().keySet()) {
      String asKey = experiment.getExperimentId().toString() + ":" + variantName;
      Key key = new Key(AEROSPIKE_NAMESPACE, VARIANT_COUNTS_SET, asKey);

      Single<Long> countSingle =
          aerospikeClient
              .get(null, key, "count")
              .map(record -> record.getLong("count"))
              .onErrorReturnItem(0L);

      countSingles.add(countSingle);
    }

    return Observable.fromIterable(countSingles).flatMapSingle(s -> s).reduce(0L, Long::sum);
  }

  private Experiment mapRowToExperiment(Row row) {
    try {
      UUID experimentId = UUID.fromString(row.getString("experiment_id"));
      UUID projectId = UUID.fromString(row.getString("project_id"));

      String variantWeightsJson = row.getString("variant_weights");
      VariantWeights variantWeights =
          variantWeightsJson != null
              ? objectMapper.readValue(variantWeightsJson, VariantWeights.class)
              : null;

      String ruleAttributesJson = row.getString("rule_attributes");
      RuleAttributes ruleAttributes =
          ruleAttributesJson != null
              ? objectMapper.readValue(ruleAttributesJson, RuleAttributes.class)
              : null;

      String entitiesJson = row.getString("entities");
      List<String> entities =
          entitiesJson != null
              ? objectMapper.readValue(entitiesJson, new TypeReference<List<String>>() {})
              : new ArrayList<>();

      String overridesJson = row.getString("overrides");
      List<String> overrides =
          overridesJson != null
              ? objectMapper.readValue(overridesJson, new TypeReference<List<String>>() {})
              : new ArrayList<>();

      String distributionStrategyStr = row.getString("distribution_strategy");
      DistributionStrategy distributionStrategy =
          distributionStrategyStr != null
              ? DistributionStrategy.valueOf(distributionStrategyStr)
              : DistributionStrategy.RANDOM;

      return Experiment.builder()
          .experimentId(experimentId)
          .projectId(projectId)
          .name(row.getString("name"))
          .description(row.getString("description"))
          .status(row.getString("status"))
          .cohorts(row.getString("cohorts"))
          .variantWeights(variantWeights)
          .ruleAttributes(ruleAttributes)
          .startTime(row.getLong("start_time"))
          .overrides(overrides)
          .endTime(row.getLong("end_time"))
          .entities(entities)
          .exposure(row.getInteger("exposure"))
          .threshold(row.getLong("threshold"))
          .distributionStrategy(distributionStrategy)
          .build();
    } catch (Exception e) {
      log.error("Error mapping row to Experiment", e);
      return null;
    }
  }
}
