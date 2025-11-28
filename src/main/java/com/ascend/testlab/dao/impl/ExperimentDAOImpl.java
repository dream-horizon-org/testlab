package com.ascend.testlab.dao.impl;

import com.ascend.testlab.client.postgresql.PgReaderClient;
import com.ascend.testlab.client.postgresql.PgWriterClient;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.constants.postgresql.ReadQuery;
import com.ascend.testlab.constants.postgresql.WriteQuery;
import com.ascend.testlab.dao.ExperimentDAO;
import com.ascend.testlab.dao.mapper.ExperimentMapper;
import com.ascend.testlab.dao.querybuilder.core.ParameterizedQuery;
import com.ascend.testlab.dao.querybuilder.factory.FilterExperimentsQueryFactory;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.Metrics;
import com.ascend.testlab.dto.request.FilterExperimentsRequest;
import com.ascend.testlab.dto.request.UpdateExperimentRequest;
import com.ascend.testlab.dto.response.FilterExperimentsResponse;
import com.ascend.testlab.dto.response.PaginationMeta;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

/**
 * Implementation of the ExperimentDAO interface.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see ExperimentDAO
 */
@Slf4j
public class ExperimentDAOImpl implements ExperimentDAO {

  private final PgReaderClient pgReaderClient;
  private final PgWriterClient pgWriterClient;
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new ExperimentDAOImpl.
   *
   * @param pgReaderClient the Postgres reader client
   * @param pgWriterClient the Postgres writer client
   * @param objectMapper the Jackson ObjectMapper for JSON serialization/deserialization
   */
  @Inject
  public ExperimentDAOImpl(
      PgReaderClient pgReaderClient, PgWriterClient pgWriterClient, ObjectMapper objectMapper) {
    this.pgReaderClient = pgReaderClient;
    this.pgWriterClient = pgWriterClient;
    this.objectMapper = objectMapper;
  }

  /** {@inheritDoc} */
  @Override
  public Maybe<com.ascend.testlab.dto.entity.experiment.Experiment> getExperiment(
      String projectKey, String experimentId) {
    return pgReaderClient.fetchOne(
        ReadQuery.FETCH_EXPERIMENT,
        Tuple.of(projectKey, experimentId),
        row -> ExperimentMapper.mapRowToExperiment(row, objectMapper));
  }

  /** {@inheritDoc} */
  @Override
  public Single<FilterExperimentsResponse> filterExperiments(
      String projectKey, FilterExperimentsRequest req) {
    ParameterizedQuery parameterizedQuery =
        FilterExperimentsQueryFactory.buildQuery(projectKey, req);

    return pgReaderClient
        .fetchAll(parameterizedQuery.query(), parameterizedQuery.tuple(), row -> row)
        .map(rows -> mapRowsToFilteredExperiment(rows, req));
  }

  /**
   * Maps database rows to a FilterExperimentsResponse object. Extracts experiment data from rows
   * and builds pagination metadata.
   */
  private FilterExperimentsResponse mapRowsToFilteredExperiment(
      List<Row> rows, FilterExperimentsRequest req) {
    FilterExperimentsResponse response = new FilterExperimentsResponse();

    int totalCount = (rows.isEmpty()) ? 0 : rows.get(0).getInteger(Columns.TOTAL_COUNT);

    List<Experiment> experiments =
        (rows.isEmpty())
            ? List.of()
            : rows.stream()
                .map(row -> ExperimentMapper.mapRowToExperiment(row, objectMapper))
                .toList();

    response.setExperiments(experiments);

    PaginationMeta paginationMeta;
    paginationMeta =
        PaginationMeta.builder()
            .pageSize(experiments.size())
            .currentPage(req.getPage())
            .totalCount(totalCount)
            .build();

    response.setPagination(paginationMeta);

    return response;
  }

  /** {@inheritDoc} */
  @Override
  public Single<Boolean> deleteExperiment(String projectKey, Experiment experiment) {
    Tuple tuple = Tuple.of(projectKey, experiment.getExperimentId().toString());

    JsonObject previous_data_json = JsonObject.mapFrom(experiment);

    // make this parallel
    return pgWriterClient
        .executeWithTransaction(
            (sqlConnection) ->
                pgWriterClient
                    .execute(sqlConnection, WriteQuery.DELETE_EXPERIMENT_BY_ID, tuple)
                    .flatMap(
                        delExp ->
                            pgWriterClient.execute(
                                sqlConnection, WriteQuery.DELETE_TAG_FOR_EXPERIMENT, tuple))
                    .flatMap(
                        delTag ->
                            pgWriterClient.execute(
                                sqlConnection, WriteQuery.DELETE_OWNER_FOR_EXPERIMENT, tuple))
                    .flatMap(
                        delOwner ->
                            pgWriterClient.execute(
                                sqlConnection,
                                WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG,
                                tuple.addJsonObject(previous_data_json).addJsonObject(null)))
                    .map(result -> true)
                    .toMaybe(),
            false)
        .onErrorResumeNext(
            err -> {
              log.error(
                  "Failed to delete experiment for projectKey: {}, experimentId: {}, error: {}",
                  projectKey,
                  experiment.getExperimentId(),
                  err.getMessage());
              return Single.error(new RestException(ErrorEnum.REST_DELETE_EXPERIMENT_FAILED, err));
            });
  }

  /**
   * Creates a new experiment using an existing SQL connection (for transaction support).
   *
   * @param connection the SQL connection to use
   * @param projectKey project identifier for partitioning
   * @param experiment experiment creation experiment
   * @return Single emitting experiment ID as String on success
   */
  private Single<Boolean> insertExperiment(
      SqlConnection connection, String projectKey, Experiment experiment) {
    log.info(
        "DAO: Creating experiment with connection, projectKey: {}, experimentId: {}, name: {}",
        projectKey,
        experiment.getExperimentId(),
        experiment.getName());

    Tuple tuple =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experiment.getExperimentId().toString())
            .addString(experiment.getName())
            .addString(experiment.getExperimentKey())
            .addString(experiment.getDescription())
            .addString(experiment.getHypothesis())
            .addString(experiment.getStatus().name())
            .addString(experiment.getType().getValue())
            .addValue(experiment.getCohorts().toArray(new String[0]))
            .addJsonObject(JsonObject.mapFrom(experiment.getVariantWeights()))
            .addJsonObject(JsonObject.mapFrom(experiment.getVariants()))
            .addString(experiment.getDistributionStrategy().name())
            .addString(experiment.getAssignmentDomain().name())
            .addValue(
                experiment.getOverrides() == null || experiment.getOverrides().isEmpty()
                    ? null
                    : experiment.getOverrides().toArray(new String[0]))
            .addJsonArray(JsonArray.of(experiment.getRuleAttributes()))
            .addValue(experiment.getExposure())
            .addValue(experiment.getThreshold())
            .addValue(experiment.getStartTime())
            .addValue(experiment.getEndTime())
            .addValue(experiment.getCreatedBy());

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT, tuple)
        .onErrorReturn(
            err -> {
              log.error("Error while inserting experiment", err);
              return false;
            });
  }

  /**
   * Creates experiment with tags, owner, update log, and analysis in a transaction.
   *
   * <p>Extracts projectKey, experimentId, tags, and owner from the experiment object and executes
   * all insert operations in parallel within a transaction.
   *
   * @param experiment experiment creation experiment with all experiment details including
   *     projectKey, experimentId, tags, and owner
   * @return Single emitting experiment ID as String on success
   */
  @Override
  public Single<Boolean> createExperiment(String projectKey, Experiment experiment) {

    log.info(
        "DAO: Creating experiment, projectKey: {}, experimentId: {}",
        projectKey,
        experiment.getExperimentId());

    return pgWriterClient.executeWithTransaction(
        connection -> createExperiment(connection, projectKey, experiment), false);
  }

  private Maybe<Boolean> createExperiment(
      SqlConnection connection, String projectKey, Experiment experiment) {

    return insertExperiment(connection, projectKey, experiment)
        .flatMap(
            success -> {
              log.info(
                  "DAO: Experiment created successfully, inserting related data for experimentId: {}",
                  experiment.getExperimentId());

              return Single.zip(
                      insertTags(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getTags()),
                      insertOwners(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getOwners()),
                      insertUpdateLog(connection, projectKey, null, experiment),
                      insertAnalysis(
                          connection,
                          projectKey,
                          experiment.getExperimentId(),
                          experiment.getMetrics()),
                      (tagsSuccess, ownerSuccess, updateLogSuccess, analysisSuccess) ->
                          tagsSuccess && ownerSuccess && updateLogSuccess && analysisSuccess)
                  .map(res -> res);
            })
        .toMaybe();
  }

  /**
   * Gets current experiment data as a map for update log.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting map of experiment data
   */
  @Override
  public Single<Map<String, Object>> getExperimentData(String projectKey, UUID experimentId) {
    log.debug(
        "DAO: Getting experiment data for projectKey: {}, experimentId: {}",
        projectKey,
        experimentId);

    Tuple params = Tuple.tuple().addString(projectKey).addString(experimentId.toString());

    return pgReaderClient
        .fetchOne(ReadQuery.FETCH_EXPERIMENT_DATA, params, this::rowToMap)
        .doOnSuccess(
            data ->
                log.info(
                    "DAO: Successfully retrieved experiment data for experimentId: {}, fields: {}",
                    experimentId,
                    data.keySet()))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning empty map for get experiment data, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return new HashMap<>();
            })
        .toSingle();
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
      String projectKey, UUID experimentId, Map<String, Object> request) {
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
   * Updates experiment fields partially using an existing SQL connection (for transaction support).
   *
   * @param connection the SQL connection to use
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param request map of field names to values for update
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> updatePartial(
      SqlConnection connection, String projectKey, UUID experimentId, Map<String, Object> request) {
    log.info(
        "DAO: Updating experiment with connection, projectKey: {}, experimentId: {}, fields: {}",
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

      log.debug("DAO: Executing UPDATE query with connection: {}", query);

      return executeUpdate(connection, query, params, projectKey, experimentId);
    } catch (Exception e) {
      log.error(
          "DAO: Exception in update experiment with connection, projectKey: {}, experimentId: {}, error: {}",
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
        objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {});

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
    return Constants.UPDATABLE_FIELDS;
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
    } else if (key.equals("overrides") && value instanceof java.util.List) {
      return convertCohortsToArray(value); // Convert to array like cohorts
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
   * @throws RestException if serialization fails
   */
  private String serializeToJson(String key, Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      log.error("Failed to serialize JSONB field {}: {}", key, ex.getMessage(), ex);
      throw new RestException(ErrorEnum.JSONB_SERIALIZATION_FAILED, ex);
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
  private String buildUpdateQuery(
      Map<String, Object> updates, String projectKey, UUID experimentId) {
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
  private Tuple buildUpdateParams(
      Map<String, Object> updates, String projectKey, UUID experimentId) {
    Tuple params = Tuple.tuple();
    for (Object value : updates.values()) {
      params.addValue(value);
    }
    params.addString(projectKey).addString(experimentId.toString());
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
      String query, Tuple params, String projectKey, UUID experimentId) {
    return pgWriterClient
        .execute(query, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully updated experiment, projectKey: {}, experimentId: {}, success: {}",
                    projectKey,
                    experimentId,
                    success))
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
   * Executes update query using an existing SQL connection (for transaction support).
   *
   * @param connection the SQL connection to use
   * @param query the UPDATE query
   * @param params query parameters
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> executeUpdate(
      SqlConnection connection, String query, Tuple params, String projectKey, UUID experimentId) {
    return pgWriterClient
        .execute(connection, query, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully updated experiment with connection, projectKey: {}, experimentId: {}, success: {}",
                    projectKey,
                    experimentId,
                    success))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for failed experiment update with connection, projectKey: {}, experimentId: {}, error: {}",
                  projectKey,
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Updates experiment with tags, owners, metrics and logs in a transaction.
   *
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to update (null if no tag update)
   * @param owners list of owners to update (null if no owner update)
   * @param metrics map of metrics to update (null if no metrics update)
   * @param previousData experiment data before update
   * @param updatedBy user who updated the experiment
   * @return Single emitting true on success
   */
  @Override
  public Single<Boolean> updateWithTransaction(
      String projectKey,
      UUID experimentId,
      UpdateExperimentRequest request,
      List<String> tags,
      List<String> owners,
      Map<String, List<String>> metrics,
      Map<String, Object> previousData,
      String updatedBy) {

    log.info(
        "DAO: Starting transactional update, projectKey: {}, experimentId: {}",
        projectKey,
        experimentId);

    // Extract non-null fields from request POJO for dynamic SQL generation
    Map<String, Object> experimentFields = extractNonNullFields(request);

    return pgWriterClient.executeWithTransaction(
        connection ->
            updateExperimentFields(connection, projectKey, experimentId, experimentFields)
                .flatMap(success -> validateUpdateSuccess(success, "experiment fields"))
                .flatMap(unused -> updateTagsIfPresent(connection, projectKey, experimentId, tags))
                .flatMap(success -> validateUpdateSuccess(success, "tags"))
                .flatMap(
                    unused -> updateOwnersIfPresent(connection, projectKey, experimentId, owners))
                .flatMap(success -> validateUpdateSuccess(success, "owners"))
                .flatMap(
                    unused -> updateMetricsIfPresent(connection, projectKey, experimentId, metrics))
                .flatMap(success -> validateUpdateSuccess(success, "metrics"))
                .flatMap(
                    unused ->
                        logUpdate(
                            connection,
                            projectKey,
                            experimentId,
                            experimentFields,
                            previousData,
                            updatedBy))
                .toMaybe(),
        false);
  }

  /**
   * Updates experiment fields if not empty.
   *
   * @param connection SQL connection
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param fields fields to update
   * @return Single emitting true on success
   */
  private Single<Boolean> updateExperimentFields(
      SqlConnection connection, String projectKey, UUID experimentId, Map<String, Object> fields) {
    return fields.isEmpty()
        ? Single.just(true)
        : updatePartial(connection, projectKey, experimentId, fields);
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
      String projectKey,
      UUID experimentId,
      List<String> tags) {
    if (tags == null) {
      return Single.just(true);
    }

    return getTags(connection, projectKey, experimentId)
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

              Single<Boolean> deleteTags =
                  tagsToRemove.isEmpty()
                      ? Single.just(true)
                      : deleteTags(connection, projectKey, experimentId, tagsToRemove);

              Single<Boolean> insertNew =
                  tagsToAdd.isEmpty()
                      ? Single.just(true)
                      : insertTags(connection, projectKey, experimentId, tagsToAdd);

              return deleteTags.flatMap(
                  deleteSuccess -> {
                    if (!deleteSuccess) {
                      RuntimeException err = new RuntimeException("Failed to delete tags");
                      return Single.error(
                          ErrorEnum.handleException(
                              err, new RestException(ErrorEnum.EXPERIMENT_UPDATE_FAILED, err)));
                    }
                    return insertNew;
                  });
            });
  }

  /**
   * Updates owners if present in the update request.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param owners list of owners to update (null if no update)
   * @return Single emitting true on success
   */
  private Single<Boolean> updateOwnersIfPresent(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      String projectKey,
      UUID experimentId,
      List<String> owners) {
    if (owners == null) {
      return Single.just(true);
    }

    return getOwners(connection, projectKey, experimentId)
        .flatMap(
            existingOwners -> {
              List<String> ownersToRemove =
                  existingOwners.stream()
                      .filter(owner -> !owners.contains(owner))
                      .collect(java.util.stream.Collectors.toList());

              List<String> ownersToAdd =
                  owners.stream()
                      .filter(owner -> !existingOwners.contains(owner))
                      .collect(java.util.stream.Collectors.toList());

              Single<Boolean> deleteOwners =
                  ownersToRemove.isEmpty()
                      ? Single.just(true)
                      : deleteOwners(connection, projectKey, experimentId, ownersToRemove);

              Single<Boolean> insertNew =
                  ownersToAdd.isEmpty()
                      ? Single.just(true)
                      : insertOwners(connection, projectKey, experimentId, ownersToAdd);

              return deleteOwners.flatMap(
                  deleteSuccess -> {
                    if (!deleteSuccess) {
                      RuntimeException err = new RuntimeException("Failed to delete owners");
                      return Single.error(
                          ErrorEnum.handleException(
                              err, new RestException(ErrorEnum.EXPERIMENT_UPDATE_FAILED, err)));
                    }
                    return insertNew;
                  });
            });
  }

  /**
   * Updates metrics if present in the update request.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param metrics map of metrics to update (null if no update)
   * @return Single emitting true on success
   */
  private Single<Boolean> updateMetricsIfPresent(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      String projectKey,
      UUID experimentId,
      Map<String, List<String>> metrics) {
    if (metrics == null) {
      return Single.just(true);
    }

    // For metrics, we'll do a simple delete and re-insert
    // First delete existing metrics, then insert new ones
    return deleteAllMetrics(connection, projectKey, experimentId)
        .flatMap(
            deleteSuccess -> {
              if (!deleteSuccess) {
                RuntimeException err = new RuntimeException("Failed to delete existing metrics");
                return Single.error(
                    ErrorEnum.handleException(
                        err, new RestException(ErrorEnum.EXPERIMENT_UPDATE_FAILED, err)));
              }
              return insertMetrics(connection, projectKey, experimentId, metrics);
            });
  }

  /**
   * Logs experiment update by computing current data from previous data and updates.
   *
   * <p>This method avoids an extra database call by merging the previous data with the update
   * fields to compute the current state.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier
   * @param experimentId experiment identifier
   * @param experimentFields fields that were updated
   * @param previousData experiment data before update
   * @param updatedBy user who updated the experiment
   * @return Single emitting true on success
   */
  private Single<Boolean> logUpdate(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      String projectKey,
      UUID experimentId,
      Map<String, Object> experimentFields,
      Map<String, Object> previousData,
      String updatedBy) {

    log.debug(
        "DAO: Computing current data from previous data and {} updated fields",
        experimentFields.size());

    // Compute current data by merging previous data with updates
    Map<String, Object> currentData = new HashMap<>(previousData);
    currentData.putAll(experimentFields);

    log.debug("DAO: Current data computed, logging update for experimentId: {}", experimentId);

    return Single.just(true);

    //    return insertUpdateLog(
    //            connection, projectKey, experimentId, previousData, currentData, updatedBy)
    //        .map(success -> true);
  }

  /**
   * Validates operation success.
   *
   * @param success operation result
   * @param operation operation name
   * @return Single emitting true on success, error otherwise
   */
  private Single<Boolean> validateUpdateSuccess(Boolean success, String operation) {
    if (success) {
      return Single.just(true);
    } else {
      RuntimeException err = new RuntimeException("Failed to update " + operation);
      return Single.error(
          ErrorEnum.handleException(
              err, new RestException(ErrorEnum.EXPERIMENT_UPDATE_FAILED, err)));
    }
  }

  // ==================== Tag Operations ====================

  /**
   * Batch inserts tags for an experiment using a single query.
   *
   * <p>This method uses batch insert for optimal performance, inserting all tags in a single
   * database round trip.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to insert
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertTags(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags) {
    if (Objects.isNull(tags) || tags.isEmpty()) {
      log.debug("No tags to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Batch inserting {} tags for experimentId: {}, projectKey: {}",
        tags.size(),
        experimentId,
        projectKey);

    List<Tuple> tuples = new ArrayList<>();
    for (String tag : tags) {
      Tuple params =
          Tuple.tuple().addString(experimentId.toString()).addString(projectKey).addString(tag);
      tuples.add(params);
    }

    return pgWriterClient
        .executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_TAG, tuples)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted {} tags for experimentId: {}, projectKey: {}",
                    tags.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for tag batch insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Gets tags for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting list of active tag names
   */
  @Override
  public Single<List<String>> getTags(
      SqlConnection connection, String projectKey, UUID experimentId) {
    log.debug("DAO: Getting tags for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params = Tuple.tuple().addString(projectKey).addString(experimentId.toString());

    return pgReaderClient
        .fetchAll(WriteQuery.GET_TAGS, params, row -> row.getString("tag"))
        .doOnSuccess(
            tags ->
                log.info(
                    "DAO: Retrieved {} tags for experimentId: {}, projectKey: {}",
                    tags.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning empty list for get tags, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return new ArrayList<>();
            });
  }

  /**
   * Marks tags as inactive (status = 0).
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param tags list of tags to delete
   * @return Single emitting true on success, false on failure
   */
  @Override
  public Single<Boolean> deleteTags(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      log.debug("DAO: No tags to delete for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Deleting {} tags for experimentId: {}, projectKey: {}",
        tags.size(),
        experimentId,
        projectKey);

    String[] tagsArray = tags.toArray(new String[0]);
    Tuple params =
        Tuple.tuple().addString(projectKey).addString(experimentId.toString()).addValue(tagsArray);

    return pgWriterClient
        .execute(connection, WriteQuery.DELETE_TAGS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully deleted {} tags for experimentId: {}, projectKey: {}",
                    tags.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for mark tags inactive, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Gets all owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting list of owner names
   */
  private Single<List<String>> getOwners(
      io.vertx.rxjava3.sqlclient.SqlConnection connection, String projectKey, UUID experimentId) {
    log.debug("DAO: Getting owners for experimentId: {}, projectKey: {}", experimentId, projectKey);

    String query =
        "SELECT owner FROM experiment.owners WHERE project_key = $1 AND experiment_id = $2 ORDER BY owner";
    Tuple params = Tuple.tuple().addString(projectKey).addString(experimentId.toString());

    return pgReaderClient
        .fetchAll(query, params, row -> row.getString("owner"))
        .doOnSuccess(
            owners ->
                log.info(
                    "DAO: Retrieved {} owners for experimentId: {}, projectKey: {}",
                    owners.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning empty list for get owners, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return new ArrayList<>();
            });
  }

  /**
   * Inserts an owner for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertOwners(
      SqlConnection connection, String projectKey, UUID experimentId, List<String> owners) {
    if (Objects.isNull(owners) || owners.isEmpty()) {
      log.debug("No owners to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Batch inserting {} owners for experimentId: {}, projectKey: {}",
        owners.size(),
        experimentId,
        projectKey);

    List<Tuple> tuples =
        owners.stream()
            .map(
                owner ->
                    Tuple.tuple()
                        .addString(experimentId.toString())
                        .addString(projectKey)
                        .addString(owner))
            .toList();

    return pgWriterClient
        .executeMultiple(connection, WriteQuery.INSERT_EXPERIMENT_OWNER, tuples)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted {} owners for experimentId: {}, projectKey: {}",
                    owners.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for owners batch insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Deletes specific owners for an experiment.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param ownersToDelete list of owner names to delete
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> deleteOwners(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      String projectKey,
      UUID experimentId,
      List<String> ownersToDelete) {
    if (ownersToDelete == null || ownersToDelete.isEmpty()) {
      return Single.just(true);
    }

    log.debug(
        "DAO: Deleting {} specific owners for experimentId: {}, projectKey: {}",
        ownersToDelete.size(),
        experimentId,
        projectKey);

    StringBuilder queryBuilder =
        new StringBuilder(
            "DELETE FROM experiment.owners WHERE project_key = $1 AND experiment_id = $2 AND owner IN (");

    for (int i = 0; i < ownersToDelete.size(); i++) {
      if (i > 0) queryBuilder.append(", ");
      queryBuilder.append("$").append(3 + i);
    }
    queryBuilder.append(")");

    Tuple params = Tuple.tuple().addString(projectKey).addString(experimentId.toString());
    for (String owner : ownersToDelete) {
      params.addString(owner);
    }

    return pgWriterClient
        .execute(connection, queryBuilder.toString(), params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully deleted {} owners for experimentId: {}, projectKey: {}",
                    ownersToDelete.size(),
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for specific owner deletion, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts an experiment update log entry.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param previousData previous experiment data (null for create operation)
   * @param currentData current experiment data
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertUpdateLog(
      SqlConnection connection,
      String projectKey,
      Experiment previousData,
      Experiment currentData) {
    log.debug(
        "DAO: Inserting update log for experimentId: {}, projectKey: {}",
        currentData.getExperimentId(),
        projectKey);

    Tuple tuple =
        Tuple.tuple()
            .addString(projectKey)
            .addString(currentData.getExperimentId().toString())
            .addJsonObject(JsonObject.mapFrom(previousData))
            .addJsonObject(JsonObject.mapFrom(currentData))
            .addString(currentData.getUpdatedBy());

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_UPDATE_LOG, tuple)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted update log for experimentId: {}, projectKey: {}",
                    currentData.getExperimentId(),
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for update log insert, experimentId: {}, error: {}",
                  currentData.getExperimentId(),
                  error.getMessage());
              return false;
            });
  }

  /**
   * Deletes all metrics for an experiment from the analysis table.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> deleteAllMetrics(
      io.vertx.rxjava3.sqlclient.SqlConnection connection, String projectKey, UUID experimentId) {
    log.debug(
        "DAO: Deleting all metrics for experimentId: {}, projectKey: {}", experimentId, projectKey);

    String query =
        "DELETE FROM experiment.experiment_analysis WHERE project_key = $1 AND experiment_id = $2";
    Tuple params = Tuple.tuple().addString(projectKey).addString(experimentId.toString());

    return pgWriterClient
        .execute(connection, query, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully deleted metrics for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for metrics deletion, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts metrics for an experiment into the analysis table.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param metrics list of metrics to insert
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertMetrics(
      io.vertx.rxjava3.sqlclient.SqlConnection connection,
      String projectKey,
      UUID experimentId,
      Map<String, List<String>> metrics) {
    if (metrics == null || metrics.isEmpty()) {
      log.debug("No metrics to insert for experimentId: {}", experimentId);
      return Single.just(true);
    }

    log.debug(
        "DAO: Inserting metrics for experimentId: {}, projectKey: {}", experimentId, projectKey);

    // Extract primary and secondary metrics from the map
    List<String> primaryMetricsList = metrics.get("primary");
    List<String> secondaryMetricsList = metrics.get("secondary");

    String primaryMetrics =
        (primaryMetricsList != null && !primaryMetricsList.isEmpty())
            ? String.join(",", primaryMetricsList)
            : null;
    String secondaryMetrics =
        (secondaryMetricsList != null && !secondaryMetricsList.isEmpty())
            ? String.join(",", secondaryMetricsList)
            : null;

    Tuple params =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experimentId.toString())
            .addValue(null)
            .addString(primaryMetrics)
            .addString(secondaryMetrics)
            .addValue(null);

    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_ANALYSIS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted metrics for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for metrics insertion, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Inserts an experiment analysis entry with default/null values.
   *
   * @param connection SQL connection for transaction
   * @param projectKey project identifier for partitioning
   * @param experimentId experiment identifier
   * @param metrics map of metrics (primary and secondary)
   * @return Single emitting true on success, false on failure
   */
  private Single<Boolean> insertAnalysis(
      SqlConnection connection, String projectKey, UUID experimentId, Metrics metrics) {

    if (Objects.isNull(metrics)) {
      return Single.just(true);
    }

    log.debug(
        "DAO: Inserting analysis for experimentId: {}, projectKey: {}", experimentId, projectKey);

    Tuple params =
        Tuple.tuple()
            .addString(projectKey)
            .addString(experimentId.toString())
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getPrimary()).toArray(new String[0]))
            .addArrayOfString(ListUtils.emptyIfNull(metrics.getSecondary()).toArray(new String[0]));
    return pgWriterClient
        .execute(connection, WriteQuery.INSERT_EXPERIMENT_ANALYSIS, params)
        .doOnSuccess(
            success ->
                log.info(
                    "DAO: Successfully inserted analysis for experimentId: {}, projectKey: {}",
                    experimentId,
                    projectKey))
        .onErrorReturn(
            error -> {
              log.error(
                  "DAO: Returning false for analysis insert, experimentId: {}, error: {}",
                  experimentId,
                  error.getMessage());
              return false;
            });
  }

  /**
   * Extracts non-null fields from UpdateExperimentRequest for dynamic SQL generation.
   *
   * <p>Uses reflection to iterate through all fields in the request DTO and extracts non-null
   * values. Field names are converted to snake_case using the @JsonProperty annotation. Excludes
   * fields that are not part of the experiments table (tags, owner, metrics, updated_by) as these
   * are handled separately.
   *
   * <p>The extracted fields are used to build dynamic UPDATE SQL statements, allowing partial
   * updates where only the provided fields are modified.
   *
   * @param request update experiment request DTO containing the fields to update
   * @return map of non-null field names (snake_case) to their values, ready for SQL generation
   * @throws RestException with REQU EST_FIELD_EXTRACTION_FAILED if reflection fails
   */
  private Map<String, Object> extractNonNullFields(UpdateExperimentRequest request) {
    Map<String, Object> fields = new LinkedHashMap<>();

    try {
      // Use reflection to get all fields from the request
      for (java.lang.reflect.Field field : UpdateExperimentRequest.class.getDeclaredFields()) {
        field.setAccessible(true);
        Object value = field.get(request);

        // Skip null values and fields that are not in the experiments table
        if (value == null) {
          continue;
        }

        // Get the JSON property name (snake_case)
        com.fasterxml.jackson.annotation.JsonProperty jsonProperty =
            field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
        String fieldName = jsonProperty != null ? jsonProperty.value() : field.getName();

        // Exclude fields that are not part of the experiments table
        if (fieldName.equals("tags")
            || fieldName.equals("owner")
            || fieldName.equals("metrics")
            || fieldName.equals("updated_by")) {
          continue;
        }

        fields.put(fieldName, value);
      }

      log.debug("DAO: Extracted {} non-null experiment fields for update", fields.size());
      return fields;

    } catch (IllegalAccessException e) {
      log.error(
          "DAO: Failed to extract fields from UpdateExperimentRequest: {}", e.getMessage(), e);
      throw new RestException(ErrorEnum.REQUEST_FIELD_EXTRACTION_FAILED, e);
    }
  }
}
