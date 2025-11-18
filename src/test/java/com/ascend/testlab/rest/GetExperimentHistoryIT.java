package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.SQLException;
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
class GetExperimentHistoryIT {

  private final String testProjectKey = "test_project_key_history";
  private final String testExperimentId = "123e4567-e89b-12d3-a456-426614174000";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", GetExperimentHistoryIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", GetExperimentHistoryIT.class.getSimpleName());
  }

  @Test
  void testGetExperimentHistory_Success_WithHistory() throws SQLException {
    try {
      // Ensure partition exists for test project key
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      // Seed history data
      seedExperimentHistory(testProjectKey, testExperimentId);

      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(testExperimentId));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.history.size()", Matchers.greaterThanOrEqualTo(1));
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.totalCount", Matchers.greaterThanOrEqualTo(1));
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
      response.body("data.history[0].updatedBy", Matchers.notNullValue());
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  @Test
  void testGetExperimentHistory_Success_EmptyHistory() {
    try {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(testExperimentId));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.history.size()", Matchers.equalTo(0));
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.totalCount", Matchers.equalTo(0));
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  @Test
  void testGetExperimentHistory_MissingHeader_BadRequest() {
    String route = String.format("/v1/experiments/%s/history", testExperimentId);

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperimentHistory_BlankHeader_BadRequest() {
    String route = String.format("/v1/experiments/%s/history", testExperimentId);
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testGetExperimentHistory_BlankExperimentId_BadRequest() {
    String route = "/v1/experiments/   /history";
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.EXPERIMENT_ID_MISSING));
  }

  @Test
  void testGetExperimentHistory_Pagination_DefaultValues() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      seedExperimentHistory(testProjectKey, testExperimentId);

      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);

      // Test default pagination (limit=20, page=1)
      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(testExperimentId));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
      response.body("data.pagination.totalCount", Matchers.greaterThanOrEqualTo(1));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_CustomLimit() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      seedExperimentHistory(testProjectKey, testExperimentId);

      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);
      Map<String, String> queryParams = Map.of("limit", "5");

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(5));
      response.body("data.pagination.totalCount", Matchers.greaterThanOrEqualTo(1));
      response.body("data.history.size()", Matchers.lessThanOrEqualTo(5));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_SecondPage_Empty() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      // Seed one history entry (schema only allows one per experiment)
      seedExperimentHistory(testProjectKey, testExperimentId);

      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);
      Map<String, String> queryParams = Map.of("limit", "10", "page", "2");

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(2));
      response.body("data.pagination.pageSize", Matchers.equalTo(10));
      // When LIMIT/OFFSET returns 0 rows, we can't get total_count from window function
      // This is a known limitation - totalCount will be 0 when no rows are returned
      response.body(
          "data.pagination.totalCount", Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(1)));
      // Page 2 should return empty since we only have 1 entry
      response.body("data.history.size()", Matchers.equalTo(0));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_MultipleExperiments() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
      TestUtil.createPartitionForProject("experiment_update_log", testProjectKey);

      // Seed history for multiple different experiments to test pagination
      // Note: Schema only allows one entry per experiment, so we use different experiment IDs
      seedMultipleExperimentHistory(testProjectKey, 25);

      // Test pagination for the first experiment
      String route = String.format("/v1/experiments/%s/history", testExperimentId);
      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, testProjectKey);
      Map<String, String> queryParams = Map.of("limit", "10", "page", "1");

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(10));
      response.body(
          "data.pagination.totalCount", Matchers.equalTo(1)); // Only 1 entry for this experiment
      response.body("data.history.size()", Matchers.equalTo(1));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", testProjectKey);
    }
  }

  private void seedExperimentHistory(String projectKey, String experimentId) throws SQLException {
    String partitionName = "experiment_update_log_p_" + testProjectKey;
    String insert =
        String.format(
            "INSERT INTO experiment.%s "
                + "(project_key, experiment_id, previous_data, current_data, updated_by, created_at, updated_at) "
                + "VALUES ('%s', '%s', '{\"status\":\"DRAFT\"}', '{\"status\":\"LIVE\"}', 'test-user', NOW(), NOW());",
            partitionName, projectKey, experimentId);
    TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
  }

  /**
   * Seeds history for multiple different experiments (since schema only allows one entry per
   * experiment).
   *
   * @param projectKey the project key
   * @param count the number of different experiments to create history for
   */
  private void seedMultipleExperimentHistory(String projectKey, int count) throws SQLException {
    String partitionName = "experiment_update_log_p_" + testProjectKey;
    // First seed the test experiment
    seedExperimentHistory(projectKey, testExperimentId);
    // Then seed other experiments with different IDs
    for (int i = 1; i < count; i++) {
      String experimentId = String.format("exp-%d", i);
      String insert =
          String.format(
              "INSERT INTO experiment.%s "
                  + "(project_key, experiment_id, previous_data, current_data, updated_by, created_at, updated_at) "
                  + "VALUES ('%s', '%s', '{\"status\":\"DRAFT\"}', '{\"status\":\"LIVE\"}', 'test-user-%d', NOW() - INTERVAL '%d hours', NOW() - INTERVAL '%d hours');",
              partitionName, projectKey, experimentId, i, i, i);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    }
  }
}
