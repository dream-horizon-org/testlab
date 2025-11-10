package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class GetExperimentIT {
  private static final String PROJECT_ID = "123e4567-e89b-12d3-a456-426614174000";
  private static final String EXPERIMENT_ID = "11111111-1111-1111-1111-111111111111";
  private static final String INVALID_EXPERIMENT_ID = "00000000-0000-0000-0000-000000000000";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", GetExperimentIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", GetExperimentIT.class.getSimpleName());
  }

  @Test
  void testGetExperiment_Success_WithSeededData() {
    String route = "/v1/experiments/" + EXPERIMENT_ID;
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);

    try {
      createPartitionForProject(PROJECT_ID);
      seedExperiment(PROJECT_ID, EXPERIMENT_ID);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(EXPERIMENT_ID));
      response.body("data.projectId", Matchers.equalTo(PROJECT_ID));
    } finally {
      TestUtil.dropTestPartition("experiments");
    }
  }

  @Test
  void testGetExperiment_NotFound() {
    String route = "/v1/experiments/" + INVALID_EXPERIMENT_ID;
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testGetExperiment_MissingHeader_BadRequest() {
    String route = "/v1/experiments/" + EXPERIMENT_ID;

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperiment_BlankHeader_BadRequest() {
    String route = "/v1/experiments/" + EXPERIMENT_ID;
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testGetExperiment_InvalidProjectId_NotFound() {
    String route = "/v1/experiments/" + EXPERIMENT_ID;
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "invalid-project-id");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    // Should return not found since experiment doesn't exist for this project
    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  private void createPartitionForProject(String projectKey) {
    String ddl =
        String.format(
            "CREATE TABLE IF NOT EXISTS experiments_p_test "
                + "PARTITION OF experiments FOR VALUES IN ('%s');",
            projectKey);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), ddl);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Failed creating partition for tests on table 'experiments'"), e);
    }
  }

  private void dropTestPartition(String projectKey) {
    try {
      // Delete test data first
      String delete =
          String.format("DELETE FROM experiments WHERE project_key = '%s';", projectKey);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), delete);

      // Drop the partition
      String ddl = "DROP TABLE IF EXISTS experiments_p_test;";
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), ddl);
    } catch (Exception e) {
      // best-effort cleanup
      log.warn("Failed dropping test partition for table experiments", e);
    }
  }

  private void seedExperiment(String projectKey, String experimentId) {
    String insert =
        String.format(
            "INSERT INTO experiments ("
                + "project_key, experiment_id, name, description, hypothesis, status, type, "
                + "guardrail_health_status, cohorts, variant_weights, assignment_strategy, "
                + "overrides, rule_attributes, winning_variant, exposure, threshold, "
                + "start_time, end_time, created_by, created_at, updated_at, name_tsvector"
                + ") VALUES ("
                + "'%s', '%s', 'Test Experiment', 'Test Description', 'Test Hypothesis', "
                + "'LIVE', 'A/B', 'PASSING', "
                + "ARRAY['all_users'], '{\"control\": 50, \"variant_a\": 50}'::jsonb, "
                + "'RANDOM', NULL::jsonb, NULL::jsonb, NULL::jsonb, "
                + "100, 1000, "
                + "EXTRACT(EPOCH FROM NOW() - INTERVAL '7 days')::bigint * 1000, "
                + "EXTRACT(EPOCH FROM NOW() + INTERVAL '23 days')::bigint * 1000, "
                + "'test@example.com', NOW(), NOW(), "
                + "to_tsvector('simple', 'Test Experiment')"
                + ");",
            projectKey, experimentId);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      throw new RuntimeException("Failed seeding experiment for tests", e);
    }
  }
}
