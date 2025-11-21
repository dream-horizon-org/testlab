package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class ExperimentKeyAvailabilityIT {

  private static final String PROJECT_KEY = "key_availability_it";
  private static final String PROJECT_KEY_2 = "key_availability_it_2";
  private static final String EXPERIMENT_KEY = "existing_experiment_key";
  private final String route = "/v1/experiments/key-availability";

  @BeforeAll
  public static void initialize() throws SQLException {
    log.info("Starting tests for {}", ExperimentKeyAvailabilityIT.class.getSimpleName());

    // Setup partition for PROJECT_KEY
    TestUtil.dropTestPartition("experiments", PROJECT_KEY);
    TestUtil.createPartitionForProject("experiments", PROJECT_KEY);

    // Setup partition and seed data for PROJECT_KEY_2
    TestUtil.dropTestPartition("experiments", PROJECT_KEY_2);
    TestUtil.createPartitionForProject("experiments", PROJECT_KEY_2);
    seedExperiment(PROJECT_KEY_2, EXPERIMENT_KEY);
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", ExperimentKeyAvailabilityIT.class.getSimpleName());
    TestUtil.dropTestPartition("experiments", PROJECT_KEY);
    TestUtil.dropTestPartition("experiments", PROJECT_KEY_2);
  }

  @Test
  void testExperimentKeyAvailability_Success_Available() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, "new_experiment_key");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.isAvailable", Matchers.equalTo(true));
  }

  @Test
  void testExperimentKeyAvailability_Success_NotAvailable() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY_2);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, EXPERIMENT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.isAvailable", Matchers.equalTo(false));
  }

  @Test
  void testExperimentKeyAvailability_MissingHeader_BadRequest() {
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, "test_key");

    ValidatableResponse response =
        TestUtil.executeRequest(null, null, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testExperimentKeyAvailability_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, "test_key");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testExperimentKeyAvailability_MissingQueryParam_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.EXPERIMENT_KEY_MISSING));
  }

  @Test
  void testExperimentKeyAvailability_BlankQueryParam_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.EXPERIMENT_KEY_MISSING));
  }

  @Test
  void testExperimentKeyAvailability_KeyTooLong_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    String longKey = "a".repeat(256);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_KEY, longKey);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  /**
   * Seeds an experiment record in the database for testing purposes.
   *
   * @param projectKey the project key
   * @param experimentKey the experiment key (also used as name for test simplicity)
   * @throws SQLException if database operation fails
   */
  private static void seedExperiment(String projectKey, String experimentKey) throws SQLException {
    String experimentId = UUID.randomUUID().toString();
    String partitionName = "experiments_p_" + projectKey;
    try (Connection connection = TestUtil.getDatabaseConnection()) {
      String insert =
          String.format(
              "INSERT INTO experiment.%s "
                  + "(project_key, experiment_id, name, experiment_key, status) "
                  + "VALUES ('%s', '%s', '%s', '%s', 'DRAFT');",
              partitionName, projectKey, experimentId, experimentKey, experimentKey);
      TestUtil.executeSQLStatement(connection, insert);
    }
  }
}
