package com.ascend.testlab.dao.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.MapOperation;
import com.aerospike.client.cdt.MapOrder;
import com.aerospike.client.cdt.MapPolicy;
import com.aerospike.client.cdt.MapWriteMode;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.ascend.testlab.client.aerospike.AerospikeClient;
import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.config.AerospikeConfig;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.postgresql.PostgresColumn;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AssignmentDAO;
import com.ascend.testlab.dto.response.UserExperimentMap;
import com.ascend.testlab.entity.AssignmentDomain;
import com.ascend.testlab.entity.Experiment;
import com.ascend.testlab.entity.RuleAttributes;
import com.ascend.testlab.entity.Variant;
import com.ascend.testlab.entity.variantWeights.CohortVariantWeights;
import com.ascend.testlab.entity.variantWeights.ManualVariantWeights;
import com.ascend.testlab.entity.variantWeights.VariantWeights;
import com.ascend.testlab.util.CommonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AssignmentDAO interface. Manages experiment assignments, user assignments,
 * variant counts, and locking using PostgreSQL and Aerospike.
 *
 * @author anudeepreddy20
 * @version 1.0
 * @since 1.0
 * @see AssignmentDAO
 */
@Slf4j
public class AssignmentDAOImpl implements AssignmentDAO {

  private final PgReaderClient pgReaderClient;
  private final AerospikeClient aerospikeClient;
  private final AerospikeConfig aerospikeConfig;
  private final ObjectMapper objectMapper;

  @Inject
  public AssignmentDAOImpl(
      PgReaderClient pgReaderClient,
      AerospikeClient aerospikeClient,
      AerospikeConfig aerospikeConfig,
      ObjectMapper objectMapper) {
    this.pgReaderClient = pgReaderClient;
    this.aerospikeClient = aerospikeClient;
    this.aerospikeConfig = aerospikeConfig;
    this.objectMapper = objectMapper;
  }

  @Override
  public Single<List<Experiment>> fetchActiveExperiments(UUID tenantId) {

    return pgReaderClient
        .fetchAll(
            ReadQuery.GET_EXPERIMENTS,
            Tuple.tuple().addString(tenantId.toString()),
            this::mapRowToExperiment)
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

    if (Objects.isNull(assignments) || assignments.isEmpty()) {
      log.debug("No assignments to insert for user {}", userId);
      return Single.just(true);
    }

    return insertUserAssignmentsToAerospike(userId, tenantId, assignments).onErrorReturnItem(false);
  }

  @Override
  public Single<Long> incrementVariantCount(UUID tenantId, UUID experimentId, String variantName) {
    String asKey = experimentId.toString() + Constants.COLON + variantName;
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), tenantId.toString()),
            asKey);

    WritePolicy policy = new WritePolicy();
    policy.expiration = -1;
    policy.sendKey = true;

    Operation incrementOp = Operation.add(new Bin(aerospikeConfig.getCountBin(), 1));
    Operation getOp = Operation.get(aerospikeConfig.getCountBin());

    return aerospikeClient
        .operate(policy, key, incrementOp, getOp)
        .map(
            record -> {
              if (record == null) {
                log.warn(
                    "Null record returned after increment for {}:{}", experimentId, variantName);
                return 1L;
              }
              Long count = record.getLong("count");
              log.debug(
                  "Incremented variant count for {}:{} to {}", experimentId, variantName, count);
              return count;
            })
        .doOnError(
            error -> {
              log.error(
                  "Error incremented variant count for {}:{}", experimentId, variantName, error);
            });
  }

  @Override
  public Single<Boolean> checkThreshold(UUID tenantId, Experiment experiment) {

    return getTotalVariantCount(tenantId, experiment)
        .map(
            currentCount -> {
              boolean underThreshold =
                  experiment.getThreshold() == 0 || currentCount < experiment.getThreshold();
              log.debug(
                  "Experiment {} - current: {}, threshold: {}, under: {}",
                  experiment.getExperimentId(),
                  currentCount,
                  experiment.getThreshold(),
                  underThreshold);
              return underThreshold;
            })
        .doOnError(
            err -> {
              log.error(
                  "Error fetching threshold for experiment {}", experiment.getExperimentId(), err);
            });
  }

  @Override
  public Single<Boolean> acquireUserLock(String userId, UUID tenantId) {
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserLockSet(), tenantId.toString()),
            userId);

    WritePolicy policy = new WritePolicy();
    policy.expiration = -1;
    policy.sendKey = true;

    Bin lockBin = new Bin(aerospikeConfig.getLockBin(), System.currentTimeMillis());

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
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserLockSet(), tenantId.toString()),
            userId);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;

    return aerospikeClient
        .delete(policy, key)
        .doOnSuccess(
            result -> log.debug("Released lock for user {} in tenant {}", userId, tenantId))
        .onErrorReturnItem(true);
  }

  @Override
  public Single<List<Experiment>> fetchConcludedExperiments(UUID tenantId) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.GET_CONCLUDED_EXPERIMENTS,
            Tuple.tuple().addString(tenantId.toString()),
            this::mapRowToExperiment)
        .doOnSuccess(
            experiments ->
                log.debug(
                    "Fetched {} concluded experiments for tenant {}", experiments.size(), tenantId))
        .onErrorReturn(
            error -> {
              log.error("Error fetching concluded experiments for tenant {}", tenantId, error);
              return new ArrayList<>();
            });
  }

  @SuppressWarnings("unchecked")
  private Single<List<UserExperimentMap>> getUserAssignmentsFromAerospike(
      String userId, UUID tenantId) {

    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserAssignmentsSet(), tenantId.toString()),
            userId);

    Policy policy = new Policy();

    return aerospikeClient
        .get(policy, key, aerospikeConfig.getAssignmentMapBin())
        .map(
            record -> {
              if (record == null) {
                log.debug("No userExperimentMaps record found in Aerospike for user {}", userId);
                return new ArrayList<UserExperimentMap>();
              }

              Map<String, Object> assignmentMap =
                  (Map<String, Object>) record.getMap(aerospikeConfig.getAssignmentMapBin());
              if (Objects.isNull(assignmentMap) || assignmentMap.isEmpty()) {
                return new ArrayList<UserExperimentMap>();
              }

              List<UserExperimentMap> userExperimentMaps = new ArrayList<>();

              for (Map.Entry<String, Object> entry : assignmentMap.entrySet()) {
                String experimentIdStr = entry.getKey();
                UserExperimentMap assignmentData =
                    objectMapper.readValue(entry.getValue().toString(), UserExperimentMap.class);

                assignmentData.setExperimentId(UUID.fromString(experimentIdStr));
                userExperimentMaps.add(assignmentData);
              }

              log.debug(
                  "Fetched {} userExperimentMaps from Aerospike for user {}",
                  userExperimentMaps.size(),
                  userId);
              return userExperimentMaps;
            })
        .doOnError(
            error -> {
              log.error(
                  "Error while getting assignments from Aerospike for user {}", userId, error);
            });
  }

  private Single<Boolean> insertUserAssignmentsToAerospike(
      String userId, UUID tenantId, List<UserExperimentMap> assignments) {

    MapPolicy mapPolicy = new MapPolicy(MapOrder.UNORDERED, MapWriteMode.UPDATE);
    String set =
        CommonUtil.getSetName(aerospikeConfig.getUserAssignmentsSet(), tenantId.toString());

    Key key = new Key(aerospikeConfig.getNamespace(), set, userId);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;
    policy.expiration = -1;

    List<Operation> operations = new ArrayList<>();
    for (UserExperimentMap assignment : assignments) {
      String expId = assignment.getExperimentId().toString();

      String valueMap = JsonObject.mapFrom(assignment).toString();

      operations.add(
          MapOperation.put(
              mapPolicy,
              aerospikeConfig.getAssignmentMapBin(),
              Value.get(expId),
              Value.get(valueMap)));
    }

    return aerospikeClient
        .operate(policy, key, operations.toArray(new Operation[0]))
        .map(res -> true)
        .doOnError(
            err -> {
              log.error("Error while inserting assignments in Aerospike for user {}", userId, err);
            });
  }

  //  @Override
  //  public Single<Boolean> updateUserAssignment(
  //      String userId, UUID tenantId, UserExperimentMap assignment) {
  //
  //    log.debug(
  //        "Updating assignment for user {} in experiment {}", userId,
  // assignment.getExperimentId());
  //
  //    return getUserAssignments(userId, tenantId)
  //        .flatMap(
  //            currentAssignments -> {
  //              List<UserExperimentMap> updatedAssignments =
  //                  currentAssignments.stream()
  //                      .map(
  //                          existing -> {
  //                            if (existing.getExperimentId().equals(assignment.getExperimentId()))
  // {
  //                              return assignment;
  //                            }
  //                            return existing;
  //                          })
  //                      .toList();
  //
  //              boolean found =
  //                  currentAssignments.stream()
  //                      .anyMatch(a -> a.getExperimentId().equals(assignment.getExperimentId()));
  //              if (!found) {
  //                updatedAssignments = new ArrayList<>(updatedAssignments);
  //                updatedAssignments.add(assignment);
  //              }
  //
  //              Key key =
  //                  new Key(
  //                      aerospikeConfig.getNamespace(),
  //                      CommonUtil.getSetName(
  //                          aerospikeConfig.getUserAssignmentsSet(), tenantId.toString()),
  //                      userId);
  //
  //              try {
  //                String assignmentsJson = objectMapper.writeValueAsString(updatedAssignments);
  //                Bin bin = new Bin("assignments", assignmentsJson);
  //
  //                WritePolicy policy = new WritePolicy();
  //                policy.expiration = -1;
  //                policy.sendKey = true;
  //
  //                return aerospikeClient
  //                    .put(policy, key, bin)
  //                    .map(
  //                        result -> {
  //                          log.info(
  //                              "Successfully updated assignment for user {} in experiment {}",
  //                              userId,
  //                              assignment.getExperimentId());
  //                          return true;
  //                        });
  //              } catch (Exception e) {
  //                log.error("Error updating assignment in Aerospike", e);
  //                return Single.just(false);
  //              }
  //            })
  //        .onErrorReturnItem(false);
  //  }

  @Override
  public Single<Long> decrementVariantCount(UUID tenantId, UUID experimentId, String variantName) {

    log.debug("Decrementing variant count for experiment {} variant {}", experimentId, variantName);

    String asKey = experimentId.toString() + Constants.COLON + variantName;
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), tenantId.toString()),
            asKey);

    Operation decrementOp = Operation.add(new Bin(aerospikeConfig.getCountBin(), -1));
    Operation getOp = Operation.get(aerospikeConfig.getCountBin());
    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;

    return aerospikeClient
        .operate(policy, key, decrementOp, getOp)
        .map(
            record -> {
              if (record == null) {
                log.warn(
                    "Null record returned after decrement for {}:{}", experimentId, variantName);
                return 0L;
              }
              long newCount = record.getLong(aerospikeConfig.getCountBin());
              log.debug(
                  "Decremented variant count to {} for experiment {} variant {}",
                  newCount,
                  experimentId,
                  variantName);
              return newCount;
            })
        .onErrorReturnItem(0L);
  }

  private Single<Long> getTotalVariantCount(UUID tenantId, Experiment experiment) {

    if (Objects.isNull(experiment.getVariant()) || experiment.getVariant().isEmpty()) {
      log.debug("No variants found for experiment {}", experiment.getExperimentId());
      return Single.just(0L);
    }
    BatchPolicy batchPolicy = new BatchPolicy();
    batchPolicy.sendKey = true;
    List<Key> keys = new ArrayList<>();
    for (String variantName : experiment.getVariant().keySet()) {
      String asKey = experiment.getExperimentId().toString() + Constants.COLON + variantName;
      Key key =
          new Key(
              aerospikeConfig.getNamespace(),
              CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), tenantId.toString()),
              asKey);
      keys.add(key);
    }
    Key[] keyArray = keys.toArray(new Key[0]);

    return aerospikeClient
        .get(batchPolicy, keyArray, aerospikeConfig.getCountBin())
        .map(
            record ->
                record.stream()
                    .map(rec -> rec.getLong(aerospikeConfig.getCountBin()))
                    .reduce(0L, Long::sum))
        .onErrorReturnItem(0L);
  }

  private Experiment mapRowToExperiment(Row row) {
    try {
      UUID experimentId = UUID.fromString(row.getString(PostgresColumn.EXPERIMENT_ID.getColumn()));
      String projectKey = row.getString(PostgresColumn.PROJECT_KEY.getColumn());

      String assignmentDomainStr = row.getString(PostgresColumn.ASSIGNMENT_DOMAIN.getColumn());
      AssignmentDomain assignmentDomain = AssignmentDomain.valueOf(assignmentDomainStr);

      JsonObject variantWeightsJson = row.getJsonObject(PostgresColumn.VARIANT_WEIGHTS.getColumn());
      VariantWeights variantWeights =
          deserializeVariantWeights(variantWeightsJson, assignmentDomain, experimentId);

      String ruleAttributesJson =
          row.getJson(PostgresColumn.RULE_ATTRIBUTES.getColumn()).toString();
      List<RuleAttributes> ruleAttributes =
          ruleAttributesJson != null
              ? objectMapper.readValue(ruleAttributesJson, new TypeReference<>() {})
              : null;

      String overridesJson = row.getString(PostgresColumn.OVERRIDES.getColumn());
      List<String> overrides = Arrays.asList(overridesJson.split(","));

      String distributionStrategyStr =
          row.getString(PostgresColumn.DISTRIBUTION_STRATEGY.getColumn());
      DistributionStrategy distributionStrategy =
          distributionStrategyStr != null
              ? DistributionStrategy.valueOf(distributionStrategyStr)
              : DistributionStrategy.RANDOM;

      JsonObject variantJson = row.getJsonObject(PostgresColumn.VARIANT.getColumn());
      TypeReference<Map<String, Variant>> typeRef = new TypeReference<>() {};
      Map<String, Variant> variants = objectMapper.readValue(variantJson.toString(), typeRef);

      return Experiment.builder()
          .experimentId(experimentId)
          .projectKey(projectKey)
          .name(row.getString(PostgresColumn.NAME.getColumn()))
          .description(row.getString(PostgresColumn.DESCRIPTION.getColumn()))
          .status(row.getString(PostgresColumn.STATUS.getColumn()))
          .cohorts(Arrays.asList(row.getArrayOfStrings(PostgresColumn.COHORTS.getColumn())))
          .variantWeights(variantWeights)
          .variant(variants)
          .ruleAttributes(ruleAttributes)
          .startTime(row.getLong(PostgresColumn.START_TIME.getColumn()))
          .overrides(overrides)
          .endTime(row.getLong(PostgresColumn.END_TIME.getColumn()))
          .exposure(row.getInteger(PostgresColumn.EXPOSURE.getColumn()))
          .threshold(row.getLong(PostgresColumn.THRESHOLD.getColumn()))
          .distributionStrategy(distributionStrategy)
          .assignmentDomain(assignmentDomain)
          .build();
    } catch (Exception e) {
      log.error("Error mapping row to Experiment", e);
      return null;
    }
  }

  /**
   * Deserializes variant weights JSON into the appropriate VariantWeights subclass based on
   * assignment domain.
   *
   * @param variantWeightsJson the JSON object containing variant weights
   * @param assignmentDomain the assignment domain type
   * @param experimentId experiment ID for logging
   * @return appropriate VariantWeights implementation
   */
  private VariantWeights deserializeVariantWeights(
      JsonObject variantWeightsJson, AssignmentDomain assignmentDomain, UUID experimentId) {

    if (Objects.isNull(variantWeightsJson)) {
      log.warn("Null variant weights JSON for experiment {}", experimentId);
      return null;
    }

    try {
      String jsonString = variantWeightsJson.toString();

      switch (assignmentDomain) {
        case MANUAL:
          ManualVariantWeights manualWeights =
              objectMapper.readValue(jsonString, ManualVariantWeights.class);
          log.debug(
              "Deserialized ManualVariantWeights for experiment {}: {}",
              experimentId,
              manualWeights);
          return manualWeights;

        case COHORT:
          CohortVariantWeights cohortWeights =
              objectMapper.readValue(jsonString, CohortVariantWeights.class);
          log.debug(
              "Deserialized CohortVariantWeights for experiment {}: {}",
              experimentId,
              cohortWeights);
          return cohortWeights;
      }
    } catch (Exception e) {
      log.error(
          "Error deserializing variant weights for experiment {} with domain {}",
          experimentId,
          assignmentDomain,
          e);
      return null;
    }
    return null;
  }
}
