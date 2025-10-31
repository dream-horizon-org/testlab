package com.ascend.testlab.rest;

import static org.hamcrest.Matchers.equalTo;
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
class GetExperimentIT {

  private static final String TEST_PROJECT_ID = UUID.randomUUID().toString();
  private static final String TEST_EXPERIMENT_ID = UUID.randomUUID().toString();
  private static final String TEST_EXPERIMENT_ID_2 = UUID.randomUUID().toString();
  private static final String INVALID_EXPERIMENT_ID = UUID.randomUUID().toString();
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
    // Note: Assuming the table uses UUID columns. If the actual schema uses project_id (VARCHAR),
    // you may need to adjust these queries accordingly.
    String insertExperiment =
        String.format(
            "INSERT INTO experiments (project_id, experiment_id, name, description, hypothesis, "
                + "status, type, guardrail_health_status, assignment_strategy, created_by, "
                + "created_at, updated_at) VALUES "
                + "('%s'::uuid, '%s'::uuid, 'Test Experiment', 'Test Description', 'Test Hypothesis', "
                + "'LIVE', 'A/B', 'PASSING', 'RANDOM', 'test-user', NOW(), NOW()), "
                + "('%s'::uuid, '%s'::uuid, 'Another Experiment', 'Another Description', 'Another Hypothesis', "
                + "'DRAFT', 'A/B', 'WARNING', 'ROUND_ROBIN', 'test-user', NOW(), NOW())",
            TEST_PROJECT_ID,
            TEST_EXPERIMENT_ID,
            TEST_PROJECT_ID,
            TEST_EXPERIMENT_ID_2);

    // Insert tags
    String insertTags =
        String.format(
            "INSERT INTO experiment.tags (project_id, experiment_id, tag, created_at, updated_at) VALUES "
                + "('%s', '%s', 'frontend', NOW(), NOW()), "
                + "('%s', '%s', 'mobile', NOW(), NOW())",
            TEST_PROJECT_ID, TEST_EXPERIMENT_ID, TEST_PROJECT_ID, TEST_EXPERIMENT_ID_2);

    // Insert owners
    String insertOwners =
        String.format(
            "INSERT INTO experiment.owners (project_id, experiment_id, owner, created_at, updated_at) VALUES "
                + "('%s', '%s', 'owner1', NOW(), NOW()), "
                + "('%s', '%s', 'owner2', NOW(), NOW())",
            TEST_PROJECT_ID, TEST_EXPERIMENT_ID, TEST_PROJECT_ID, TEST_EXPERIMENT_ID_2);

    TestUtil.executeSQLStatement(connection, insertExperiment);
    TestUtil.executeSQLStatement(connection, insertTags);
    TestUtil.executeSQLStatement(connection, insertOwners);
    log.info("Test data inserted successfully");
  }

  private void cleanupTestData() throws SQLException {
    try {
      String deleteTags =
          String.format(
              "DELETE FROM experiment.tags WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, TEST_EXPERIMENT_ID, TEST_EXPERIMENT_ID_2);
      String deleteOwners =
          String.format(
              "DELETE FROM experiment.owners WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, TEST_EXPERIMENT_ID, TEST_EXPERIMENT_ID_2);
      String deleteExperiments =
          String.format(
              "DELETE FROM experiments WHERE project_id = '%s'::uuid AND experiment_id IN ('%s'::uuid, '%s'::uuid)",
              TEST_PROJECT_ID, TEST_EXPERIMENT_ID, TEST_EXPERIMENT_ID_2);

      TestUtil.executeSQLStatement(connection, deleteTags);
      TestUtil.executeSQLStatement(connection, deleteOwners);
      TestUtil.executeSQLStatement(connection, deleteExperiments);
      log.info("Test data cleaned up successfully");
    } catch (SQLException e) {
      log.warn("Error cleaning up test data: {}", e.getMessage());
    }
  }

  @Test
  void testGetExperimentSuccess() {
    String route = "/v1/experiment/" + TEST_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data", notNullValue())
        .body("data.experimentId", equalTo(TEST_EXPERIMENT_ID))
        .body("data.projectId", equalTo(TEST_PROJECT_ID))
        .body("data.name", equalTo("Test Experiment"))
        .body("data.description", equalTo("Test Description"))
        .body("data.hypothesis", equalTo("Test Hypothesis"))
        .body("data.status", equalTo("LIVE"))
        .body("data.type", equalTo("A_B"))
        .body("data.guardrailHealthStatus", equalTo("PASSING"))
        .body("data.assignmentStrategy", equalTo("RANDOM"))
        .body("data.createdBy", equalTo("test-user"))
        .body("data.tags", notNullValue());
  }

  @Test
  void testGetExperimentNotFound() {
    String route = "/v1/experiment/" + INVALID_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testGetExperimentMissingProjectId() {
    String route = "/v1/experiment/" + TEST_EXPERIMENT_ID;

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperimentInvalidProjectId() {
    String route = "/v1/experiment/" + TEST_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, "invalid-project-id");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    // Should return not found since experiment doesn't exist for this project
    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }
}

