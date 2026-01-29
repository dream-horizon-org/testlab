package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.CommonUtil;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.Connection;
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
  private static final String PROJECT_KEY = "get_exp_it";
  private static final String EXPERIMENT_ID = "11111111-1111-1111-1111-111111111111";
  private static final String INVALID_EXPERIMENT_ID = "00000000-0000-0000-0000-000000000000";
  private final String route = "/v1/experiments/{experiment_id}";

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
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    try {
      createPartitionForProject();
      seedExperiment();

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experiment_id", Matchers.equalTo(EXPERIMENT_ID));
      response.body("data.project_key", Matchers.equalTo(PROJECT_KEY));
    } finally {
      TestUtil.dropTestPartition("experiments", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperiment_NotFound() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(
            null, headers, null, spec -> spec.get(route, INVALID_EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testGetExperiment_MissingHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperiment_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testGetExperiment_InvalidProjectKey_NotFound() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "invalid-project-id");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route, EXPERIMENT_ID));

    // Should return not found since experiment doesn't exist for this project
    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testGetExperiment_BlankExperimentId_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route, "   "));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.EXPERIMENT_ID_MISSING));
  }

  private void createPartitionForProject() {
    try {
      TestUtil.createPartitionForProject("experiments", PROJECT_KEY);
    } catch (Exception e) {
      throw new RuntimeException("Failed creating partition for tests on table 'experiments'", e);
    }
  }

  private void seedExperiment() {
    String experimentName = "Test Experiment";
    String experimentKey = CommonUtil.normalizeExperimentKey(experimentName);
    String insert =
        """
           INSERT INTO experiment.experiments (
                project_key, experiment_id, name, experiment_key, description, hypothesis, status, type,
                guardrail_health_status, cohorts, variant_weights, distribution_strategy,
                assignment_domain, overrides, rule_attributes, winning_variant, exposure, threshold,
                start_time, end_time, created_by, created_at, updated_at
            ) VALUES (
                '%s', '%s', '%s', '%s', 'Test Description', 'Test Hypothesis', 'LIVE', 'A/B',
                'NO_CHECKS_AVAILABLE', ARRAY['all_users'], '{"weights": {"control": 0.5, "variant_a": 0.5}}'::jsonb, 'RANDOM',
                'COHORT', NULL, NULL::jsonb, NULL::jsonb, 100, 1000,
                EXTRACT(EPOCH FROM NOW() - INTERVAL '7 days')::bigint * 1000,
                EXTRACT(EPOCH FROM NOW() + INTERVAL '23 days')::bigint * 1000,
                'test@example.com', NOW(), NOW()
            );
           """
            .formatted(PROJECT_KEY, EXPERIMENT_ID, experimentName, experimentKey);
    try (Connection connection = TestUtil.getDatabaseConnection()) {
      TestUtil.executeSQLStatement(connection, insert);
    } catch (Exception e) {
      throw new RuntimeException("Failed seeding experiment for tests", e);
    }
  }
}
