package com.ascend.testlab.rest;

import static com.ascend.testlab.constants.TestConstants.TEST_PROJECT_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
  void testFilterExperimentsSuccess() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data", notNullValue())
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.pagination", notNullValue())
        .body("data.pagination.currentPage", notNullValue())
        .body("data.pagination.pageSize", notNullValue())
        .body("data.pagination.totalCount", greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperimentsByStatus() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.NAME, "Checkout");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].name", equalTo("Checkout Button Color Test"));
  }

  @Test
  void testFilterExperimentsByTag() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.TAG, "conversion");

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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.OWNER, "alice@example.com");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperimentsByType() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_TYPE, "A/B");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperimentsWithPagination() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
    Map<String, String> headers = new HashMap<>();
    headers.put(Constants.PROJECT_ID, TEST_PROJECT_ID);
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(Constants.EXPERIMENT_STATUS, "LIVE");
    queryParams.put(Constants.NAME, "checkout");
    queryParams.put(Constants.TAG, "conversion");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(route));

    response
        .statusCode(HttpStatus.SC_OK)
        .body("data.experimentList", notNullValue())
        .body("data.experimentList.size()", greaterThanOrEqualTo(1))
        .body("data.experimentList[0].status", equalTo("LIVE"))
        .body("data.experimentList[0].name", equalTo("Checkout Button Color Test"));
  }

  @Test
  void testFilterExperimentsMissingProjectId() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperimentsInvalidStatus() {
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
    String route = TestConstants.FILTER_EXPERIMENTS_PATH;
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
