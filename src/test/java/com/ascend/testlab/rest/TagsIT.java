package com.ascend.testlab.rest;

import com.ascend.testlab.Setup;
import com.ascend.testlab.constants.web.WebConstants;
import com.ascend.testlab.exception.ErrorMessages;
import com.ascend.testlab.util.TestUtil;
import io.restassured.response.ValidatableResponse;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(Setup.class)
class TagsIT {

  private final String route = "/v1/experiments/tags";

  @BeforeAll
  public static void initialize() {
    log.info("Starting tests for {}", TagsIT.class.getSimpleName());
  }

  @AfterAll
  public static void cleanup() {
    log.info("Cleaning up {} resources", TagsIT.class.getSimpleName());
  }

  @Test
  void testGetTags_Success() {
    Map<String, String> headers =
        Map.of(WebConstants.PROJECT_KEY_HEADER, "123e4567-e89b-12d3-a456-426614174000");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_OK);
    response.contentType(WebConstants.APPLICATION_JSON);
    response.body("data", Matchers.notNullValue());
    response.body("data.tags", Matchers.notNullValue());
    response.body("data.tags.size()", Matchers.greaterThanOrEqualTo(0));
  }

  @Test
  void testGetTags_Success_ResponseBody() {
    String projectKey = "123e4567-e89b-12d3-a456-426614174000";
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, projectKey);

    String[] expectedTags = new String[] {"ui-test", "feature-flag", "performance"};
    try {
      dropSeedPartition("tags", projectKey);
      TestUtil.createPartitionForProject("tags", projectKey);
      seedTags(projectKey, expectedTags);

      ValidatableResponse response =
          TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

      response.statusCode(HttpStatus.SC_OK);
      response.contentType(WebConstants.APPLICATION_JSON);
      response.body("data", Matchers.notNullValue());
      response.body("data.tags", Matchers.notNullValue());
      for (String tag : expectedTags) {
        response.body("data.tags", Matchers.hasItem(tag));
      }
    } finally {
      TestUtil.dropTestPartition("tags");
    }
  }

  @Test
  void testGetTags_MissingHeader_BadRequest() {
    ValidatableResponse response =
        TestUtil.executeRequest(null, null, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void testGetTags_BlankHeader_BadRequest() {
    Map<String, String> headers = Map.of(WebConstants.PROJECT_KEY_HEADER, "   ");

    ValidatableResponse response =
        TestUtil.executeRequest(null, headers, null, spec -> spec.get(this.route));

    response.statusCode(HttpStatus.SC_BAD_REQUEST);
    response.body(Matchers.containsString(ErrorMessages.PROJECT_KEY_MISSING));
  }

  private void seedTags(String projectKey, String[] tags) {
    String values =
        IntStream.range(0, tags.length)
            .mapToObj(i -> "('" + projectKey + "','exp-" + (i + 1) + "','" + tags[i] + "')")
            .collect(Collectors.joining(","));
    String insert =
        "INSERT INTO experiment.tags (project_key, experiment_id, tag) VALUES " + values + ";";
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), insert);
    } catch (Exception e) {
      throw new RuntimeException("Failed seeding tags for tests", e);
    }
  }

  private void dropSeedPartition(String tableName, String projectKey) {
    String partitionName = tableName + "_" + projectKey.replace("-", "");
    String drop = String.format("DROP TABLE IF EXISTS experiment.%s;", partitionName);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), drop);
    } catch (Exception e) {
      log.warn("Failed dropping seed partition for table {}", tableName, e);
    }
  }
}
