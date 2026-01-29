package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.enums.PartitionStatus;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.sql.Connection;
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
class PartitionIT {

  private static final String PROJECT_KEY = "partition_it";
  private static final String PROJECT_KEY_2 = "partition_it_2";
  private final String route = "/v1/partitions";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", PartitionIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", PartitionIT.class.getSimpleName());
    // Cleanup partitions and metadata
    try {
      TestUtil.dropTestPartition("experiments", PROJECT_KEY);
      TestUtil.dropTestPartition("owners", PROJECT_KEY);
      TestUtil.dropTestPartition("tags", PROJECT_KEY);
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY);
      TestUtil.dropTestPartition("experiment_analysis", PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
      TestUtil.dropTestPartition("experiments", PROJECT_KEY_2);
      TestUtil.dropTestPartition("owners", PROJECT_KEY_2);
      TestUtil.dropTestPartition("tags", PROJECT_KEY_2);
      TestUtil.dropTestPartition("experiment_update_log", PROJECT_KEY_2);
      TestUtil.dropTestPartition("experiment_analysis", PROJECT_KEY_2);
      TestUtil.deletePartitionMetadata(PROJECT_KEY_2);
    } catch (Exception e) {
      log.warn("Failed to cleanup partitions", e);
    }
  }

  @Test
  void testCreatePartition_Success() {
    // Clean up any existing partitions/metadata before test
    cleanupPartitions(PROJECT_KEY);
    TestUtil.deletePartitionMetadata(PROJECT_KEY);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    try {
      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.project_key", Matchers.equalTo(PROJECT_KEY));
      response.body("data.status", Matchers.equalTo(PartitionStatus.SUCCESS.name()));
      response.body("data.message", Matchers.equalTo("Partitions created successfully"));
    } finally {
      cleanupPartitions(PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
    }
  }

  @Test
  void testCreatePartition_Idempotent() {
    // Clean up any existing partitions/metadata before test
    cleanupPartitions(PROJECT_KEY);
    TestUtil.deletePartitionMetadata(PROJECT_KEY);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    try {
      // First call - should create partitions
      ValidatableResponse response1 =
          TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

      response1.statusCode(HttpStatus.SC_OK);
      response1.body("data.message", Matchers.equalTo("Partitions created successfully"));

      // Second call - should be idempotent
      ValidatableResponse response2 =
          TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

      response2.statusCode(HttpStatus.SC_OK);
      response2.body("data.project_key", Matchers.equalTo(PROJECT_KEY));
      response2.body("data.status", Matchers.equalTo(PartitionStatus.SUCCESS.name()));
      response2.body("data.message", Matchers.equalTo("Partitions already exist"));
    } finally {
      cleanupPartitions(PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
    }
  }

  @Test
  void testCreatePartition_MissingProjectKeyHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.post(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testCreatePartition_BlankProjectKeyHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  @Test
  void testCreatePartition_WithUserIdHeader() {
    // Clean up any existing partitions/metadata before test
    cleanupPartitions(PROJECT_KEY);
    TestUtil.deletePartitionMetadata(PROJECT_KEY);

    Map<String, String> headers =
        Map.of(
            WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY, WebConstants.USER_ID_HEADER, "test-user");

    try {
      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

      response.statusCode(HttpStatus.SC_OK);
      response.body("data.project_key", Matchers.equalTo(PROJECT_KEY));
      response.body("data.status", Matchers.equalTo(PartitionStatus.SUCCESS.name()));
    } finally {
      cleanupPartitions(PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
    }
  }

  @Test
  void testCreatePartition_DifferentProjectKeys() {
    // Clean up any existing partitions/metadata before test
    cleanupPartitions(PROJECT_KEY);
    TestUtil.deletePartitionMetadata(PROJECT_KEY);
    cleanupPartitions(PROJECT_KEY_2);
    TestUtil.deletePartitionMetadata(PROJECT_KEY_2);

    Map<String, String> headers1 = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);
    Map<String, String> headers2 = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY_2);

    try {
      // Create partition for first project
      ValidatableResponse response1 =
          TestUtil.executeRequest(null, headers1, null, spec -> spec.post(this.route));

      response1.statusCode(HttpStatus.SC_OK);
      response1.body("data.project_key", Matchers.equalTo(PROJECT_KEY));

      // Create partition for second project
      ValidatableResponse response2 =
          TestUtil.executeRequest(null, headers2, null, spec -> spec.post(this.route));

      response2.statusCode(HttpStatus.SC_OK);
      response2.body("data.project_key", Matchers.equalTo(PROJECT_KEY_2));
    } finally {
      cleanupPartitions(PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
      cleanupPartitions(PROJECT_KEY_2);
      TestUtil.deletePartitionMetadata(PROJECT_KEY_2);
    }
  }

  @Test
  void testCreatePartition_VerifyAllTablesPartitioned() {
    // Clean up any existing partitions/metadata before test
    cleanupPartitions(PROJECT_KEY);
    TestUtil.deletePartitionMetadata(PROJECT_KEY);

    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, PROJECT_KEY);

    try {
      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.post(this.route));

      response.statusCode(HttpStatus.SC_OK);
      response.body("data.status", Matchers.equalTo(PartitionStatus.SUCCESS.name()));

      // Verify partitions exist in database
      verifyPartitionExists("experiments", PROJECT_KEY);
      verifyPartitionExists("owners", PROJECT_KEY);
      verifyPartitionExists("tags", PROJECT_KEY);
      verifyPartitionExists("experiment_update_log", PROJECT_KEY);
      verifyPartitionExists("experiment_analysis", PROJECT_KEY);
    } finally {
      cleanupPartitions(PROJECT_KEY);
      TestUtil.deletePartitionMetadata(PROJECT_KEY);
    }
  }

  private void cleanupPartitions(String projectKey) {
    String[] tables = {
      "experiments", "owners", "tags", "experiment_update_log", "experiment_analysis"
    };
    for (String table : tables) {
      try {
        TestUtil.dropTestPartition(table, projectKey);
      } catch (Exception e) {
        log.warn("Failed to drop partition for table {} and project {}", table, projectKey, e);
      }
    }
    try {
      TestUtil.deletePartitionMetadata(projectKey);
    } catch (Exception e) {
      log.warn("Failed to delete partition metadata for project {}", projectKey, e);
    }
  }

  private void verifyPartitionExists(String tableName, String projectKey) {
    String suffix = projectKey.replace("-", "_");
    String partitionName = tableName + "_" + suffix;
    String query =
        String.format(
            "SELECT EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'experiment' AND tablename = '%s') as exists;",
            partitionName);
    try (Connection connection = TestUtil.getDatabaseConnection();
        java.sql.Statement stmt = connection.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(query)) {
      if (rs.next()) {
        boolean exists = rs.getBoolean("exists");
        if (!exists) {
          throw new AssertionError(
              String.format(
                  "Partition %s does not exist for table %s and project %s",
                  partitionName, tableName, projectKey));
        }
      } else {
        throw new AssertionError("Query returned no results");
      }
    } catch (AssertionError e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to verify partition existence for table " + tableName, e);
    }
  }
}
