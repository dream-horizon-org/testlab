package com.ascend.testlab.rest;

import static com.ascend.testlab.constants.TestConstants.TEST_PROJECT_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.Constants;
import com.ascend.testlab.constants.TestConstants;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class GetExperimentIT {
  private Connection connection;

  @BeforeEach
  void setUp() throws SQLException {
    connection = TestUtil.getDatabaseConnection();
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  @Test
  void testGetExperimentSuccess() {
    String route = "/v1/experiment/" + TestConstants.TEST_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TestConstants.TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data", notNullValue())
        .body("data.experimentId", equalTo(TestConstants.TEST_EXPERIMENT_ID))
        .body("data.projectId", equalTo(TEST_PROJECT_ID));
  }

  @Test
  void testGetExperimentNotFound() {
    String route = "/v1/experiment/" + TestConstants.INVALID_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void testGetExperimentMissingProjectId() {
    String route = "/v1/experiment/" + TestConstants.TEST_EXPERIMENT_ID;

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetExperimentInvalidProjectId() {
    String route = "/v1/experiment/" + TestConstants.TEST_EXPERIMENT_ID;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, "invalid-project-id");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    // Should return not found since experiment doesn't exist for this project
    response.statusCode(HttpStatus.SC_NOT_FOUND);
  }
}
