package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentAnalysisDAO;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.ExperimentUpdateLogDAO;
import com.ascend.testlab.dao.OwnerDAO;
import com.ascend.testlab.dao.TagDAO;
import com.ascend.testlab.dto.request.CreateExperimentRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExperimentDAO for PostgreSQL database operations.
 *
 * <p>This class handles all experiment-related database operations including create and update
 * operations with proper type conversions for PostgreSQL-specific types (UUID, JSONB, arrays).
 *
 * @author Ravi Pandey
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ExperimentDAOImpl implements ExperimentDAO {

  private final PgWriterClient pgWriterClient;
  private final PgReaderClient pgReaderClient;
  private final TagDAO tagDAO;
  private final OwnerDAO ownerDAO;
  private final ExperimentUpdateLogDAO experimentUpdateLogDAO;
  private final ExperimentAnalysisDAO experimentAnalysisDAO;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Constructs ExperimentDAOImpl with PostgreSQL clients and related DAOs.
   *
   * @param pgWriterClient PostgreSQL writer client for database operations
   * @param pgReaderClient PostgreSQL reader client for database operations
   * @param tagDAO DAO for tag operations
   * @param ownerDAO DAO for owner operations
   * @param experimentUpdateLogDAO DAO for update log operations
   * @param experimentAnalysisDAO DAO for analysis operations
   */
  @Inject
  public ExperimentDAOImpl(
      PgWriterClient pgWriterClient,
      PgReaderClient pgReaderClient,
      TagDAO tagDAO,
      OwnerDAO ownerDAO,
      ExperimentUpdateLogDAO experimentUpdateLogDAO,
      ExperimentAnalysisDAO experimentAnalysisDAO) {
    this.pgWriterClient = pgWriterClient;
    this.pgReaderClient = pgReaderClient;
    this.tagDAO = tagDAO;
    this.ownerDAO = ownerDAO;
    this.experimentUpdateLogDAO = experimentUpdateLogDAO;
    this.experimentAnalysisDAO = experimentAnalysisDAO;
  }

  /**
   * Creates a new experiment in the database.
   *
   * <p>This method inserts a new experiment record with all provided fields including enums,
   * arrays, and JSONB data. It handles proper type conversion for PostgreSQL compatibility.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param request experiment creation request with all experiment details
   * @return Single emitting 1L on success, 0L on failure
   */
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
      String variantsJson = null;
      String ruleAttributesJson = null;
      String winningVariantJson = null;

      try {
        if (request.getVariantWeights() != null) {
          variantWeightsJson = MAPPER.writeValueAsString(request.getVariantWeights());
        }
        if (request.getVariants() != null) {
          variantsJson = MAPPER.writeValueAsString(request.getVariants());
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
                  .addValue(request.getProjectKey().toString()) // $1 project_key
                  .addValue(request.getExperimentId().toString()) // $2 experiment_id
                  .addValue(request.getName()) // $3 name
                  .addValue(request.getDescription()) // $4 description
                  .addValue(request.getHypothesis()) // $5 hypothesis
                  .addValue(
                      request.getStatus() == null ? null : request.getStatus().name()) // $6 status
                  .addValue(
                      request.getType() == null ? null : request.getType().getValue()) // $7 type
                  .addValue(
                      request.getGuardrailHealthStatus() == null
                          ? null
                          : request.getGuardrailHealthStatus().name()) // $8 guardrail_health_status
                  .addValue(cohortsArray) // $9 cohorts as varchar array
                  .addValue(variantWeightsJson) // $10 variant_weights as jsonb string
                  .addValue(variantsJson) // $11 variants as jsonb string
                  .addValue(
                      request.getDistributionStrategy() == null
                          ? null
                          : request.getDistributionStrategy().name()) // $12 distribution_strategy
                  .addValue(
                      request.getAssignmentDomain() == null
                          ? null
                          : request.getAssignmentDomain().name()) // $13 assignment_domain
                  .addValue(
                      request.getAssignmentStrategy() == null
                          ? null
                          : request.getAssignmentStrategy().name()) // $14 assignment_strategy
                  .addValue(request.getOverrides()) // $15 overrides as varchar
                  .addValue(ruleAttributesJson) // $16 rule_attributes as jsonb string
                  .addValue(winningVariantJson) // $17 winning_variant as jsonb string
                  .addValue(request.getExposure()) // $18 exposure
                  .addValue(request.getThreshold()) // $19 threshold
                  .addValue(request.getStartTime()) // $20 start_time
                  .addValue(request.getEndTime()) // $21 end_time
                  .addValue(request.getCreatedBy())) // $22 created_by
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

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * @param tenantId tenant identifier for multi-tenancy
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request experiment creation request with all experiment details
   * @param tags list of tags to associate with experiment
   * @param owner owner of the experiment
   * @return Maybe emitting experiment ID on success
   */
  @Override
  public Maybe<Long> createWithRelatedData(
      UUID tenantId,
      UUID projectKey,
      UUID experimentId,
      CreateExperimentRequest request,
      List<String> tags,
      String owner) {

    log.info(
        "DAO: Creating experiment with related data, projectKey: {}, experimentId: {}",
        projectKey,
        experimentId);

    return pgWriterClient.executeWithTransaction(
        connection ->
            create(tenantId, projectKey, request)
                .flatMapMaybe(
                    id -> {
                      if (id <= 0) {
                        log.error("DAO: Failed to create experiment - id is 0");
                        return Maybe.error(new RuntimeException("Failed to insert experiment"));
                      }

                      log.info("DAO: Experiment created with id: {}, inserting related data", id);

                      // Execute all inserts in parallel using zip
                      return Single.zip(
                              tagDAO.batchInsertTags(connection, projectKey, experimentId, tags),
                              ownerDAO.insertOwner(connection, projectKey, experimentId, owner),
                              experimentUpdateLogDAO.insertUpdateLog(
                                  connection,
                                  projectKey,
                                  experimentId,
                                  null, // previous_data is null for create
                                  convertRequestToMap(request),
                                  request.getCreatedBy()),
                              experimentAnalysisDAO.insertAnalysis(
                                  connection, projectKey, experimentId, request.getMetrics()),
                              (tagsSuccess, ownerSuccess, updateLogSuccess, analysisSuccess) -> {
                                // Validate all operations succeeded
                                if (!tagsSuccess) {
                                  throw new RuntimeException("Failed to insert tags");
                                }
                                if (!ownerSuccess) {
                                  throw new RuntimeException("Failed to insert owner");
                                }
                                if (!updateLogSuccess) {
                                  throw new RuntimeException("Failed to insert update log");
                                }
                                if (!analysisSuccess) {
                                  throw new RuntimeException("Failed to insert analysis");
                                }
                                log.info(
                                    "DAO: Successfully created experiment with all related data in parallel, id: {}, experimentId: {}, projectKey: {}",
                                    id,
                                    experimentId,
                                    projectKey);
                                return id;
                              })
                          .flatMapMaybe(Maybe::just);
                    }));
  }

  /**
   * Converts CreateExperimentRequest to Map for storing in update log.
   *
   * @param request experiment creation request
   * @return Map representation of the request
   */
  private Map<String, Object> convertRequestToMap(CreateExperimentRequest request) {
    try {
      String json = MAPPER.writeValueAsString(request);
      return MAPPER.readValue(json, Map.class);
    } catch (Exception e) {
      log.error("DAO: Failed to convert request to map: {}", e.getMessage(), e);
      return new HashMap<>();
    }
  }

  /**
   * Gets current experiment data as a map for update log.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting map of experiment data
   */
  @Override
  public Single<Map<String, Object>> getExperimentData(UUID projectKey, UUID experimentId) {
    log.debug(
        "DAO: Getting experiment data for projectKey: {}, experimentId: {}",
        projectKey,
        experimentId);

    Tuple params =
        Tuple.tuple().addString(projectKey.toString()).addString(experimentId.toString());

    return pgReaderClient
        .fetchOne(ReadQuery.GET_EXPERIMENT_DATA, params, this::rowToMap)
        .doOnSuccess(
            data ->
                log.info(
                    "DAO: Successfully retrieved experiment data for experimentId: {}, fields: {}",
                    experimentId,
                    data.keySet()))
        .doOnError(
            error ->
                log.error(
                    "DAO: Failed to get experiment data for experimentId: {}, error: {}",
                    experimentId,
                    error.getMessage(),
                    error))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning empty map for get experiment data, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return new HashMap<>();
            });
  }

  /**
   * Converts database row to map.
   *
   * @param row database row
   * @return map of column names to values
   */
  private Map<String, Object> rowToMap(Row row) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < row.size(); i++) {
      String columnName = row.getColumnName(i);
      Object value = row.getValue(i);

      // Convert JsonObject and JsonArray to Map/List for serialization
      if (value instanceof JsonObject) {
        map.put(columnName, ((JsonObject) value).getMap());
      } else if (value instanceof JsonArray) {
        map.put(columnName, ((JsonArray) value).getList());
      } else if (value instanceof java.time.OffsetDateTime) {
        // Convert OffsetDateTime to String for JSON serialization
        map.put(columnName, value.toString());
      } else if (value instanceof java.time.LocalDateTime) {
        // Convert LocalDateTime to String for JSON serialization
        map.put(columnName, value.toString());
      } else {
        map.put(columnName, value);
      }
    }
    return map;
  }

  /**
   * Updates experiment fields partially based on provided request map.
   *
   * <p>This method performs dynamic partial updates on experiment records. It validates fields,
   * converts data types appropriately, and builds a dynamic SQL query with only the provided
   * fields.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> updatePartial(
      UUID projectKey, UUID experimentId, Map<String, Object> request) {
    log.info(
        "DAO: Updating experiment, projectKey: {}, experimentId: {}, fields: {}",
        projectKey,
        experimentId,
        request != null ? request.keySet() : "null");

    try {
      Map<String, Object> requestMap = convertRequestMap(request);
      Map<String, Object> updates = buildUpdatesMap(requestMap);

      if (updates.isEmpty()) {
        log.info(
            "DAO: No valid fields to update for projectKey: {}, experimentId: {}",
            projectKey,
            experimentId);
        return Single.just(true);
      }

      log.debug("DAO: Fields to update: {}", updates.keySet());

      String query = buildUpdateQuery(updates, projectKey, experimentId);
      Tuple params = buildUpdateParams(updates, projectKey, experimentId);

      log.debug("DAO: Executing UPDATE query: {}", query);

      return executeUpdate(query, params, projectKey, experimentId);
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

  /**
   * Converts request map and handles special field transformations.
   *
   * <p>Performs end_date to end_time conversion if needed.
   *
   * @param request raw request map
   * @return converted request map with transformations applied
   */
  private Map<String, Object> convertRequestMap(Map<String, Object> request) {
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
            "Failed to parse end_date: {}, error: {}", requestMap.get("end_date"), e.getMessage());
      }
    }
    return requestMap;
  }

  /**
   * Builds map of valid updates from request map.
   *
   * <p>Filters out null values, validates against allowed columns, and processes field values based
   * on their types.
   *
   * @param requestMap converted request map
   * @return map of validated and processed updates
   */
  private Map<String, Object> buildUpdatesMap(Map<String, Object> requestMap) {
    Set<String> allowedColumns = getAllowedUpdateColumns();
    Map<String, Object> updates = new LinkedHashMap<>();

    for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (value != null && allowedColumns.contains(key)) {
        Object processedValue = processFieldValue(key, value);
        if (processedValue != null) {
          updates.put(key, processedValue);
        }
      }
    }
    return updates;
  }

  /**
   * Returns set of allowed columns for update operations.
   *
   * <p>Excludes primary keys and auto-managed fields like created_at, updated_at.
   *
   * @return set of allowed column names
   */
  private Set<String> getAllowedUpdateColumns() {
    return Set.of(
        "name",
        "description",
        "hypothesis",
        "status",
        "type",
        "guardrail_health_status",
        "cohorts",
        "variant_weights",
        "variants",
        "distribution_strategy",
        "assignment_domain",
        "assignment_strategy",
        "overrides",
        "winning_variant",
        "exposure",
        "threshold",
        "start_time",
        "end_time",
        "created_by");
  }

  /**
   * Processes field value based on its type.
   *
   * <p>Handles special conversions for cohorts (list to array), enums (to string), and JSONB fields
   * (to JSON string).
   *
   * @param key field name
   * @param value field value
   * @return processed value ready for database insertion
   */
  private Object processFieldValue(String key, Object value) {
    if (key.equals("cohorts") && value instanceof java.util.List) {
      return convertCohortsToArray(value);
    } else if (isEnumField(key)) {
      return convertEnumValue(value);
    } else if (isJsonbField(key)) {
      return serializeToJson(key, value);
    }
    return value;
  }

  /**
   * Converts cohorts list to string array for PostgreSQL varchar[] type.
   *
   * @param value cohorts list
   * @return string array
   */
  private String[] convertCohortsToArray(Object value) {
    @SuppressWarnings("unchecked")
    java.util.List<String> cohortList = (java.util.List<String>) value;
    return cohortList.toArray(new String[0]);
  }

  /**
   * Checks if field is an enum type.
   *
   * @param key field name
   * @return true if field is enum type
   */
  private boolean isEnumField(String key) {
    return key.equals("status")
        || key.equals("type")
        || key.equals("guardrail_health_status")
        || key.equals("assignment_strategy")
        || key.equals("distribution_strategy")
        || key.equals("assignment_domain");
  }

  /**
   * Converts enum value to string representation.
   *
   * @param value enum value
   * @return string representation of enum
   */
  private String convertEnumValue(Object value) {
    return value instanceof Enum ? ((Enum<?>) value).name() : value.toString();
  }

  /**
   * Checks if field is a JSONB type.
   *
   * @param key field name
   * @return true if field is JSONB type
   */
  private boolean isJsonbField(String key) {
    return key.equals("variant_weights")
        || key.equals("variants")
        || key.equals("rule_attributes")
        || key.equals("winning_variant");
  }

  /**
   * Serializes object to JSON string for JSONB fields.
   *
   * @param key field name for error logging
   * @param value object to serialize
   * @return JSON string representation
   * @throws RuntimeException if serialization fails
   */
  private String serializeToJson(String key, Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (Exception ex) {
      log.error("Failed to serialize JSONB field {}: {}", key, ex.getMessage());
      throw new RuntimeException("Failed to serialize JSONB field: " + key, ex);
    }
  }

  /**
   * Builds dynamic UPDATE SQL query with positional parameters.
   *
   * <p>Constructs SET clause with appropriate type casts and WHERE clause for project_key and
   * experiment_id.
   *
   * @param updates map of fields to update
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @return complete UPDATE SQL query string
   */
  private String buildUpdateQuery(Map<String, Object> updates, UUID projectKey, UUID experimentId) {
    StringBuilder query = new StringBuilder(WriteQuery.UPDATE_EXPERIMENT_PREFIX);
    int paramIndex = 1;

    for (String key : updates.keySet()) {
      query.append(key).append(" = $").append(paramIndex);
      appendTypeCast(query, key);
      query.append(", ");
      paramIndex++;
    }

    query.setLength(query.length() - 2); // Remove trailing ", "
    query
        .append(" WHERE project_key = $")
        .append(paramIndex)
        .append(" AND experiment_id = $")
        .append(paramIndex + 1);

    return query.toString();
  }

  /**
   * Appends PostgreSQL type cast to query for specific field types.
   *
   * @param query query string builder
   * @param key field name to determine type cast
   */
  private void appendTypeCast(StringBuilder query, String key) {
    if (key.equals("status")) {
      query.append("::experiment_status");
    } else if (key.equals("type")) {
      query.append("::experiment_type");
    } else if (key.equals("guardrail_health_status")) {
      query.append("::experiment_health");
    } else if (key.equals("assignment_strategy")) {
      query.append("::experiment_strategy");
    } else if (key.equals("distribution_strategy")) {
      query.append("::experiment_strategy");
    } else if (key.equals("assignment_domain")) {
      query.append("::assignment_domain");
    } else if (key.equals("cohorts")) {
      query.append("::varchar[]");
    } else if (isJsonbField(key)) {
      query.append("::jsonb");
    }
  }

  /**
   * Builds Tuple of parameters for prepared statement.
   *
   * <p>Adds update values followed by WHERE clause parameters (projectKey, experimentId).
   *
   * @param updates map of fields to update
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @return Tuple with all query parameters
   */
  private Tuple buildUpdateParams(Map<String, Object> updates, UUID projectKey, UUID experimentId) {
    Tuple params = Tuple.tuple();
    for (Object value : updates.values()) {
      params.addValue(value);
    }
    params.addString(projectKey.toString()).addString(experimentId.toString());
    return params;
  }

  /**
   * Executes UPDATE query and handles success/error logging.
   *
   * @param query SQL query string
   * @param params query parameters
   * @param projectKey project identifier for logging
   * @param experimentId experiment identifier for logging
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> executeUpdate(
      String query, Tuple params, UUID projectKey, UUID experimentId) {
    return pgWriterClient
        .execute(query, params)
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
  }

  /**
   * Updates experiment with tags and logs in a transaction.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param experimentFields map of experiment fields to update
   * @param tags list of tags to update (null if no tag update)
   * @param previousData experiment data before update
   * @param updatedBy user who updated the experiment
   * @return Maybe emitting true on success
   */
  @Override
  public Maybe<Boolean> updateWithTransaction(
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> experimentFields,
      List<String> tags,
      Map<String, Object> previousData,
      String updatedBy) {

    log.info(
        "DAO: Starting transactional update, projectKey: {}, experimentId: {}",
        projectKey,
        experimentId);

    return pgWriterClient.executeWithTransaction(
        connection ->
            updateExperimentFields(projectKey, experimentId, experimentFields)
                .flatMap(success -> validateUpdateSuccess(success, "experiment fields"))
                .flatMap(unused -> updateTagsIfPresent(connection, projectKey, experimentId, tags))
                .flatMap(success -> validateUpdateSuccess(success, "tags"))
                .flatMapMaybe(
                    unused ->
                        logUpdate(connection, projectKey, experimentId, previousData, updatedBy)));
  }

  /**
   * Updates experiment fields if not empty.
   *
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param fields fields to update
   * @return Single emitting true on success
   */
  private Single<Boolean> updateExperimentFields(
      UUID projectKey, UUID experimentId, Map<String, Object> fields) {
    return fields.isEmpty() ? Single.just(true) : updatePartial(projectKey, experimentId, fields);
  }

  /**
   * Updates tags if present.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param tags tags to update
   * @return Single emitting true on success
   */
  private Single<Boolean> updateTagsIfPresent(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      List<String> tags) {
    if (tags == null) {
      return Single.just(true);
    }

    return tagDAO
        .getActiveTags(connection, projectKey, experimentId)
        .flatMap(
            existingTags -> {
              List<String> tagsToRemove =
                  existingTags.stream()
                      .filter(tag -> !tags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              List<String> tagsToAdd =
                  tags.stream()
                      .filter(tag -> !existingTags.contains(tag))
                      .collect(java.util.stream.Collectors.toList());

              Single<Boolean> markInactive =
                  tagsToRemove.isEmpty()
                      ? Single.just(true)
                      : tagDAO.markTagsInactive(connection, projectKey, experimentId, tagsToRemove);

              Single<Boolean> insertNew =
                  tagsToAdd.isEmpty()
                      ? Single.just(true)
                      : tagDAO.batchInsertTags(connection, projectKey, experimentId, tagsToAdd);

              return markInactive.flatMap(
                  markSuccess -> {
                    if (!markSuccess) {
                      return Single.error(new RuntimeException("Failed to mark tags as inactive"));
                    }
                    return insertNew;
                  });
            });
  }

  /**
   * Logs the update operation.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param previousData data before update
   * @param updatedBy user who updated
   * @return Maybe emitting true on success
   */
  private Maybe<Boolean> logUpdate(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      UUID projectKey,
      UUID experimentId,
      Map<String, Object> previousData,
      String updatedBy) {

    return getExperimentData(projectKey, experimentId)
        .flatMap(
            currentData ->
                experimentUpdateLogDAO.insertUpdateLog(
                    connection, projectKey, experimentId, previousData, currentData, updatedBy))
        .map(success -> true)
        .toMaybe();
  }

  /**
   * Validates operation success.
   *
   * @param success operation result
   * @param operation operation name
   * @return Single emitting true on success, error otherwise
   */
  private Single<Boolean> validateUpdateSuccess(Boolean success, String operation) {
    return success
        ? Single.just(true)
        : Single.error(new RuntimeException("Failed to update " + operation));
  }
}
