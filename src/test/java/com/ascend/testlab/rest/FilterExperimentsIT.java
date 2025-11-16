package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
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
class FilterExperimentsIT {
  private static final String PROJECT_KEY = "filter_exp_it";
  private static final String EXPERIMENT_ID_1 = "11111111-1111-1111-1111-111111111111";
  private static final String EXPERIMENT_ID_2 = "123e4567-e89b-12d3-a456-426614174000";
  private final String route = WebConstants.FILTER_EXPERIMENTS_PATH;

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", FilterExperimentsIT.class.getSimpleName());
    seedFilterTestData();
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", FilterExperimentsIT.class.getSimpleName());
    cleanupTestData();
  }

  @Test
  void testFilterExperiments_Success() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
    response.body("data.pagination", Matchers.notNullValue());
    response.body("data.pagination.currentPage", Matchers.notNullValue());
    response.body("data.pagination.pageSize", Matchers.notNullValue());
    response.body("data.pagination.totalCount", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByStatus() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "LIVE");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.experiments[0].status", Matchers.equalTo("LIVE"));
    response.body("data.experiments[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
  }

  @Test
  void testFilterExperiments_ByMultipleStatuses() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "LIVE,DRAFT");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByName() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.NAME, "Filter");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.greaterThanOrEqualTo(1));
    response.body("data.experiments[0].name", Matchers.containsString("Filter Test Experiment"));
  }

  @Test
  void testFilterExperiments_ByTag() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.TAG, "test-tag-1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.experiments[0].tags", Matchers.notNullValue());
    response.body("data.experiments[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
  }

  @Test
  void testFilterExperiments_ByOwner() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.OWNER, "filter-test@example.com");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByType() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_TYPE, "A/B");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_WithPagination() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "1", WebConstants.PAGE, "1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.pagination.pageSize", Matchers.equalTo(1));
    response.body("data.pagination.currentPage", Matchers.equalTo(1));
    response.body("data.pagination.totalCount", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_WithMultipleFilters() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams =
        Map.of(
            WebConstants.EXPERIMENT_STATUS,
            "LIVE",
            WebConstants.NAME,
            "Filter",
            WebConstants.TAG,
            "test-tag-1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.experiments[0].status", Matchers.equalTo("LIVE"));
    response.body("data.experiments[0].name", Matchers.equalTo("Filter Test Experiment 1"));
    response.body("data.experiments[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
  }

  @Test
  void testFilterExperiments_MissingHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperiments_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testFilterExperiments_InvalidStatus_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "INVALID_STATUS");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperiments_InvalidPagination_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "0");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperiments_NoResults() {
    Map<String, String> headers =
        Map.of(WebConstants.PROJECT_KEY_HEADER, UUID.randomUUID().toString());

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(0));
    response.body("data.pagination.totalCount", Matchers.equalTo(0));
  }

  @Test
  void testFilterExperiments_InvalidType_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_TYPE, "INVALID_TYPE");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.INVALID_EXPERIMENT_TYPE));
  }

  @Test
  void testFilterExperiments_NegativeLimit_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "-1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.INVALID_LIMIT_VALUE));
  }

  @Test
  void testFilterExperiments_NegativePage_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.PAGE, "-1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.INVALID_PAGE_VALUE));
  }

  @Test
  void testFilterExperiments_ZeroPage_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.PAGE, "0");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.INVALID_PAGE_VALUE));
  }

  @Test
  void testFilterExperiments_PageBeyondAvailable_EmptyResults() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.PAGE, "1000");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(0));
    response.body("data.pagination.currentPage", Matchers.equalTo(1000));
    response.body("data.pagination.totalCount", Matchers.equalTo(0));
    // totalCount is implementation specific, and for `count(*) over ()` it returns 0 when no rows
  }

  @Test
  void testFilterExperiments_LargeLimit() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "100");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
    response.body("data.pagination.pageSize", Matchers.equalTo(100));
  }

  @Test
  void testFilterExperiments_MultipleTypes() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_TYPE, "A/B,A/A");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.greaterThanOrEqualTo(2));
  }

  @Test
  void testFilterExperiments_MultipleTags() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.TAG, "test-tag-1,test-tag-2");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperiments_MultipleOwners() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams =
        Map.of(WebConstants.OWNER, "filter-test@example.com,another-owner@example.com");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperiments_NonExistentTag_NoResults() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.TAG, "non-existent-tag-12345");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(0));
  }

  @Test
  void testFilterExperiments_NonExistentOwner_NoResults() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.OWNER, "nonexistent@example.com");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(0));
  }

  @Test
  void testFilterExperiments_NameSearchPartialMatch() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.NAME, "Test");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.greaterThanOrEqualTo(1));
  }

  @Test
  void testFilterExperiments_EmptyName_ReturnsAll() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.NAME, "");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_CombineFiltersWithPagination() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams =
        Map.of(
            WebConstants.EXPERIMENT_TYPE, "A/B", WebConstants.LIMIT, "1", WebConstants.PAGE, "1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.pagination.pageSize", Matchers.equalTo(1));
    response.body("data.pagination.currentPage", Matchers.equalTo(1));
  }

  @Test
  void testFilterExperiments_SecondPage() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "1", WebConstants.PAGE, "2");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments.size()", Matchers.equalTo(1));
    response.body("data.pagination.currentPage", Matchers.equalTo(2));
    response.body("data.pagination.pageSize", Matchers.equalTo(1));
  }

  @Test
  void testFilterExperiments_DefaultPagination() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.pagination.currentPage", Matchers.equalTo(1));
    response.body("data.pagination.pageSize", Matchers.equalTo(20));
  }

  @Test
  void testFilterExperiments_InvalidStatusAndType_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams =
        Map.of(
            WebConstants.EXPERIMENT_STATUS,
            "INVALID_STATUS",
            WebConstants.EXPERIMENT_TYPE,
            "INVALID_TYPE");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperiments_MixedValidAndInvalidStatus_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "LIVE,INVALID");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.INVALID_EXPERIMENT_STATUS));
  }

  @Test
  void testFilterExperiments_StatusCaseInsensitive() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "live");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(1));
  }

  @Test
  void testFilterExperiments_CommonTagAcrossExperiments() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> queryParams = Map.of(WebConstants.TAG, "test-tag-2");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.body("data.experiments", Matchers.notNullValue());
    response.body("data.experiments.size()", Matchers.equalTo(2));
  }

  /**
   * Seeds test data for filter experiments tests. This ensures all tests have the necessary data to
   * run successfully. The seed.sql already contains some data, but we add additional test data here
   * to ensure comprehensive test coverage.
   */
  private static void seedFilterTestData() {
    // Create partitions for the test project_key (required for partitioned tables)
    // This must succeed before attempting to insert data
    TestUtil.createPartitionForProject("experiments", PROJECT_KEY);
    TestUtil.createPartitionForProject("tags", PROJECT_KEY);
    TestUtil.createPartitionForProject("owners", PROJECT_KEY);

    // Seed additional experiments with various attributes for comprehensive filter testing
    // Note: seed.sql already has data, but we add more here to ensure all filter scenarios work
    seedExperiment(PROJECT_KEY, EXPERIMENT_ID_1, "Filter Test Experiment 1", "LIVE", "A/B");
    seedTags(PROJECT_KEY, EXPERIMENT_ID_1, "test-tag-1", "test-tag-2");
    seedOwners(PROJECT_KEY, EXPERIMENT_ID_1, "filter-test@example.com");

    seedExperiment(PROJECT_KEY, EXPERIMENT_ID_2, "Filter Test Experiment 2", "DRAFT", "A/B");
    seedTags(PROJECT_KEY, EXPERIMENT_ID_2, "test-tag-2", "test-tag-3");
    seedOwners(
        PROJECT_KEY, EXPERIMENT_ID_2, "filter-test@example.com", "another-owner@example.com");

    log.info("Seeded filter test data for project: {}", PROJECT_KEY);
  }

  /**
   * Cleans up test-specific data after all tests complete. We only clean up data we added, not the
   * seed.sql data which is shared across test classes.
   */
  private static void cleanupTestData() {
    // Drop the partition we created
    try {
      TestUtil.dropTestPartition("experiments", PROJECT_KEY);
      TestUtil.dropTestPartition("tags", PROJECT_KEY);
      TestUtil.dropTestPartition("owners", PROJECT_KEY);
    } catch (Exception e) {
      log.debug("Failed to drop partition during cleanup (non-critical)", e);
    }
    log.info("Cleanup completed for {}", FilterExperimentsIT.class.getSimpleName());
  }

  /**
   * Seeds an experiment with the given attributes.
   *
   * @param projectKey the project key
   * @param experimentId the experiment ID
   * @param name the experiment name
   * @param status the experiment status (e.g., 'LIVE', 'DRAFT', 'PAUSED')
   * @param type the experiment type (e.g., 'A/B')
   */
  private static void seedExperiment(
      String projectKey, String experimentId, String name, String status, String type) {
    String insert =
        String.format(
            "INSERT INTO experiment.experiments ("
                + "project_key, experiment_id, name, description, hypothesis, status, type, "
                + "guardrail_health_status, cohorts, variant_weights, assignment_strategy, "
                + "overrides, rule_attributes, winning_variant, exposure, threshold, "
                + "start_time, end_time, created_by, created_at, updated_at, name_tsvector"
                + ") VALUES ("
                + "'%s', '%s', '%s', 'Test Description', 'Test Hypothesis', "
                + "'%s', '%s', 'PASSED', "
                + "ARRAY['all_users'], '{\"control\": 50, \"variant_a\": 50}'::jsonb, "
                + "'RANDOM', NULL::jsonb, NULL::jsonb, NULL::jsonb, "
                + "100, 1000, "
                + "EXTRACT(EPOCH FROM NOW() - INTERVAL '7 days')::bigint * 1000, "
                + "EXTRACT(EPOCH FROM NOW() + INTERVAL '23 days')::bigint * 1000, "
                + "'test@example.com', NOW(), NOW(), "
                + "to_tsvector('simple', '%s')"
                + ") ON CONFLICT (project_key, experiment_id) DO NOTHING;",
            projectKey, experimentId, name, status, type, name);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      // Log the error and rethrow - we need to know if data insertion fails
      log.error("Failed to seed experiment {}: {}", experimentId, e.getMessage(), e);
      throw new RuntimeException(
          String.format("Failed to seed experiment %s for project %s", experimentId, projectKey),
          e);
    }
  }

  /**
   * Seeds tags for an experiment.
   *
   * @param projectKey the project key
   * @param experimentId the experiment ID
   * @param tags array of tag names
   */
  private static void seedTags(String projectKey, String experimentId, String... tags) {
    if (tags.length == 0) {
      return;
    }
    try {
      StringBuilder values = new StringBuilder();
      for (int i = 0; i < tags.length; i++) {
        if (i > 0) {
          values.append(",");
        }
        values.append(
            String.format("('%s', '%s', '%s', NOW(), NOW())", experimentId, projectKey, tags[i]));
      }
      String insert =
          String.format(
              "INSERT INTO experiment.tags (experiment_id, project_key, tag, created_at, updated_at) VALUES %s;",
              values);

      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      log.error("Failed to seed tags for experiment {}: {}", experimentId, e.getMessage(), e);
      throw new RuntimeException(
          String.format("Failed to seed tags for experiment %s", experimentId), e);
    }
  }

  /**
   * Seeds owners for an experiment.
   *
   * @param projectKey the project key
   * @param experimentId the experiment ID
   * @param owners array of owner email addresses
   */
  private static void seedOwners(String projectKey, String experimentId, String... owners) {
    if (owners.length == 0) {
      return;
    }
    try {
      StringBuilder values = new StringBuilder();
      for (int i = 0; i < owners.length; i++) {
        if (i > 0) {
          values.append(",");
        }
        values.append(
            String.format("('%s', '%s', '%s', NOW(), NOW())", experimentId, projectKey, owners[i]));
      }
      String insert =
          String.format(
              "INSERT INTO experiment.owners (experiment_id, project_key, owner, created_at, updated_at) VALUES %s;",
              values);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      log.error("Failed to seed owners for experiment {}: {}", experimentId, e.getMessage(), e);
      throw new RuntimeException(
          String.format("Failed to seed owners for experiment %s", experimentId), e);
    }
  }
}
