package com.ascend.testlab.dao.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
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
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.dao.AllocationDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.util.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the AllocationDAO interface. Manages experiment allocations, user allocations,
 * variant counts, and locking using PostgreSQL and Aerospike.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 * @see AllocationDAO
 */
@Slf4j
public class AllocationDAOImpl implements AllocationDAO {

  private final PgReaderClient pgReaderClient;
  private final AerospikeClient aerospikeClient;
  private final AerospikeConfig aerospikeConfig;
  private final ObjectMapper objectMapper;

  /**
   * Constructor for AllocationDAOImpl with dependency injection.
   *
   * @param pgReaderClient PostgreSQL client for database operations
   * @param aerospikeClient Aerospike client for cache operations
   * @param aerospikeConfig Aerospike configuration
   * @param objectMapper Jackson ObjectMapper for JSON serialization
   */
  @Inject
  public AllocationDAOImpl(
      PgReaderClient pgReaderClient,
      AerospikeClient aerospikeClient,
      AerospikeConfig aerospikeConfig,
      ObjectMapper objectMapper) {
    this.pgReaderClient = pgReaderClient;
    this.aerospikeClient = aerospikeClient;
    this.aerospikeConfig = aerospikeConfig;
    this.objectMapper = objectMapper;
  }

  /** {@inheritDoc} */
  @Override
  public Single<List<Experiment>> fetchActiveExperiments(
      String projectKey, List<String> experimentKeys) {
    String[] experimentKeysArray = experimentKeys.toArray(new String[0]);
    return pgReaderClient
        .fetchAll(
            ReadQuery.GET_EXPERIMENTS_FROM_KEY,
            Tuple.tuple().addString(projectKey).addArrayOfString(experimentKeysArray),
            row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
        .doOnSuccess(
            experiments ->
                log.debug(
                    "Fetched {} active experiments for tenant {}", experiments.size(), projectKey))
        .onErrorReturn(
            error -> {
              log.error("Error fetching active experiments for tenant {}", projectKey, error);
              return new ArrayList<>();
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<List<UserExperimentMap>> getAllocations(String userId, String projectKey) {

    return getUserAssignmentsFromAerospike(userId, projectKey)
        .doOnError(
            error -> {
              log.warn(
                  "Error fetching from Aerospike, falling back to MySQL: {}", error.getMessage());
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Map<String, List<UserExperimentMap>>> getAllocations(
      List<String> userIds, String projectKey) {

    if (Objects.isNull(userIds) || userIds.isEmpty()) {
      log.debug("No user IDs provided for batch fetch");
      return Single.just(Collections.emptyMap());
    }

    log.debug("Fetching allocations for {} users in project {}", userIds.size(), projectKey);

    return getAllocationsBatchFromAerospike(userIds, projectKey)
        .doOnSuccess(
            result ->
                log.debug(
                    "Fetched allocations for {} users (requested: {})",
                    result.size(),
                    userIds.size()))
        .doOnError(
            error -> {
              log.error(
                  "Error fetching batch allocations from Aerospike for {} users",
                  userIds.size(),
                  error);
            });
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> checkThreshold(String projectKey, Experiment experiment) {

    return getTotalVariantCount(projectKey, experiment)
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
  public Single<Boolean> acquireUserLock(String userId, String projectKey) {
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserLockSet(), projectKey),
            userId);

    WritePolicy policy = new WritePolicy();
    policy.expiration = -1;
    policy.sendKey = true;

    Bin lockBin = new Bin(aerospikeConfig.getUserAllocationLockBin(), System.currentTimeMillis());

    return aerospikeClient
        .put(policy, key, lockBin)
        .map(
            result -> {
              log.debug("Acquired lock for user {} in tenant {}", userId, projectKey);
              return true;
            })
        .onErrorReturnItem(false);
  }

  @Override
  public Single<Boolean> releaseUserLock(String userId, String projectKey) {
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserLockSet(), projectKey),
            userId);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;

    return aerospikeClient
        .delete(policy, key)
        .doOnSuccess(
            result -> log.debug("Released lock for user {} in tenant {}", userId, projectKey))
        .onErrorReturnItem(true);
  }

  @Override
  public Single<List<Experiment>> fetchConcludedExperiments(String projectKey) {
    return pgReaderClient
        .fetchAll(
            ReadQuery.GET_CONCLUDED_EXPERIMENTS,
            Tuple.tuple().addString(projectKey),
            row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
        .doOnSuccess(
            experiments ->
                log.debug(
                    "Fetched {} concluded experiments for tenant {}",
                    experiments.size(),
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error("Error fetching concluded experiments for tenant {}", projectKey, error);
              return new ArrayList<>();
            });
  }

  /**
   * Fetches user allocations from Aerospike for a single user
   *
   * @param userId user identifier
   * @param projectKey project identifier
   * @return list of user experiment mappings
   */
  @SuppressWarnings("unchecked")
  private Single<List<UserExperimentMap>> getUserAssignmentsFromAerospike(
      String userId, String projectKey) {

    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getUserAllocationsSet(), projectKey),
            userId);

    Policy policy = new Policy();

    return aerospikeClient
        .get(policy, key, aerospikeConfig.getAllocationMapBin())
        .map(record -> parseUserAssignmentsFromRecord(record, userId))
        .doOnError(
            error -> {
              log.error(
                  "Error while getting allocations from Aerospike for user {}", userId, error);
            });
  }

  /**
   * Fetches user assignments from Aerospike for multiple users using batch operations
   *
   * @param userIds list of user identifiers
   * @param projectKey project identifier
   * @return map of userId to list of user experiment mappings
   */
  @SuppressWarnings("unchecked")
  private Single<Map<String, List<UserExperimentMap>>> getAllocationsBatchFromAerospike(
      List<String> userIds, String projectKey) {

    String set = CommonUtil.getSetName(aerospikeConfig.getUserAllocationsSet(), projectKey);

    Key[] keys = new Key[userIds.size()];
    for (int i = 0; i < userIds.size(); i++) {
      keys[i] = new Key(aerospikeConfig.getNamespace(), set, userIds.get(i));
    }

    BatchPolicy batchPolicy = new BatchPolicy();
    batchPolicy.sendKey = true;

    return aerospikeClient
        .get(batchPolicy, keys, aerospikeConfig.getAllocationMapBin())
        .map(
            records -> {
              Map<String, List<UserExperimentMap>> resultMap = new HashMap<>();

              for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);
                String userId = userIds.get(i);

                List<UserExperimentMap> assignments =
                    parseUserAssignmentsFromRecord(record, userId);
                resultMap.put(userId, assignments);
              }

              log.debug("Batch fetched assignments for {} users from Aerospike", resultMap.size());
              return resultMap;
            })
        .doOnError(
            error -> {
              log.error(
                  "Error while getting batch assignments from Aerospike for {} users",
                  userIds.size(),
                  error);
            });
  }

  /**
   * Parses user assignments from an Aerospike record. Extracted for reusability between single and
   * batch operations.
   *
   * @param record Aerospike record containing assignment data
   * @param userId user identifier for logging
   * @return list of user experiment mappings
   */
  @SuppressWarnings("unchecked")
  private List<UserExperimentMap> parseUserAssignmentsFromRecord(
      com.aerospike.client.Record record, String userId) {

    if (record == null) {
      log.debug("No userExperimentMaps record found in Aerospike for user {}", userId);
      return new ArrayList<>();
    }

    Map<String, Object> assignmentMap =
        (Map<String, Object>) record.getMap(aerospikeConfig.getAllocationMapBin());

    if (Objects.isNull(assignmentMap) || assignmentMap.isEmpty()) {
      log.debug("Empty assignment map for user {}", userId);
      return new ArrayList<>();
    }

    List<UserExperimentMap> userExperimentMaps = new ArrayList<>();

    try {
      for (Map.Entry<String, Object> entry : assignmentMap.entrySet()) {
        String experimentIdStr = entry.getKey();
        UserExperimentMap assignmentData =
            objectMapper.readValue(entry.getValue().toString(), UserExperimentMap.class);

        assignmentData.setExperimentId(UUID.fromString(experimentIdStr));
        userExperimentMaps.add(assignmentData);
      }

      log.debug(
          "Parsed {} userExperimentMaps from Aerospike for user {}",
          userExperimentMaps.size(),
          userId);
    } catch (Exception e) {
      log.error("Error parsing assignments for user {}", userId, e);
      return new ArrayList<>();
    }

    return userExperimentMaps;
  }

  private Single<Boolean> insertUserAssignmentsToAerospike(
      String userId, String projectKey, List<UserExperimentMap> assignments) {

    MapPolicy mapPolicy = new MapPolicy(MapOrder.UNORDERED, MapWriteMode.UPDATE);
    String set = CommonUtil.getSetName(aerospikeConfig.getUserAllocationsSet(), projectKey);

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
              aerospikeConfig.getAllocationMapBin(),
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

  /**
   * Decrements variant count for a single variant. This is a utility method used during rollback.
   * Note: This method is kept for potential future use but the batch rollback method is preferred.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param variantName variant name
   * @return the new count after decrement
   */
  private Single<Long> decrementVariantCount(
      String projectKey, UUID experimentId, String variantName) {

    log.debug("Decrementing variant count for experiment {} variant {}", experimentId, variantName);

    String asKey = experimentId.toString() + Constants.COLON + variantName;
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey),
            asKey);

    Operation decrementOp = Operation.add(new Bin(aerospikeConfig.getVariantCountBin(), -1));
    Operation getOp = Operation.get(aerospikeConfig.getVariantCountBin());
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
              long newCount = record.getLong(aerospikeConfig.getVariantCountBin());
              log.debug(
                  "Decremented variant count to {} for experiment {} variant {}",
                  newCount,
                  experimentId,
                  variantName);
              return newCount;
            })
        .onErrorReturnItem(0L);
  }

  private Single<Long> getTotalVariantCount(String projectKey, Experiment experiment) {

    if (Objects.isNull(experiment.getVariants()) || experiment.getVariants().isEmpty()) {
      log.debug("No variants found for experiment {}", experiment.getExperimentId());
      return Single.just(0L);
    }
    BatchPolicy batchPolicy = new BatchPolicy();
    batchPolicy.sendKey = true;
    List<Key> keys = new ArrayList<>();
    for (String variantName : experiment.getVariants().keySet()) {
      String asKey = experiment.getExperimentId().toString() + Constants.COLON + variantName;
      Key key =
          new Key(
              aerospikeConfig.getNamespace(),
              CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey),
              asKey);
      keys.add(key);
    }
    Key[] keyArray = keys.toArray(new Key[0]);

    return aerospikeClient
        .get(batchPolicy, keyArray, aerospikeConfig.getVariantCountBin())
        .map(
            record ->
                record.stream()
                    .map(rec -> rec.getLong(aerospikeConfig.getVariantCountBin()))
                    .reduce(0L, Long::sum))
        .onErrorReturnItem(0L);
  }

  @Override
  public Single<Boolean> insertAllocationsAndIncrementCounts(
      String userId,
      String projectKey,
      List<UserExperimentMap> assignments,
      Map<String, String> variantCountMap) {

    if (Objects.isNull(assignments) || assignments.isEmpty()) {
      log.debug("No assignments to insert for user {}", userId);
      return Single.just(true);
    }

    log.debug(
        "Transactionally inserting {} assignments and incrementing {} variant counts for user {}",
        assignments.size(),
        variantCountMap.size(),
        userId);

    return incrementVariantCountsBatch(projectKey, variantCountMap)
        .flatMap(
            incrementedCounts -> {
              if (incrementedCounts.isEmpty()) {
                log.warn("No variant counts were successfully incremented for user {}", userId);
                return Single.just(false);
              }

              log.debug(
                  "Successfully incremented {} variant counts, proceeding to insert assignments",
                  incrementedCounts.size());

              return insertUserAssignmentsToAerospike(userId, projectKey, assignments)
                  .onErrorResumeNext(
                      error -> {
                        log.error(
                            "Failed to insert user assignments for user {}, rolling back {} variant counts",
                            userId,
                            incrementedCounts.size(),
                            error);

                        Map<String, String> successfulIncrements = new HashMap<>();
                        for (String key : incrementedCounts.keySet()) {
                          successfulIncrements.put(key, variantCountMap.get(key));
                        }

                        return rollbackVariantCounts(projectKey, successfulIncrements)
                            .flatMap(rollbackSuccess -> Single.error(error));
                      });
            })
        .doOnSuccess(
            success ->
                log.info(
                    "Successfully completed transactional assignment for user {}: {} assignments, {} variant increments",
                    userId,
                    assignments.size(),
                    variantCountMap.size()))
        .onErrorResumeNext(
            error -> {
              log.error(
                  "Failed to increment variant counts for user {}, no rollback needed",
                  userId,
                  error);
              return Single.error(error);
            });
  }

  /**
   * Increments variant counts for multiple experiments in batch using map operations. Tracks which
   * increments succeed for proper rollback on failure.
   *
   * @param projectKey project identifier
   * @param variantCountMap map of "experimentId:variantName" -> count to increment (always 1 for
   *     now)
   * @return map of keys to their new counts
   */
  private Single<Map<String, Long>> incrementVariantCountsBatch(
      String projectKey, Map<String, String> variantCountMap) {

    if (Objects.isNull(variantCountMap) || variantCountMap.isEmpty()) {
      return Single.just(Collections.emptyMap());
    }

    WritePolicy policy = new WritePolicy();
    policy.expiration = -1;
    policy.sendKey = true;

    Map<String, Long> results = new HashMap<>();

    return Observable.fromIterable(variantCountMap.entrySet())
        .concatMapSingle(
            entry -> {
              String keyStr = entry.getKey();
              Key key =
                  new Key(
                      aerospikeConfig.getNamespace(),
                      CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey),
                      keyStr);

              Operation incrementOp =
                  Operation.add(new Bin(aerospikeConfig.getVariantCountBin(), 1));
              Operation getOp = Operation.get(aerospikeConfig.getVariantCountBin());

              return aerospikeClient
                  .operate(policy, key, incrementOp, getOp)
                  .map(
                      record -> {
                        Long count =
                            record != null
                                ? record.getLong(aerospikeConfig.getVariantCountBin())
                                : 1L;
                        results.put(keyStr, count);
                        log.debug("Incremented variant count for {} to {}", keyStr, count);
                        return keyStr;
                      })
                  .onErrorResumeNext(
                      error -> {
                        log.error("Failed to increment variant count for {}", keyStr, error);

                        return Single.error(
                            new RuntimeException(
                                "Failed to increment variant count for "
                                    + keyStr
                                    + " after successfully incrementing "
                                    + results.size()
                                    + " variants",
                                error));
                      });
            })
        .ignoreElements()
        .andThen(Single.just(results))
        .doOnError(
            error ->
                log.error(
                    "Error incrementing variant counts batch for project {}: {} of {} succeeded",
                    projectKey,
                    results.size(),
                    variantCountMap.size(),
                    error));
  }

  /**
   * Rollback variant counts by decrementing them. This method is called when user assignment
   * insertion fails after variant counts have been incremented. It attempts to rollback all
   * provided variants and continues even if individual rollbacks fail to ensure best-effort
   * cleanup.
   *
   * @param projectKey project identifier
   * @param variantCountMap map of "experimentId:variantName" -> variant name to decrement
   * @return success status (true if all rollbacks attempted, regardless of individual failures)
   */
  private Single<Boolean> rollbackVariantCounts(
      String projectKey, Map<String, String> variantCountMap) {

    if (Objects.isNull(variantCountMap) || variantCountMap.isEmpty()) {
      log.debug("No variant counts to rollback");
      return Single.just(true);
    }

    log.warn(
        "Rolling back {} variant count increments for project {}",
        variantCountMap.size(),
        projectKey);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;

    int[] successCount = {0};
    int[] failureCount = {0};

    return Observable.fromIterable(variantCountMap.entrySet())
        .concatMapSingle(
            entry -> {
              String keyStr = entry.getKey();
              Key key =
                  new Key(
                      aerospikeConfig.getNamespace(),
                      CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey),
                      keyStr);

              Operation decrementOp =
                  Operation.add(new Bin(aerospikeConfig.getVariantCountBin(), -1));
              Operation getOp = Operation.get(aerospikeConfig.getVariantCountBin());

              return aerospikeClient
                  .operate(policy, key, decrementOp, getOp)
                  .map(
                      record -> {
                        Long newCount =
                            record != null
                                ? record.getLong(aerospikeConfig.getVariantCountBin())
                                : 0L;
                        successCount[0]++;

                        if (newCount < 0) {
                          log.warn(
                              "Variant count for {} went negative during rollback: {}. "
                                  + "This may indicate the count was not properly incremented.",
                              keyStr,
                              newCount);
                        } else {
                          log.debug("Rolled back variant count for {} to {}", keyStr, newCount);
                        }
                        return true;
                      })
                  .onErrorResumeNext(
                      error -> {
                        failureCount[0]++;
                        log.error(
                            "Failed to rollback variant count for {}. Continuing with remaining rollbacks.",
                            keyStr,
                            error);

                        return Single.just(false);
                      });
            })
        .ignoreElements()
        .andThen(Single.just(true))
        .doOnSuccess(
            success ->
                log.info(
                    "Variant count rollback completed for project {}: {} succeeded, {} failed",
                    projectKey,
                    successCount[0],
                    failureCount[0]))
        .doOnError(
            error ->
                log.error(
                    "Unexpected error during variant count rollback for project {}: {} succeeded, {} failed",
                    projectKey,
                    successCount[0],
                    failureCount[0],
                    error));
  }
}
