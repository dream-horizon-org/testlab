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
  private static final String PROJECT_ID = UUID.randomUUID().toString();
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
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(2));
    response.body("data.pagination", Matchers.notNullValue());
    response.body("data.pagination.currentPage", Matchers.notNullValue());
    response.body("data.pagination.pageSize", Matchers.notNullValue());
    response.body("data.pagination.totalCount", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByStatus() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "LIVE");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(1));
    response.body("data.experimentList[0].status", Matchers.equalTo("LIVE"));
    response.body("data.experimentList[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
  }

  @Test
  void testFilterExperiments_ByMultipleStatuses() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "LIVE,DRAFT");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByName() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.NAME, "Filter");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.greaterThanOrEqualTo(1));
    response.body("data.experimentList[0].name", Matchers.containsString("Filter Test Experiment"));
  }

  @Test
  void testFilterExperiments_ByTag() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.TAG, "test-tag-1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(1));
    response.body("data.experimentList[0].tags", Matchers.notNullValue());
    response.body("data.experimentList[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
  }

  @Test
  void testFilterExperiments_ByOwner() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.OWNER, "filter-test@example.com");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_ByType() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_TYPE, "A/B");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_WithPagination() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.LIMIT, "1", WebConstants.OFFSET, "1");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(1));
    response.body("data.pagination.pageSize", Matchers.equalTo(1));
    response.body("data.pagination.currentPage", Matchers.equalTo(1));
    response.body("data.pagination.totalCount", Matchers.equalTo(2));
  }

  @Test
  void testFilterExperiments_WithMultipleFilters() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
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
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(1));
    response.body("data.experimentList[0].status", Matchers.equalTo("LIVE"));
    response.body("data.experimentList[0].name", Matchers.equalTo("Filter Test Experiment 1"));
    response.body("data.experimentList[0].experimentId", Matchers.equalTo(EXPERIMENT_ID_1));
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
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
    Map<String, String> queryParams = Map.of(WebConstants.EXPERIMENT_STATUS, "INVALID_STATUS");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, queryParams, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testFilterExperiments_InvalidPagination_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_ID);
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
    response.body("data.experimentList", Matchers.notNullValue());
    response.body("data.experimentList.size()", Matchers.equalTo(0));
    response.body("data.pagination.totalCount", Matchers.equalTo(0));
  }

  /**
   * Seeds test data for filter experiments tests. This ensures all tests have the necessary data to
   * run successfully. The seed.sql already contains some data, but we add additional test data here
   * to ensure comprehensive test coverage.
   */
  private static void seedFilterTestData() {
    try {
      // Create partitions for the test project_key (required for partitioned tables)
      createPartitionsForProject(PROJECT_ID);

      // Seed additional experiments with various attributes for comprehensive filter testing
      // Note: seed.sql already has data, but we add more here to ensure all filter scenarios work
      seedExperiment(PROJECT_ID, EXPERIMENT_ID_1, "Filter Test Experiment 1", "LIVE", "A/B");
      seedTags(PROJECT_ID, EXPERIMENT_ID_1, "test-tag-1", "test-tag-2");
      seedOwners(PROJECT_ID, EXPERIMENT_ID_1, "filter-test@example.com");

      seedExperiment(PROJECT_ID, EXPERIMENT_ID_2, "Filter Test Experiment 2", "DRAFT", "A/B");
      seedTags(PROJECT_ID, EXPERIMENT_ID_2, "test-tag-2", "test-tag-3");
      seedOwners(
          PROJECT_ID, EXPERIMENT_ID_2, "filter-test@example.com", "another-owner@example.com");

      log.info("Seeded filter test data for project: {}", PROJECT_ID);
    } catch (Exception e) {
      // If data already exists (from seed.sql or previous runs), that's okay
      log.warn("Test data seeding error (may have skipped duplicates): {}", e.getMessage(), e);
    }
  }

  /**
   * Cleans up test-specific data after all tests complete. We only clean up data we added, not the
   * seed.sql data which is shared across test classes.
   */
  private static void cleanupTestData() {
    try {
      // Clean up only the test-specific data we added
      String deleteTags =
          String.format(
              "DELETE FROM experiment.tags WHERE project_key = '%s' AND experiment_id IN ('%s', '%s');",
              PROJECT_ID, EXPERIMENT_ID_1, EXPERIMENT_ID_2);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), deleteTags);

      String deleteOwners =
          String.format(
              "DELETE FROM experiment.owners WHERE project_key = '%s' AND experiment_id IN ('%s', '%s');",
              PROJECT_ID, EXPERIMENT_ID_1, EXPERIMENT_ID_2);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), deleteOwners);

      String deleteExperiments =
          String.format(
              "DELETE FROM experiments WHERE project_key = '%s' AND experiment_id IN ('%s', '%s');",
              PROJECT_ID, EXPERIMENT_ID_1, EXPERIMENT_ID_2);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), deleteExperiments);

      log.info("Cleanup completed for {}", FilterExperimentsIT.class.getSimpleName());
    } catch (Exception e) {
      log.warn("Failed to cleanup test data", e);
    }
  }

  /**
   * Creates partitions for all tables needed for the test project_key. This is required because the
   * tables are partitioned by project_key.
   *
   * @param projectKey the project key to create partitions for
   */
  private static void createPartitionsForProject(String projectKey) {
    try {
      // Create partition for experiments table
      String experimentsPartition =
          String.format(
              "CREATE TABLE IF NOT EXISTS experiments_p_test "
                  + "PARTITION OF experiments FOR VALUES IN ('%s');",
              projectKey);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), experimentsPartition);

      // Create partition for tags table
      TestUtil.createPartitionForProject("tags", projectKey);

      // Create partition for owners table
      TestUtil.createPartitionForProject("owners", projectKey);

      log.debug("Created partitions for project: {}", projectKey);
    } catch (Exception e) {
      log.warn("Failed to create partitions for project: {}", projectKey, e);
      throw new RuntimeException("Failed creating partitions for tests", e);
    }
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
            "INSERT INTO experiments ("
                + "project_key, experiment_id, name, description, hypothesis, status, type, "
                + "guardrail_health_status, cohorts, variant_weights, assignment_strategy, "
                + "overrides, rule_attributes, winning_variant, exposure, threshold, "
                + "start_time, end_time, created_by, created_at, updated_at, name_tsvector"
                + ") VALUES ("
                + "'%s', '%s', '%s', 'Test Description', 'Test Hypothesis', "
                + "'%s', '%s', 'PASSING', "
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
      // If insert fails, data might already exist - that's okay
      log.debug("Experiment may already exist: {}", experimentId);
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
      // Delete existing tags first to avoid duplicates
      String delete =
          String.format(
              "DELETE FROM experiment.tags WHERE project_key = '%s' AND experiment_id = '%s';",
              projectKey, experimentId);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), delete);

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
              values.toString());
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      log.debug("Tags may already exist for experiment: {}", experimentId);
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
      // Delete existing owners first to avoid duplicates
      String delete =
          String.format(
              "DELETE FROM experiment.owners WHERE project_key = '%s' AND experiment_id = '%s';",
              projectKey, experimentId);
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), delete);

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
              values.toString());
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      log.debug("Owners may already exist for experiment: {}", experimentId);
    }
  }
}
