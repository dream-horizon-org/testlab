package com.ascend.testlab.util;

import com.ascend.testlab.constants.TestConstants;
import com.ascend.testlab.constants.web.WebConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@UtilityClass
public final class TestUtil {

  public static void setAerospikeSystemProperties(GenericContainer<?> aerospikeContainer) {
    System.setProperty(TestConstants.AEROSPIKE_HOST_KEY, aerospikeContainer.getHost());
    System.setProperty(
        TestConstants.AEROSPIKE_PORT_KEY, String.valueOf(aerospikeContainer.getFirstMappedPort()));
    System.setProperty(
        TestConstants.AEROSPIKE_NAMESPACE_KEY,
        aerospikeContainer.getEnvMap().get(TestConstants.NAMESPACE));
  }

  public static void setPostgresSystemProperties(PostgreSQLContainer<?> postgreSQLContainer) {
    System.setProperty(TestConstants.POSTGRES_HOST_KEY, postgreSQLContainer.getHost());
    System.setProperty(
        TestConstants.POSTGRES_PORT_KEY, String.valueOf(postgreSQLContainer.getFirstMappedPort()));
    System.setProperty(TestConstants.POSTGRES_DATABASE_KEY, postgreSQLContainer.getDatabaseName());
    System.setProperty(TestConstants.POSTGRES_USER_KEY, postgreSQLContainer.getUsername());
    System.setProperty(TestConstants.POSTGRES_PASSWORD_KEY, postgreSQLContainer.getPassword());
  }

  public static void prepareDatabase() throws IOException, SQLException {
    Connection connection = getDatabaseConnection();
    executeSQLFile(connection, TestConstants.SCHEMA_FILE_PATH);
    log.info("Database schema created");
    executeSQLFile(connection, TestConstants.SEED_FILE_PATH);
    log.info("Database seed data inserted");
    connection.close();
  }

  public static Connection getDatabaseConnection() throws SQLException {
    String dbURL =
        String.format(
            "jdbc:postgresql://%s:%s/%s?currentSchema=public&searchpath=public,experiment",
            System.getProperty(TestConstants.POSTGRES_HOST_KEY),
            System.getProperty(TestConstants.POSTGRES_PORT_KEY),
            System.getProperty(TestConstants.POSTGRES_DATABASE_KEY));

    Connection connection =
        DriverManager.getConnection(
            dbURL,
            System.getProperty(TestConstants.POSTGRES_USER_KEY),
            System.getProperty(TestConstants.POSTGRES_PASSWORD_KEY));

    // Ensure search_path is set for this connection
    try (var stmt = connection.createStatement()) {
      stmt.execute("SET search_path TO public, experiment;");
    }

    return connection;
  }

  public static void executeSQLFile(Connection connection, String filePath)
      throws IOException, SQLException {
    String content = FileUtils.readFileToString(new File(filePath), Charset.defaultCharset());

    // Remove transaction-level commands that cannot run inside a transaction block
    content = content.replaceAll("(?i)DROP\\s+DATABASE[^;]*;", "");
    content = content.replaceAll("(?i)CREATE\\s+DATABASE[^;]*;", "");
    content = content.replaceAll("(?i)\\\\c\\s+\\w+;?", ""); // Remove \c commands

    executeSQLStatement(connection, content);
  }

  public static void executeSQLStatement(Connection connection, String query) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      connection.setAutoCommit(false);
      statement.executeUpdate(query);
      connection.commit();
    } catch (SQLException exception) {
      connection.rollback();
      throw exception;
    } finally {
      connection.setAutoCommit(true);
    }
  }

  public static ValidatableResponse executeRequest(
      Map<String, Object> requestBody,
      Map<String, String> headers,
      Map<String, String> queryParams,
      Function<RequestSpecification, Response> requestMapper) {
    RequestSpecification specification = RestAssured.given();
    if (Objects.nonNull(requestBody))
      specification
          .body(requestBody)
          .header(WebConstants.CONTENT_TYPE, WebConstants.APPLICATION_JSON);
    if (Objects.nonNull(headers)) specification.headers(headers);
    if (Objects.nonNull(queryParams)) specification.queryParams(queryParams);

    // TODO: verify that the API response follows the swagger specification

    return requestMapper.apply(specification).then();
  }

  /**
   * Create a partition for the given table and project key for test purposes.
   *
   * @param tableName the name of the table to partition
   * @param projectKey the project key
   */
  public static void createPartitionForProject(String tableName, String projectKey) {
    String ddl =
        String.format(
            "CREATE TABLE IF NOT EXISTS %s_p_test " + "PARTITION OF %s FOR VALUES IN ('%s');",
            tableName, tableName, projectKey);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), ddl);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Failed creating partition for tests on table '%s'", tableName), e);
    }
  }

  /**
   * Drop the partition for the given table for test cleanup.
   *
   * @param tableName the name of the table whose test partition should be dropped
   */
  public static void dropTestPartition(String tableName) {
    String drop = String.format("DROP TABLE IF EXISTS %s_p_test;", tableName);
    try {
      TestUtil.executeSQLStatement(TestUtil.getDatabaseConnection(), drop);
    } catch (Exception e) {
      // best-effort cleanup
      log.warn(String.format("Failed dropping test partition for table '%s'", tableName), e);
    }
  }
}
