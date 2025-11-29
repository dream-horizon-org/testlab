package com.ascend.testlab.dao.querybuilder;

import com.ascend.testlab.dto.entity.experiment.Experiment;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * Builds dynamic UPDATE query for experiments based on changed fields only.
 *
 * <p>Only includes fields that have actually changed between previous and updated experiment.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Getter
public class ExperimentUpdateQueryBuilder {

  private final List<String> setClauses = new ArrayList<>();
  private final Tuple tuple = Tuple.tuple();
  private int paramIndex = 1;

  /**
   * Builds a dynamic UPDATE query comparing previous and updated experiment.
   *
   * @param previous the existing experiment state
   * @param updated the new experiment state
   * @param projectKey project identifier
   * @return the builder with query and tuple ready
   */
  public static ExperimentUpdateQueryBuilder build(
      Experiment previous, Experiment updated, String projectKey) {

    ExperimentUpdateQueryBuilder builder = new ExperimentUpdateQueryBuilder();

    builder.addIfChanged("name", previous.getName(), updated.getName());
    builder.addIfChanged("experiment_key", previous.getExperimentKey(), updated.getExperimentKey());
    builder.addIfChanged("description", previous.getDescription(), updated.getDescription());
    builder.addIfChanged("hypothesis", previous.getHypothesis(), updated.getHypothesis());

    builder.addEnumIfChanged(
        "status",
        "experiment.experiment_status",
        previous.getStatus() != null ? previous.getStatus().name() : null,
        updated.getStatus() != null ? updated.getStatus().name() : null);

    builder.addEnumIfChanged(
        "guardrail_health_status",
        "experiment.experiment_health",
        previous.getGuardrailHealthStatus() != null
            ? previous.getGuardrailHealthStatus().name()
            : null,
        updated.getGuardrailHealthStatus() != null
            ? updated.getGuardrailHealthStatus().name()
            : null);

    builder.addArrayIfChanged("cohorts", previous.getCohorts(), updated.getCohorts());

    builder.addJsonIfChanged(
        "variant_weights",
        previous.getVariantWeights() != null
            ? JsonObject.mapFrom(previous.getVariantWeights())
            : null,
        updated.getVariantWeights() != null
            ? JsonObject.mapFrom(updated.getVariantWeights())
            : null);

    builder.addJsonIfChanged(
        "variants",
        previous.getVariants() != null ? JsonObject.mapFrom(previous.getVariants()) : null,
        updated.getVariants() != null ? JsonObject.mapFrom(updated.getVariants()) : null);

    builder.addJsonArrayIfChanged(
        "rule_attributes",
        previous.getRuleAttributes() != null ? new JsonArray(previous.getRuleAttributes()) : null,
        updated.getRuleAttributes() != null ? new JsonArray(updated.getRuleAttributes()) : null);

    builder.addArrayIfChanged("overrides", previous.getOverrides(), updated.getOverrides());

    builder.addJsonIfChanged(
        "winning_variant",
        previous.getWinningVariant() != null
            ? JsonObject.mapFrom(previous.getWinningVariant())
            : null,
        updated.getWinningVariant() != null
            ? JsonObject.mapFrom(updated.getWinningVariant())
            : null);

    builder.addIfChanged("exposure", previous.getExposure(), updated.getExposure());
    builder.addIfChanged("threshold", previous.getThreshold(), updated.getThreshold());
    builder.addIfChanged("start_time", previous.getStartTime(), updated.getStartTime());
    builder.addIfChanged("end_time", previous.getEndTime(), updated.getEndTime());

    builder.setClauses.add("updated_at = CURRENT_TIMESTAMP");

    if (!Objects.equals(previous.getName(), updated.getName())) {
      builder.setClauses.add(
          "name_tsvector = to_tsvector('simple', LOWER(REGEXP_REPLACE($1::varchar, '[-_.]', ' ', 'g')))");
    }

    builder.tuple.addString(projectKey);
    builder.tuple.addString(updated.getExperimentId().toString());

    return builder;
  }

  private void addIfChanged(String column, Object previous, Object updated) {
    if (!Objects.equals(previous, updated)) {
      setClauses.add(column + " = $" + paramIndex);
      tuple.addValue(updated);
      paramIndex++;
    }
  }

  private void addEnumIfChanged(String column, String enumType, String previous, String updated) {
    if (!Objects.equals(previous, updated)) {
      setClauses.add(column + " = $" + paramIndex + "::" + enumType);
      tuple.addString(updated);
      paramIndex++;
    }
  }

  private void addArrayIfChanged(String column, List<String> previous, List<String> updated) {
    if (!Objects.equals(previous, updated)) {
      setClauses.add(column + " = $" + paramIndex + "::varchar[]");
      tuple.addValue(updated != null && !updated.isEmpty() ? updated.toArray(new String[0]) : null);
      paramIndex++;
    }
  }

  private void addJsonIfChanged(String column, JsonObject previous, JsonObject updated) {
    if (!Objects.equals(previous, updated)) {
      setClauses.add(column + " = $" + paramIndex + "::jsonb");
      tuple.addJsonObject(updated);
      paramIndex++;
    }
  }

  private void addJsonArrayIfChanged(String column, JsonArray previous, JsonArray updated) {
    if (!Objects.equals(previous, updated)) {
      setClauses.add(column + " = $" + paramIndex + "::jsonb");
      tuple.addJsonArray(updated);
      paramIndex++;
    }
  }

  /**
   * Returns true if there are actual changes to update.
   *
   * @return true if there are field changes beyond just updated_at
   */
  public boolean hasChanges() {
    return setClauses.size() > 1
        || (setClauses.size() == 1 && !setClauses.get(0).startsWith("updated_at"));
  }

  /**
   * Builds the complete UPDATE SQL query.
   *
   * @return the SQL UPDATE query string, or null if no SET clauses
   */
  public String buildQuery() {
    if (setClauses.isEmpty()) {
      return null;
    }

    int whereParamStart = paramIndex;
    return "UPDATE experiment.experiments SET "
        + String.join(", ", setClauses)
        + " WHERE project_key = $"
        + whereParamStart
        + " AND experiment_id = $"
        + (whereParamStart + 1);
  }
}
