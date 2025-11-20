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

  private static final String PROJECT_KEY = "history_it";
  private static final String EXPERIMENT_ID = "123e4567-e89b-12d3-a456-426614174000";
  private final String route = "/v1/experiments/{experimentId}/history";

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
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 1);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(EXPERIMENT_ID));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.history.size()", Matchers.equalTo(1));
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.totalCount", Matchers.equalTo(1));
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
      response.body("data.history[0].updatedBy", Matchers.notNullValue());
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Success_EmptyHistory() {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(EXPERIMENT_ID));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.history.size()", Matchers.equalTo(0));
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.totalCount", Matchers.equalTo(0));
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_MissingHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(this.route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperimentHistory_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, EXPERIMENT_ID));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testGetExperimentHistory_BlankExperimentId_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, "   "));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.EXPERIMENT_ID_MISSING));
  }

  @Test
  void testGetExperimentHistory_Pagination_DefaultValues() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 1);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.experimentId", Matchers.equalTo(EXPERIMENT_ID));
      response.body("data.history", Matchers.notNullValue());
      response.body("data.pagination", Matchers.notNullValue());
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(20));
      response.body("data.pagination.totalCount", Matchers.equalTo(1));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_CustomLimit() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 10);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "5");

      ValidatableResponse response =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(5));
      response.body("data.pagination.totalCount", Matchers.equalTo(10));
      response.body("data.history.size()", Matchers.equalTo(5));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_SecondPage_WithData() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 15);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "10", WebConstants.PAGE, "2");

      ValidatableResponse response =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(2));
      response.body("data.pagination.pageSize", Matchers.equalTo(10));
      response.body("data.pagination.totalCount", Matchers.equalTo(15));
      response.body("data.history.size()", Matchers.equalTo(5));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_SecondPage_Empty() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 1);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "10", WebConstants.PAGE, "2");

      ValidatableResponse response =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(2));
      response.body("data.pagination.pageSize", Matchers.equalTo(10));
      response.body(
          "data.pagination.totalCount", Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(1)));
      response.body("data.history.size()", Matchers.equalTo(0));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_MultiplePages_SameExperiment() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 25);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "5", WebConstants.PAGE, "1");

      ValidatableResponse responsePage1 =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      responsePage1.statusCode(HttpStatus.SC_OK);
      responsePage1.body("data.pagination.currentPage", Matchers.equalTo(1));
      responsePage1.body("data.pagination.pageSize", Matchers.equalTo(5));
      responsePage1.body("data.pagination.totalCount", Matchers.equalTo(25));
      responsePage1.body("data.history.size()", Matchers.equalTo(5));

      queryParams = Map.of(WebConstants.LIMIT, "5", WebConstants.PAGE, "2");
      ValidatableResponse responsePage2 =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      responsePage2.statusCode(HttpStatus.SC_OK);
      responsePage2.body("data.pagination.currentPage", Matchers.equalTo(2));
      responsePage2.body("data.pagination.pageSize", Matchers.equalTo(5));
      responsePage2.body("data.pagination.totalCount", Matchers.equalTo(25));
      responsePage2.body("data.history.size()", Matchers.equalTo(5));

      queryParams = Map.of(WebConstants.LIMIT, "5", WebConstants.PAGE, "5");
      ValidatableResponse responsePage5 =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      responsePage5.statusCode(HttpStatus.SC_OK);
      responsePage5.body("data.pagination.currentPage", Matchers.equalTo(5));
      responsePage5.body("data.pagination.pageSize", Matchers.equalTo(5));
      responsePage5.body("data.pagination.totalCount", Matchers.equalTo(25));
      responsePage5.body("data.history.size()", Matchers.equalTo(5));

      queryParams = Map.of(WebConstants.LIMIT, "5", WebConstants.PAGE, "6");
      ValidatableResponse responsePage6 =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      responsePage6.statusCode(HttpStatus.SC_OK);
      responsePage6.body("data.pagination.currentPage", Matchers.equalTo(6));
      responsePage6.body("data.pagination.pageSize", Matchers.equalTo(5));
      responsePage6.body(
          "data.pagination.totalCount", Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(25)));
      responsePage6.body("data.history.size()", Matchers.equalTo(0));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_Limit5_Page2() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 10);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "5", WebConstants.PAGE, "2");

      ValidatableResponse response =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(2));
      response.body("data.pagination.pageSize", Matchers.equalTo(5));
      response.body("data.pagination.totalCount", Matchers.equalTo(10));
      response.body("data.history.size()", Matchers.equalTo(5));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Ordering_LatestFirst() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedExperimentHistory(PROJECT_KEY, EXPERIMENT_ID, 5);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.history.size()", Matchers.equalTo(5));
      response.body("data.history[0].updatedBy", Matchers.equalTo("test-user-0"));
      response.body("data.history[1].updatedBy", Matchers.equalTo("test-user-1"));
      response.body("data.history[2].updatedBy", Matchers.equalTo("test-user-2"));
      response.body("data.history[3].updatedBy", Matchers.equalTo("test-user-3"));
      response.body("data.history[4].updatedBy", Matchers.equalTo("test-user-4"));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  @Test
  void testGetExperimentHistory_Pagination_MultipleExperiments() throws SQLException {
    try {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.createPartitionForProject("experiment_update_log", PROJECT_KEY);

      seedMultipleExperimentHistory(PROJECT_KEY, 5);

      Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
      Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "10", WebConstants.PAGE, "1");

      ValidatableResponse response =
          TestUtil.executeRequest(
              null, headers, queryParams, spec -> spec.get(this.route, EXPERIMENT_ID));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data.pagination.currentPage", Matchers.equalTo(1));
      response.body("data.pagination.pageSize", Matchers.equalTo(10));
      response.body("data.pagination.totalCount", Matchers.equalTo(3));
      response.body("data.history.size()", Matchers.equalTo(3));
    } finally {
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
    }
  }

  /**
   * Seeds multiple history entries for the same experiment with unique timestamps.
   *
   * @param projectKey the project key
   * @param experimentId the experiment ID
   * @param count the number of history entries to create
   */
  private void seedExperimentHistory(String projectKey, String experimentId, int count)
      throws SQLException {
    String partitionName = "experiment_update_log_p_" + projectKey;
    try (java.sql.Connection connection = TestUtil.getDatabaseConnection()) {
      for (int i = 0; i < count; i++) {
        String insert =
            String.format(
                "INSERT INTO experiment.%s "
                    + "(project_key, experiment_id, previous_data, current_data, updated_by, created_at, updated_at) "
                    + "VALUES ('%s', '%s', '{\"status\":\"DRAFT\"}', '{\"status\":\"LIVE\"}', 'test-user-%d', "
                    + "NOW() - INTERVAL '%d minutes', NOW() - INTERVAL '%d minutes');",
                partitionName, projectKey, experimentId, i, i, i);
        TestUtil.executeSQLStatement(connection, insert);
      }
    }
  }

  /**
   * Seeds history for multiple different experiments, each with multiple entries.
   *
   * @param projectKey the project key
   * @param experimentCount the number of different experiments to create
   */
  private void seedMultipleExperimentHistory(String projectKey, int experimentCount)
      throws SQLException {
    seedExperimentHistory(projectKey, EXPERIMENT_ID, 3);
    for (int i = 1; i < experimentCount; i++) {
      String experimentId = String.format("exp-%d", i);
      seedExperimentHistory(projectKey, experimentId, 2);
    }
  }
}
