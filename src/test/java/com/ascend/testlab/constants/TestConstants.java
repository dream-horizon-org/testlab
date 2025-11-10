package com.ascend.testlab.constants;


public final class TestConstants {

  /** The {@code app.environment} value for the test environment. */
  public static final String TEST_APP_ENV = "test";

  /* Constants related to Aerospike Server */
  /** The {@code aerospike.host} system property key. */
  public static final String AEROSPIKE_HOST_KEY = "aerospike.host";

  /** The {@code aerospike.port} system property key. */
  public static final String AEROSPIKE_PORT_KEY = "aerospike.port";

  /** The {@code aerospike.image} system property key. */
  public static final String AEROSPIKE_IMAGE_KEY = "aerospike.image";

  /** The {@code aerospike.namespace} system property key. */
  public static final String AEROSPIKE_NAMESPACE_KEY = "aerospike.namespace";

  /** The NAMESPACE environment variable. */
  public static final String NAMESPACE = "NAMESPACE";

  /** The default aerospike image. */
  public static final String DEFAULT_AEROSPIKE_IMAGE = "aerospike/aerospike-server:8.1.0.1";

  /** The test aerospike namespace. */
  public static final String AEROSPIKE_NAMESPACE = "experiment";

  /* Constants related to PostgreSQL Server */
  /** The {@code postgres.host} system property key. */
  public static final String POSTGRES_HOST_KEY = "postgres.host";

  /** The {@code postgres.port} system property key. */
  public static final String POSTGRES_PORT_KEY = "postgres.port";

  /** The {@code postgres.image} system property key. */
  public static final String POSTGRES_IMAGE_KEY = "postgres.image";

  /** The {@code postgres.database} system property key. */
  public static final String POSTGRES_DATABASE_KEY = "postgres.database";

  /** The {@code postgres.user} system property key. */
  public static final String POSTGRES_USER_KEY = "postgres.user";

  /** The {@code postgres.password} system property key. */
  public static final String POSTGRES_PASSWORD_KEY = "postgres.password";

  /** The default postgres image. */
  public static final String DEFAULT_POSTGRES_IMAGE = "postgres:16";

  /** The test postgres database. */
  public static final String POSTGRES_DATABASE = "experiment";

  /** The test postgres user. */
  public static final String POSTGRES_USER = "test_user";

  /** The test postgres password. */
  public static final String POSTGRES_PASSWORD = "test_password";

  /* Constants related to DB Migrations */
  /** The schema file path. */
  public static final String SCHEMA_FILE_PATH = "src/main/resources/db/postgresql/schema.sql";

  /** The seed file path. */
  public static final String SEED_FILE_PATH = "src/main/resources/db/postgresql/seed.sql";

  /* Constants related to Application Server */
  /** The {@code http.server.host} system property key. */
  public static final String APPLICATION_HOST_KEY = "http.server.host";

  /** The {@code http.server.port} system property key. */
  public static final String APPLICATION_PORT_KEY = "http.server.port";

  private TestConstants() {}
}
