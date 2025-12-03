package com.ascend.testlab.dao.mapper;

import com.ascend.testlab.constants.enums.AssignmentDomain;
import com.ascend.testlab.constants.enums.DistributionStrategy;
import com.ascend.testlab.constants.enums.ExperimentStatus;
import com.ascend.testlab.constants.enums.ExperimentType;
import com.ascend.testlab.constants.enums.HealthStatus;
import com.ascend.testlab.constants.postgresql.Columns;
import com.ascend.testlab.dto.entity.experiment.Experiment;
import com.ascend.testlab.dto.entity.experiment.RuleAttributes;
import com.ascend.testlab.dto.entity.experiment.Variant;
import com.ascend.testlab.dto.entity.experiment.WinningVariant;
import com.ascend.testlab.dto.entity.variantweights.CohortVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.StratifiedVariantWeights;
import com.ascend.testlab.dto.entity.variantweights.VariantWeights;
import com.ascend.testlab.exception.ErrorEnum;
import com.dream11.rest.exception.RestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper class for converting database rows to Experiment entities. Provides utility methods to
 * transform database query results into Java domain objects.
 *
 * @author Yashita Bansal
 * @version 1.0
 * @since 1.0
 * @see Experiment
 */
@Slf4j
public class ExperimentMapper {

  /**
   * Maps a database row to an Experiment entity.
   *
   * <p>Extracts all experiment-related fields from the database row and constructs an Experiment
   * object. Handles type conversions including enum mapping for status, type, health status, and
   * assignment strategy. Also processes JSON fields and aggregates tags and owners from the
   * database result.
   *
   * @param row the database row containing experiment data
   * @param mapper the object mapper
   * @return an Experiment object populated with data from the row
   */
  public static Experiment mapRowToExperiment(Row row, ObjectMapper mapper) {

    try {
      UUID experimentId = UUID.fromString(row.getString(Columns.EXPERIMENT_ID));

      ExperimentType experimentType =
          row.getString(Columns.TYPE) != null
              ? ExperimentType.fromValue(row.getString(Columns.TYPE))
              : null;
      HealthStatus guardrailHealthStatus =
          row.getString(Columns.GUARDRAIL_HEALTH_STATUS) != null
              ? HealthStatus.valueOf(row.getString(Columns.GUARDRAIL_HEALTH_STATUS))
              : null;
      List<String> cohorts =
          row.getArrayOfStrings(Columns.COHORTS) != null
              ? List.of(row.getArrayOfStrings(Columns.COHORTS))
              : null;
      Map<String, List<String>> overrides =
          deserializeOverrides(row.getJsonObject(Columns.OVERRIDES), mapper, experimentId);
      List<String> tags =
          row.getColumnIndex(Columns.TAGS) < 0 || row.getString(Columns.TAGS) == null
              ? null
              : Arrays.stream(row.getString(Columns.TAGS).split(",")).toList();
      List<String> owners =
          row.getColumnIndex(Columns.OWNERS) < 0 || row.getString(Columns.OWNERS) == null
              ? null
              : Arrays.stream(row.getString(Columns.OWNERS).split(",")).toList();

      AssignmentDomain assignmentDomain =
          AssignmentDomain.valueOf(row.getString(Columns.ASSIGNMENT_DOMAIN));
      VariantWeights variantWeights =
          deserializeVariantWeights(
              row.getJsonObject(Columns.VARIANT_WEIGHTS), assignmentDomain, mapper, experimentId);
      Map<String, Variant> variants =
          deserializeVariants(row.getJsonObject(Columns.VARIANTS), mapper, experimentId);
      List<RuleAttributes> ruleAttributes =
          deserializeRuleAttributes(
              row.getJsonArray(Columns.RULE_ATTRIBUTES), mapper, experimentId);
      WinningVariant winningVariant =
          deserializeWinningVariant(
              row.getJsonObject(Columns.WINNING_VARIANT), mapper, experimentId);

      return Experiment.builder()
          .projectKey(row.getString(Columns.PROJECT_KEY))
          .experimentId(experimentId)
          .name(row.getString(Columns.NAME))
          .experimentKey(row.getString(Columns.EXPERIMENT_KEY))
          .description(row.getString(Columns.DESCRIPTION))
          .hypothesis(row.getString(Columns.HYPOTHESIS))
          .status(ExperimentStatus.valueOf(row.getString(Columns.STATUS)))
          .type(experimentType)
          .guardrailHealthStatus(guardrailHealthStatus)
          .cohorts(cohorts)
          .variantWeights(variantWeights)
          .variants(variants)
          .distributionStrategy(
              DistributionStrategy.valueOf(row.getString(Columns.DISTRIBUTION_STRATEGY)))
          .assignmentDomain(assignmentDomain)
          .overrides(overrides)
          .ruleAttributes(ruleAttributes)
          .winningVariant(winningVariant)
          .exposure(row.getInteger(Columns.EXPOSURE))
          .threshold(row.getLong(Columns.THRESHOLD))
          .startTime(row.getLong(Columns.START_TIME))
          .endTime(row.getLong(Columns.END_TIME))
          .createdBy(row.getString(Columns.CREATED_BY))
          .createdAt(row.getOffsetDateTime(Columns.CREATED_AT).toInstant())
          .updatedAt(row.getOffsetDateTime(Columns.UPDATED_AT).toInstant())
          .tags(tags)
          .owners(owners)
          .build();
    } catch (Exception e) {
      log.error("Error mapping row to Experiment: {}", e.getMessage(), e);
      throw new RestException(ErrorEnum.ROW_MAPPING_FAILED, e);
    }
  }

  private static WinningVariant deserializeWinningVariant(
      JsonObject winningVariantJson, ObjectMapper mapper, UUID experimentId)
      throws JsonProcessingException {
    if (Objects.isNull(winningVariantJson)) {
      log.warn("Null winning variant JSON for experiment {}", experimentId);
      return null;
    }
    try {
      String jsonString = winningVariantJson.toString();
      WinningVariant winningVariant = mapper.readValue(jsonString, WinningVariant.class);

      log.debug("Deserialized winning variant for experiment {}: {}", experimentId, winningVariant);
      return winningVariant;
    } catch (Exception e) {
      log.error("Error deserializing winning variant for experiment {} ", experimentId, e);
      throw e;
    }
  }

  /**
   * Deserializes variant weights JSON into the appropriate VariantWeights subclass based on
   * assignment domain.
   *
   * @param variantWeightsJson the JSON object containing variant weights
   * @param assignmentDomain the assignment domain type
   * @param objectMapper the ObjectMapper for JSON deserialization
   * @param experimentId experiment ID for logging
   * @return appropriate VariantWeights implementation, or null if null
   * @throws Exception if deserialization fails
   */
  private static VariantWeights deserializeVariantWeights(
      JsonObject variantWeightsJson,
      AssignmentDomain assignmentDomain,
      ObjectMapper objectMapper,
      UUID experimentId)
      throws Exception {

    if (Objects.isNull(variantWeightsJson)) {
      log.warn("Null variant weights JSON for experiment {}", experimentId);
      return null;
    }

    try {
      String jsonString = variantWeightsJson.toString();
      VariantWeights variantWeights =
          switch (assignmentDomain) {
            case STRATIFIED -> objectMapper.readValue(jsonString, StratifiedVariantWeights.class);
            case COHORT -> objectMapper.readValue(jsonString, CohortVariantWeights.class);
          };

      log.debug("Deserialized VariantWeights for experiment {}: {}", experimentId, variantWeights);
      return variantWeights;
    } catch (Exception e) {
      log.error(
          "Error deserializing variant weights for experiment {} with domain {}",
          experimentId,
          assignmentDomain,
          e);
      throw e;
    }
  }

  /**
   * Deserializes variants JSON into a map of Variant objects.
   *
   * @param variantsJson the JSON object containing variants
   * @param objectMapper the ObjectMapper for JSON deserialization
   * @param experimentId experiment ID for logging
   * @return map of variant keys to Variant objects, or empty map if null
   * @throws Exception if deserialization fails
   */
  private static Map<String, Variant> deserializeVariants(
      JsonObject variantsJson, ObjectMapper objectMapper, UUID experimentId) throws Exception {

    if (Objects.isNull(variantsJson)) {
      log.warn("Null variants JSON for experiment {}", experimentId);
      return Map.of();
    }

    try {
      String jsonString = variantsJson.toString();
      Map<String, Variant> variants =
          objectMapper.readValue(
              jsonString,
              objectMapper
                  .getTypeFactory()
                  .constructMapType(Map.class, String.class, Variant.class));

      log.debug("Deserialized Variants for experiment {}: {}", experimentId, variants);
      return variants;
    } catch (Exception e) {
      log.error("Error deserializing variants for experiment {}", experimentId, e);
      throw e;
    }
  }

  /**
   * Deserializes rule attributes JSON into a list of RuleAttributes objects.
   *
   * @param ruleAttributesJson the JSON object containing rule attributes
   * @param objectMapper the ObjectMapper for JSON deserialization
   * @param experimentId experiment ID for logging
   * @return list of RuleAttributes, or empty list if null
   * @throws Exception if deserialization fails
   */
  private static List<RuleAttributes> deserializeRuleAttributes(
      JsonArray ruleAttributesJson, ObjectMapper objectMapper, UUID experimentId) throws Exception {

    if (Objects.isNull(ruleAttributesJson)) {
      log.warn("Null rule attributes JSON for experiment {}", experimentId);
      return List.of();
    }

    try {
      String jsonString = ruleAttributesJson.toString();
      List<RuleAttributes> ruleAttributes =
          objectMapper.readValue(
              jsonString,
              objectMapper
                  .getTypeFactory()
                  .constructCollectionType(List.class, RuleAttributes.class));

      log.debug("Deserialized RuleAttributes for experiment {}: {}", experimentId, ruleAttributes);
      return ruleAttributes;
    } catch (Exception e) {
      log.error("Error deserializing rule attributes for experiment {}", experimentId, e);
      throw e;
    }
  }

  /**
   * Deserializes overrides JSON into a map of variant names to user ID lists.
   *
   * @param overridesJson the JSON object containing overrides
   * @param objectMapper the ObjectMapper for JSON deserialization
   * @param experimentId experiment ID for logging
   * @return map of variant names to user IDs, or null if null
   * @throws Exception if deserialization fails
   */
  private static Map<String, List<String>> deserializeOverrides(
      JsonObject overridesJson, ObjectMapper objectMapper, UUID experimentId) throws Exception {

    if (Objects.isNull(overridesJson)) {
      log.debug("Null overrides JSON for experiment {}", experimentId);
      return null;
    }

    try {
      String jsonString = overridesJson.toString();
      Map<String, List<String>> overrides =
          objectMapper.readValue(
              jsonString,
              objectMapper
                  .getTypeFactory()
                  .constructMapType(
                      Map.class,
                      objectMapper.getTypeFactory().constructType(String.class),
                      objectMapper
                          .getTypeFactory()
                          .constructCollectionType(List.class, String.class)));

      log.debug("Deserialized Overrides for experiment {}: {}", experimentId, overrides);
      return overrides;
    } catch (Exception e) {
      log.error("Error deserializing overrides for experiment {}", experimentId, e);
      throw e;
    }
  }
}
