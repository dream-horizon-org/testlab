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
class DeleteExperimentIT {
  private static final String PROJECT_KEY = "delete_exp_it";
  private static final String EXPERIMENT_ID = "22222222-2222-2222-2222-222222222222";
  private static final String INVALID_EXPERIMENT_ID = "00000000-0000-0000-0000-000000000000";
  private final String route = "/v1/experiments/{experiment_id}";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", DeleteExperimentIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", DeleteExperimentIT.class.getSimpleName());
  }

  @Test
  void testDeleteExperiment_Success_WithSeededData() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    try {
      createPartitionForProject();
      seedExperiment();
      seedExperimentUpdateLog();

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.delete(route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.success", Matchers.equalTo(true));
    } finally {
      TestUtil.dropTestPartition("experiments", PROJECT_KEY);
    }
  }

  @Test
  void testDeleteExperiment_NotFound() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(
            null, headers, null, spec -> spec.delete(route, INVALID_EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testDeleteExperiment_MissingHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.delete(route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testDeleteExperiment_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.delete(route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testDeleteExperiment_InvalidProjectKey_NotFound() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "invalid-project-id");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.delete(route, EXPERIMENT_ID));

    // Should return not found since experiment doesn't exist for this project
    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testDeleteExperiment_MissingExperimentId_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.delete(route, " "));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  private void createPartitionForProject() {
    try {
      TestUtil.createPartitionForProject("experiments", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);
    } catch (Exception e) {
      throw new RuntimeException("Failed creating partition for tests on table 'experiments'", e);
    }
  }

  private void seedExperiment() {
    String variantsJson =
        "'{\"control\": {\"display_name\": \"Control\", \"variant_name\": \"control\", \"variables\": []}, "
            + "\"variant1\": {\"display_name\": \"Treatment\", \"variant_name\": \"treatment\", \"variables\": []}}'";

    String variantWeightsJson = "'{\"weights\":{\"control\": 50, \"variant1\": 50}}'";

    String insert =
        String.format(
            "INSERT INTO experiment.experiments "
                + "(project_key, experiment_id, name, description, status, type, distribution_strategy, "
                + "assignment_domain, variants, variant_weights, cohorts, overrides, exposure, threshold, "
                + "start_time, end_time, experiment_key, created_by) "
                + "VALUES ('%s', '%s', 'test-exp', 'Test experiment description', 'LIVE', 'A/B', 'RANDOM', "
                + "'COHORT', %s::jsonb, %s::jsonb, '{}'::varchar[], NULL, 100, 1000, "
                + "EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW() + INTERVAL '30 days') * 1000, "
                + "'test-key', 'test-user');",
            PROJECT_KEY, EXPERIMENT_ID, variantsJson, variantWeightsJson);

    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      log.error("Failed to execute SQL: {}", insert, e);
      throw new RuntimeException(
          String.format(
              "Failed seeding experiment %s for tests: %s", EXPERIMENT_ID, e.getMessage()),
          e);
    }
  }

  private void seedExperimentUpdateLog() {
    String insert =
        """
            INSERT INTO experiment.experiment_update_log (
              project_key, experiment_id, previous_data, current_data
            ) VALUES (
              '%s', '%s', '{"control": 50, "variant_a": 50}'::jsonb, '{"control": 50, "variant_a": 50}'::jsonb
            );
            """
            .formatted(PROJECT_KEY, EXPERIMENT_ID);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      throw new RuntimeException("Failed seeding experiment update log for tests", e);
    }
  }
}
