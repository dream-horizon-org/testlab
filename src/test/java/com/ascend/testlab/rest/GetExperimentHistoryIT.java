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

  private final String testProjectKey = "test-project-key-history";
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
      TestUtil.dropTestPartition("experiment_update_log");
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
      response.body("data.totalCount", Matchers.greaterThanOrEqualTo(1));
      response.body("data.history[0].updatedBy", Matchers.notNullValue());
    } finally {
      TestUtil.dropTestPartition("experiment_update_log");
    }
  }

  @Test
  void testGetExperimentHistory_Success_EmptyHistory() {
    try {
      // Ensure partition exists for test project key
      TestUtil.dropTestPartition("experiment_update_log");
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
      response.body("data.totalCount", Matchers.equalTo(0));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log");
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

  private void seedExperimentHistory(String projectKey, String experimentId) throws SQLException {
    String insert =
        String.format(
            "INSERT INTO experiment.experiment_update_log_p_test "
                + "(project_key, experiment_id, previous_data, current_data, updated_by) "
                + "VALUES ('%s', '%s', '{\"status\":\"DRAFT\"}', '{\"status\":\"LIVE\"}', 'test-user');",
            projectKey, experimentId);
    TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
  }
}
