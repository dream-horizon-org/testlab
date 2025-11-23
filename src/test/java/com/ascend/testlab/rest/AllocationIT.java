package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for the Allocation API endpoints. Tests both POST /v1/allocation (allocate
 * experiments) and GET /v1/allocation (retrieve allocations) endpoints.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@ExtendWith(Setup.class)
class AllocationIT {

  private final String allocationRoute = "/v1/allocations";

  private static final String projectKey = "allocation_test_exp_it";

  @BeforeAll
  public static void initialize() {

    log.info("Starting tests for {}", AllocationIT.class.getSimpleName());
    TestUtil.createPartitionForProject("experiments", projectKey);
  }

  @AfterAll
  public static void cleanup() {

    log.info("Cleaning up {} resources", AllocationIT.class.getSimpleName());
    TestUtil.dropTestPartition("experiments", projectKey);
  }

  @Test
  void testAllocation_Success_SingleExperiment() {
    String experimentId = UUID.randomUUID().toString();
    String experimentKey = "test-experiment-11";

    seedExperiment(projectKey, experimentId, "Test Experiment 11", experimentKey);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-123");
    requestBody.put("stable_id", "guest-123");
    requestBody.put("experiment_keys", List.of(experimentKey));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    response.body("data.experiment_map.size()", Matchers.equalTo(1));
    response.body("data.experiment_map[0].experiment_id", Matchers.notNullValue());
    response.body("data.experiment_map[0].experiment_name", Matchers.notNullValue());
    response.body("data.experiment_map[0].variant", Matchers.notNullValue());
    response.body("data.experiment_map[0].assigned_at", Matchers.notNullValue());
  }

  @Test
  void testAllocation_Success_MultipleExperiments() {
    String experimentId1 = UUID.randomUUID().toString();
    String experimentId2 = UUID.randomUUID().toString();
    String experimentId3 = UUID.randomUUID().toString();
    String experimentKey1 = "test-experiment-1";
    String experimentKey2 = "test-experiment-2";
    String experimentKey3 = "test-experiment-3";

    seedExperiment(projectKey, experimentId1, "Test Experiment 1", experimentKey1);
    seedExperiment(projectKey, experimentId2, "Test Experiment 2", experimentKey2);
    seedExperiment(projectKey, experimentId3, "Test Experiment 3", experimentKey3);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-456");
    requestBody.put("stable_id", "guest-456");
    requestBody.put("experiment_keys", List.of(experimentKey1, experimentKey2, experimentKey3));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    response.body("data.experiment_map.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testAllocation_Missinguser_id_Success() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("stable_id", "guest-789");
    requestBody.put("experiment_keys", List.of("test-experiment-key"));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    // Should succeed with only stable_id (user_id is optional)
    response.statusCode(HttpStatus.SC_OK);
  }

  @Test
  void testAllocation_Missingstable_id_Success() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-789");
    requestBody.put("experiment_keys", List.of("test-experiment-key"));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    // Should succeed with only user_id (stable_id is optional)
    response.statusCode(HttpStatus.SC_OK);
  }

  @Test
  void testAllocation_Emptyexperiment_keysList_BadRequest() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-789");
    requestBody.put("stable_id", "guest-789");
    requestBody.put("experiment_keys", List.of());
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testAllocation_Blankuser_id_Success() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "   ");
    requestBody.put("stable_id", "guest-789");
    requestBody.put("experiment_keys", List.of("test-experiment-key"));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    // Should succeed with valid stable_id even if user_id is blank
    response.statusCode(HttpStatus.SC_OK);
  }

  @Test
  void testAllocation_Blankstable_id_Success() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-789");
    requestBody.put("stable_id", "   ");
    requestBody.put("experiment_keys", List.of("test-experiment-key"));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    // Should succeed with valid user_id even if stable_id is blank
    response.statusCode(HttpStatus.SC_OK);
  }

  @Test
  void testAllocation_MissingRequestBody_BadRequest() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.allocationRoute));

    // Expects 415 Unsupported Media Type when no body is sent to a JSON endpoint
    response.statusCode(415);
  }

  @Test
  void testAllocation_DefaultProjectKey_Success() {
    String experimentId = UUID.randomUUID().toString();
    String experimentKey = "test-experiment-default";

    seedExperiment(projectKey, experimentId, "Test Experiment Default", experimentKey);

    // No project key header - should use default
    Map<String, String> headers = new HashMap<>();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-default-123");
    requestBody.put("stable_id", "guest-default-123");
    requestBody.put("experiment_keys", List.of(experimentKey));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
  }

  @Test
  void testAllocation_DuplicateExperimentKeys_UsesUniqueKeys() {
    String experimentId = UUID.randomUUID().toString();
    String experimentKey = "test-experiment-duplicate";

    seedExperiment(projectKey, experimentId, "Test Experiment Duplicate", experimentKey);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    // Request with duplicate experiment keys - should only process unique keys
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("user_id", "user-duplicate-test");
    requestBody.put("stable_id", "guest-duplicate-test");
    requestBody.put("experiment_keys", List.of(experimentKey, experimentKey, experimentKey));
    requestBody.put("attributes", createDefaultAttributes());

    ValidatableResponse response =
        TestUtil.executeRequest(
            requestBody, headers, null, spec -> spec.post(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    // Should only have one experiment allocation despite duplicates in request
    response.body("data.experiment_map.size()", Matchers.equalTo(1));
  }

  // ===========================
  // GET /v1/allocation Tests
  // ===========================

  @Test
  void testGetAllocations_Success_WithExistingAllocations() {
    String user_id = "user-get-test-123";
    String experimentId = UUID.randomUUID().toString();
    String experimentKey = "test-experiment-get";

    seedExperiment(projectKey, experimentId, "Test Experiment Get", experimentKey);

    // First, allocate an experiment to the user
    Map<String, String> postHeaders = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> allocateRequestBody = new HashMap<>();
    allocateRequestBody.put("user_id", user_id);
    allocateRequestBody.put("stable_id", "guest-get-test-123");
    allocateRequestBody.put("experiment_keys", List.of(experimentKey));
    allocateRequestBody.put("attributes", createDefaultAttributes());

    TestUtil.executeRequest(
        allocateRequestBody, postHeaders, null, spec -> spec.post(this.allocationRoute));

    // Now retrieve the allocations
    Map<String, String> getHeaders = new HashMap<>();
    getHeaders.put(WebConstants.PROJECT_KEY_HEADER, projectKey);
    getHeaders.put(WebConstants.USER_ID_HEADER, user_id);

    ValidatableResponse response =
        TestUtil.executeRequest(null, getHeaders, null, spec -> spec.get(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    response.body("data.experiment_map.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testGetAllocations_Success_EmptyAllocations() {
    String user_id = "user-new-no-allocations";

    Map<String, String> headers = new HashMap<>();
    headers.put(WebConstants.PROJECT_KEY_HEADER, projectKey);
    headers.put(WebConstants.USER_ID_HEADER, user_id);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    response.body("data.experiment_map.size()", Matchers.equalTo(0));
  }

  @Test
  void testGetAllocations_Missinguser_idHeader_ReturnsEmptyList() {

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.allocationRoute));

    // user_id header is optional - returns 200 with empty list when not provided
    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
  }

  @Test
  void testGetAllocations_DefaultProjectKey_Success() {
    String user_id = "user-default-get-test";

    Map<String, String> headers = Map.of(WebConstants.USER_ID_HEADER, user_id);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.allocationRoute));

    // Should use default project key and return success (empty allocations)
    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
  }

  @Test
  void testGetAllocations_MultipleExperimentsAllocated() {
    String user_id = "user-multiple-exp-get";
    String experimentId1 = UUID.randomUUID().toString();
    String experimentId2 = UUID.randomUUID().toString();
    String experimentId3 = UUID.randomUUID().toString();
    String experimentKey1 = "test-experiment-multi-1";
    String experimentKey2 = "test-experiment-multi-2";
    String experimentKey3 = "test-experiment-multi-3";

    seedExperiment(projectKey, experimentId1, "Test Experiment Multi 1", experimentKey1);
    seedExperiment(projectKey, experimentId2, "Test Experiment Multi 2", experimentKey2);
    seedExperiment(projectKey, experimentId3, "Test Experiment Multi 3", experimentKey3);

    // First, allocate multiple experiments to the user
    Map<String, String> postHeaders = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> allocateRequestBody = new HashMap<>();
    allocateRequestBody.put("user_id", user_id);
    allocateRequestBody.put("stable_id", "guest-multiple-exp-get");
    allocateRequestBody.put(
        "experiment_keys", List.of(experimentKey1, experimentKey2, experimentKey3));
    allocateRequestBody.put("attributes", createDefaultAttributes());

    TestUtil.executeRequest(
        allocateRequestBody, postHeaders, null, spec -> spec.post(this.allocationRoute));

    // Now retrieve the allocations
    Map<String, String> getHeaders = new HashMap<>();
    getHeaders.put(WebConstants.PROJECT_KEY_HEADER, projectKey);
    getHeaders.put(WebConstants.USER_ID_HEADER, user_id);

    ValidatableResponse response =
        TestUtil.executeRequest(null, getHeaders, null, spec -> spec.get(this.allocationRoute));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiment_map", Matchers.notNullValue());
    response.body("data.experiment_map.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testGetAllocations_RetrieveSameAllocationsTwice() {
    String user_id = "user-consistent-allocations";
    String experimentId = UUID.randomUUID().toString();
    String experimentKey = "test-experiment-consistent";

    seedExperiment(projectKey, experimentId, "Test Experiment Consistent", experimentKey);

    // Allocate an experiment
    Map<String, String> postHeaders = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    Map<String, Object> allocateRequestBody = new HashMap<>();
    allocateRequestBody.put("user_id", user_id);
    allocateRequestBody.put("stable_id", "guest-consistent");
    allocateRequestBody.put("experiment_keys", List.of(experimentKey));
    allocateRequestBody.put("attributes", createDefaultAttributes());

    TestUtil.executeRequest(
        allocateRequestBody, postHeaders, null, spec -> spec.post(this.allocationRoute));

    // Retrieve allocations first time
    Map<String, String> getHeaders = new HashMap<>();
    getHeaders.put(WebConstants.PROJECT_KEY_HEADER, projectKey);
    getHeaders.put(WebConstants.USER_ID_HEADER, user_id);

    ValidatableResponse response1 =
        TestUtil.executeRequest(null, getHeaders, null, spec -> spec.get(this.allocationRoute));

    response1.statusCode(HttpStatus.SC_OK);
    String variant1 = response1.extract().path("data.experiment_map[0].variant_name");

    // Retrieve allocations second time - should be consistent
    ValidatableResponse response2 =
        TestUtil.executeRequest(null, getHeaders, null, spec -> spec.get(this.allocationRoute));

    response2.statusCode(HttpStatus.SC_OK);
    String variant2 = response2.extract().path("data.experiment_map[0].variant_name");

    // Variants should be the same across both calls
    assert variant1.equals(variant2) : "Variants should be consistent across multiple GET calls";
  }

  // ===========================
  // Helper Methods
  // ===========================

  /**
   * Seeds a test experiment with default values into the database.
   *
   * @param projectKey the project key
   * @param experimentId the experiment ID
   * @param experimentName the experiment name
   * @param experimentKey the unique experiment key
   */
  private void seedExperiment(
      String projectKey, String experimentId, String experimentName, String experimentKey) {
    String variantsJson =
        "'{\"control\": {\"displayName\": \"Control\", \"variantName\": \"control\", \"variables\": []}, "
            + "\"treatment\": {\"displayName\": \"Treatment\", \"variantName\": \"treatment\", \"variables\": []}}'";

    String variantWeightsJson = "'{\"weights\":{\"control\": 50, \"treatment\": 50}}'";

    String insert =
        String.format(
            "INSERT INTO experiment.experiments "
                + "(project_key, experiment_id, name, description, status, type, distribution_strategy, "
                + "assignment_domain, variants, variant_weights, cohorts, overrides, exposure, threshold, "
                + "start_time, end_time, experiment_key, created_by) "
                + "VALUES ('%s', '%s', '%s', 'Test experiment description', 'LIVE', 'A/B', 'RANDOM', "
                + "'COHORT', %s::jsonb, %s::jsonb, '{}'::varchar[], NULL, 100, 1000, "
                + "EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW() + INTERVAL '30 days') * 1000, "
                + "'%s', 'test-user');",
            projectKey,
            experimentId,
            experimentName,
            variantsJson,
            variantWeightsJson,
            experimentKey);

    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
      log.debug(
          "Seeded experiment {} with key {} for project {}",
          experimentId,
          experimentKey,
          projectKey);
    } catch (Exception e) {
      log.error("Failed to execute SQL: {}", insert, e);
      throw new RuntimeException(
          String.format("Failed seeding experiment %s for tests: %s", experimentId, e.getMessage()),
          e);
    }
  }

  /**
   * Creates default attributes for allocation requests.
   *
   * @return a map containing default attribute values
   */
  private Map<String, String> createDefaultAttributes() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("platform", "android");
    attributes.put("app_version", "1.0.0");
    attributes.put("os_version", "11.0");
    attributes.put("device", "pixel");
    return attributes;
  }
}
