package com.ascend.testlab.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class FilterExperimentsIT {

  private static final String TEST_PROJECT_ID = UUID.randomUUID().toString();
  private static final String EXP1_ID = UUID.randomUUID().toString();
  private static final String EXP2_ID = UUID.randomUUID().toString();
  private static final String EXP3_ID = UUID.randomUUID().toString();
  private static final String EXP4_ID = UUID.randomUUID().toString();
  private Connection connection;

  @BeforeEach
  void setUp() throws SQLException {
    connection = TestUtil.getDatabaseConnection();
    insertTestData();
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      cleanupTestData();
      connection.close();
    }
  }

  private void insertTestData() throws SQLException {
    // Insert multiple experiments with different statuses, types, names, tags, and owners
    String insertExperiments =
        String.format(
            "INSERT INTO experiments (project_id, experiment_id, name, description, hypothesis, "
                + "status, type, guardrail_health_status, assignment_strategy, created_by, "
                + "created_at, updated_at) VALUES "
                + "('%s'::uuid, '%s'::uuid, 'Frontend Experiment', 'Frontend test', 'Test Hypothesis 1', "
                + "'LIVE', 'A/B', 'PASSING', 'RANDOM', 'user1', NOW(), NOW()), "
                + "('%s'::uuid, '%s'::uuid, 'Backend Experiment', 'Backend test', 'Test Hypothesis 2', "
                + "'DRAFT', 'A/B', 'WARNING', 'ROUND_ROBIN', 'user2', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'), "
                + "('%s'::uuid, '%s'::uuid, 'Mobile App Test', 'Mobile test', 'Test Hypothesis 3', "
                + "'PAUSED', 'A/B', 'NO_CHECKS_AVAILABLE', 'RANDOM', 'user1', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'), "
                + "('%s'::uuid, '%s'::uuid, 'API Performance Test', 'API test', 'Test Hypothesis 4', "
                + "'CONCLUDED', 'A/B', 'FAILED', 'ROUND_ROBIN', 'user3', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours')",
            TEST_PROJECT_ID, EXP1_ID,
            TEST_PROJECT_ID, EXP2_ID,
            TEST_PROJECT_ID, EXP3_ID,
            TEST_PROJECT_ID, EXP4_ID);

    // Insert tags
    String insertTags =
        String.format(
            "INSERT INTO experiment.tags (project_id, experiment_id, tag, created_at, updated_at) VALUES "
                + "('%s', '%s', 'frontend', NOW(), NOW()), "
                + "('%s', '%s', 'backend', NOW(), NOW()), "
                + "('%s', '%s', 'mobile', NOW(), NOW()), "
                + "('%s', '%s', 'api', NOW(), NOW()), "
                + "('%s', '%s', 'performance', NOW(), NOW())",
            TEST_PROJECT_ID, EXP1_ID,  // frontend
            TEST_PROJECT_ID, EXP2_ID,  // backend
            TEST_PROJECT_ID, EXP3_ID,  // mobile
            TEST_PROJECT_ID, EXP4_ID,  // api
            TEST_PROJECT_ID, EXP4_ID); // performance (for EXP4)

    // Insert owners
    String insertOwners =
        String.format(
            "INSERT INTO experiment.owners (project_id, experiment_id, owner, created_at, updated_at) VALUES "
                + "('%s', '%s', 'owner1', NOW(), NOW()), "
                + "('%s', '%s', 'owner2', NOW(), NOW()), "
                + "('%s', '%s', 'owner1', NOW(), NOW()), "
                + "('%s', '%s', 'owner3', NOW(), NOW())",
            TEST_PROJECT_ID, EXP1_ID,  // owner1
            TEST_PROJECT_ID, EXP2_ID,  // owner2
            TEST_PROJECT_ID, EXP3_ID,  // owner1
            TEST_PROJECT_ID, EXP4_ID); // owner3

    TestUtil.executeSQLStatement(connection, insertExperiments);
    TestUtil.executeSQLStatement(connection, insertTags);
    TestUtil.executeSQLStatement(connection, insertOwners);
    log.info("Test data inserted successfully for FilterExperimentsIT");
  }

  private void cleanupTestData() throws SQLException {
    try {
      String deleteTags =
          String.format(
              "DELETE FROM experiment.tags WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid, '%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, EXP1_ID, EXP2_ID, EXP3_ID, EXP4_ID);
      String deleteOwners =
          String.format(
              "DELETE FROM experiment.owners WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid, '%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, EXP1_ID, EXP2_ID, EXP3_ID, EXP4_ID);
      String deleteExperiments =
          String.format(
              "DELETE FROM experiments WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid, '%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, EXP1_ID, EXP2_ID, EXP3_ID, EXP4_ID);

      TestUtil.executeSQLStatement(connection, deleteTags);
      TestUtil.executeSQLStatement(connection, deleteOwners);
      TestUtil.executeSQLStatement(connection, deleteExperiments);
      log.info("Test data cleaned up successfully for FilterExperimentsIT");
    } catch (SQLException e) {
      log.warn("Error cleaning up test data: {}", e.getMessage());
    }
  }

  @Test
  void testFilterExperimentsSuccess() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data", notNullValue())
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(4))
        .body("data.pagination", notNullValue())
        .body("data.pagination.currentPage", notNullValue())
        .body("data.pagination.pageSize", notNullValue())
        .body("data.pagination.totalCount", greaterThanOrEqualTo(4));
  }

  @Test
  void testFilterExperimentsByStatus() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_STATUS, "LIVE");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].status", equalTo("LIVE"));
  }

  @Test
  void testFilterExperimentsByMultipleStatuses() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_STATUS, "LIVE,DRAFT");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(2));
  }

  @Test
  void testFilterExperimentsByName() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.NAME, "Frontend");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].name", equalTo("Frontend Experiment"));
  }

  @Test
  void testFilterExperimentsByTag() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.TAG, "frontend");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].tags", notNullValue());
  }

  @Test
  void testFilterExperimentsByOwner() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.OWNER, "owner1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(2));
  }

  @Test
  void testFilterExperimentsByType() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_TYPE, "A/B");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(4));
  }

  @Test
  void testFilterExperimentsWithPagination() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.LIMIT, "2");
    queryParams.put(Constants.OFFSET, "1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", lessThanOrEqualTo(2))
        .body("data.pagination.pageSize", equalTo(2))
        .body("data.pagination.currentPage", equalTo(1));
  }

  @Test
  void testFilterExperimentsWithMultipleFilters() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_STATUS, "LIVE");
    queryParams.put(Constants.NAME, "Frontend");
    queryParams.put(Constants.TAG, "frontend");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].status", equalTo("LIVE"))
        .body("data.experimentList[0].name", equalTo("Frontend Experiment"));
  }

  @Test
  void testFilterExperimentsMissingProjectId() {
    String route = "/v1/experiment";

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperimentsInvalidStatus() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_STATUS, "INVALID_STATUS");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperimentsInvalidPagination() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.LIMIT, "0");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperimentsNoResults() {
    String route = "/v1/experiment";
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, UUID.randomUUID().toString()); // Different project ID

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", equalTo(0))
        .body("data.pagination.totalCount", equalTo(0));
  }
}

