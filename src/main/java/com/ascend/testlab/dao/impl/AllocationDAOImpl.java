package com.ascend.testlab.dao.impl;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.*;
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
import com.ascend.testlab.dto.entity.allocation.ReallocationLogEntry;
import com.ascend.testlab.dto.entity.allocation.UserExperimentMap;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.request.ReallocateRequest;
import com.ascend.testlab.exception.ErrorEnum;
import com.ascend.testlab.util.CommonUtil;
import com.dream11.rest.util.ExceptionUtil;
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

  @Override
  public Single<Experiment> fetchActiveExperiment(String projectKey, String experimentId) {
    return pgReaderClient
        .fetchOne(
            ReadQuery.GET_LIVE_EXPERIMENT,
            Tuple.of(projectKey, experimentId),
            row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
        .switchIfEmpty(
            Single.error(ExceptionUtil.getException(ErrorEnum.ACTIVE_EXPERIMENT_NOT_FOUND)))
        .doOnError(
            error ->
                log.error(
                    "Error fetching experiment {} for project {}",
                    experimentId,
                    projectKey,
                    error));
  }

  /**
   * Transactionally reallocates a user to a new variant. Handles: 1. Decrementing the old variant
   * count (if exists) 2. Incrementing the new variant count 3. Updating the user's assignment in
   * Aerospike 4. Logging the reallocation with PK (projectKey, userId, experimentId)
   *
   * <p>If any step fails after variant counts are modified, appropriate rollbacks are performed
   * using the same parallel pattern.
   *
   * @param projectKey project identifier
   * @param oldVariant the old variant name (null if user had no previous assignment)
   * @param newVariantAssignment the new user experiment map with updated variant
   * @param reallocateRequest request object containing userId, experimentId, and reason for
   *     reallocation
   * @return the updated user experiment map on success
   */
  @Override
  public Single<UserExperimentMap> reallocateUserVariant(
      String projectKey,
      String oldVariant,
      UserExperimentMap newVariantAssignment,
      ReallocateRequest reallocateRequest) {

    String newVariantName = newVariantAssignment.getVariantName();
    String experimentId = reallocateRequest.getExperimentId();

    return decrementAndIncrementVariantCounts(projectKey, experimentId, oldVariant, newVariantName)
        .flatMap(
            countUpdated ->
                updateUserAssignmentInAerospike(
                        reallocateRequest.getUserId(),
                        projectKey,
                        experimentId,
                        newVariantAssignment)
                    .flatMap(
                        assignmentUpdated -> {
                          if (!assignmentUpdated) {
                            log.error(
                                "Failed to update user assignment, rolling back variant counts in parallel");
                            // rollback variant counts
                            return decrementAndIncrementVariantCounts(
                                    projectKey, experimentId, newVariantName, oldVariant)
                                .flatMap(
                                    rollbackSuccess ->
                                        Single.error(
                                            ExceptionUtil.getException(
                                                ErrorEnum.REST_REALLOCATION_FAILED)));
                          }

                          return logReallocation(
                                  reallocateRequest.getUserId(),
                                  projectKey,
                                  experimentId,
                                  oldVariant,
                                  newVariantName,
                                  reallocateRequest.getReason())
                              .flatMap(logResult -> Single.just(newVariantAssignment))
                              .onErrorResumeNext(
                                  logError -> {
                                    log.warn(
                                        "Failed to log reallocation for user {} experiment {}, but reallocation succeeded",
                                        reallocateRequest.getUserId(),
                                        experimentId,
                                        logError);
                                    return Single.just(newVariantAssignment);
                                  });
                        }))
        .doOnError(
            error ->
                log.error(
                    "Error during reallocation for user {} experiment {}",
                    reallocateRequest.getUserId(),
                    experimentId,
                    error));
  }

  /**
   * Decrements the old variant count and increments the new variant count using parallel execution
   * via Single.zip for better performance. If either operation fails, appropriate rollback is
   * attempted.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param oldVariantName old variant name (may be null for new users)
   * @param newVariantName new variant name
   * @return a Single emitting true if variant counts updated successfully
   */
  private Single<Boolean> decrementAndIncrementVariantCounts(
      String projectKey, String experimentId, String oldVariantName, String newVariantName) {

    return Single.zip(
        updateVariantCount(projectKey, experimentId, oldVariantName, true),
        updateVariantCount(projectKey, experimentId, newVariantName, false),
        (oldCount, newCount) -> {
          log.debug(
              "Variant counts updated in parallel: old_count={}, new_count={}, oldKey={}, newKey={}",
              oldCount,
              newCount,
              experimentId + Constants.COLON + oldVariantName,
              experimentId + Constants.COLON + newVariantName);
          return true;
        });
  }

  /**
   * Updates user's experiment assignment in Aerospike with the new variant.
   *
   * @param userId user identifier
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param newAssignment the new assignment with updated variant
   * @return true if update successful
   */
  private Single<Boolean> updateUserAssignmentInAerospike(
      String userId, String projectKey, String experimentId, UserExperimentMap newAssignment) {

    String set = CommonUtil.getSetName(aerospikeConfig.getUserAllocationsSet(), projectKey);
    Key key = new Key(aerospikeConfig.getNamespace(), set, userId);

    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;
    policy.expiration = -1;

    try {
      String valueMap = objectMapper.writeValueAsString(newAssignment);

      MapPolicy mapPolicy = new MapPolicy(MapOrder.UNORDERED, MapWriteMode.UPDATE);

      return aerospikeClient
          .operate(
              policy,
              key,
              MapOperation.put(
                  mapPolicy,
                  aerospikeConfig.getAllocationMapBin(),
                  Value.get(experimentId),
                  Value.get(valueMap)))
          .map(
              result -> {
                log.debug(
                    "Successfully updated user assignment for user {} experiment {}",
                    userId,
                    experimentId);
                return true;
              })
          .onErrorResumeNext(
              error -> {
                log.error(
                    "Failed to update user assignment for user {} experiment {}",
                    userId,
                    experimentId,
                    error);
                return Single.just(false);
              });
    } catch (Exception e) {
      log.error(
          "Failed to serialize assignment for update - user {} experiment {}",
          userId,
          experimentId,
          e);
      return Single.just(false);
    }
  }

  private Single<Boolean> logReallocation(
      String userId,
      String projectKey,
      String experimentId,
      String oldVariantName,
      String newVariantName,
      String reason) {

    try {
      String set = CommonUtil.getSetName(aerospikeConfig.getReallocationLogSet(), projectKey);
      String compositeKey = userId + Constants.UNDER_SCORE + experimentId;
      Key key = new Key(aerospikeConfig.getNamespace(), set, compositeKey);

      WritePolicy policy = new WritePolicy();
      policy.sendKey = true;
      policy.expiration = -1;

      ReallocationLogEntry entry =
          ReallocationLogEntry.builder()
              .timestamp(System.currentTimeMillis())
              .oldVariant(oldVariantName)
              .newVariant(newVariantName)
              .reason(reason)
              .changedBy(Constants.ADMIN)
              .build();

      String entryJson = objectMapper.writeValueAsString(entry);

      if (entryJson == null || entryJson.isEmpty()) {
        log.error(
            "Serialized reallocation log is empty for user: {} experiment: {}",
            userId,
            experimentId);
        return Single.just(false);
      }

      // Append entry to the entries list
      return aerospikeClient
          .operate(
              policy,
              key,
              ListOperation.append(
                  new ListPolicy(), aerospikeConfig.getReallocationLogBin(), Value.get(entryJson)))
          .map(
              result -> {
                log.info(
                    "Successfully logged reallocation for user: {} experiment: {} (key: {})",
                    userId,
                    experimentId,
                    compositeKey);
                return true;
              })
          .doOnError(
              error ->
                  log.error(
                      "Failed to log reallocation to Aerospike for user: {} experiment: {}, "
                          + "error: {}",
                      userId,
                      experimentId,
                      error.getClass().getSimpleName(),
                      error));
    } catch (Exception e) {
      log.error("Error serializing reallocation log for user: {}", userId, e);
      return Single.just(false);
    }
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
  private Single<Map<String, List<UserExperimentMap>>> getAllocationsBatchFromAerospike(
      List<String> userIds, String projectKey) {

    String set = CommonUtil.getSetName(aerospikeConfig.getUserAllocationsSet(), projectKey);

    List<Key> keys =
        userIds.stream()
            .map(userId -> new Key(aerospikeConfig.getNamespace(), set, userId))
            .toList();

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
   * Atomically adjusts the stored variant count in Aerospike by \+1 or \-1 and returns the new
   * count. Performs an add operation followed by a read of the count bin so the returned value
   * reflects the latest count.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param variantName variant name
   * @param decrement if `true` the count is decremented by 1; if `false` the count is incremented
   *     by 1
   * @return the new count after the update, or 0L on error
   */
  private Single<Long> updateVariantCount(
      String projectKey, String experimentId, String variantName, Boolean decrement) {

    log.debug("Decrementing variant count for experiment {} variant {}", experimentId, variantName);

    String asKey = experimentId + Constants.COLON + variantName;
    Key key =
        new Key(
            aerospikeConfig.getNamespace(),
            CommonUtil.getSetName(aerospikeConfig.getVariantCountSet(), projectKey),
            asKey);

    int val = decrement ? -1 : 1;
    Operation decrementOp = Operation.add(new Bin(aerospikeConfig.getVariantCountBin(), val));
    Operation getOp = Operation.get(aerospikeConfig.getVariantCountBin());
    WritePolicy policy = new WritePolicy();
    policy.sendKey = true;

    return aerospikeClient
        .operate(policy, key, decrementOp, getOp)
        .map(
            record -> {
              if (record == null) {
                log.warn(
                    "Null record returned after update operation for {}:{}",
                    experimentId,
                    variantName);
                return 0L;
              }
              long newCount = record.getLong(aerospikeConfig.getVariantCountBin());
              log.debug(
                  "updated variant count to {} for experiment {} variant {}",
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

    return aerospikeClient
        .get(batchPolicy, keys, aerospikeConfig.getVariantCountBin())
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
   * Increments variant counts for multiple experiments in batch. Each variant count is stored as a
   * separate record in Aerospike. Tracks which increments succeed for proper rollback on failure.
   *
   * @param projectKey project identifier
   * @param variantCountMap map of "experimentId:variantName" -> variant name
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
                        Long newCount =
                            record != null
                                ? record.getLong(aerospikeConfig.getVariantCountBin())
                                : 1L;
                        log.debug("Incremented variant count for {} to {}", keyStr, newCount);
                        return new AbstractMap.SimpleEntry<>(keyStr, newCount);
                      })
                  .onErrorResumeNext(
                      error -> {
                        log.error(
                            "Failed to increment variant count for {}. Continuing with remaining increments.",
                            keyStr,
                            error);
                        return Single.error(error);
                      });
            })
        .toMap(Map.Entry::getKey, Map.Entry::getValue)
        .doOnSuccess(
            result ->
                log.debug(
                    "Successfully incremented {} variant counts for project {}",
                    result.size(),
                    projectKey))
        .doOnError(
            error ->
                log.error(
                    "Error incrementing variant counts batch for project {}: of {}",
                    projectKey,
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
