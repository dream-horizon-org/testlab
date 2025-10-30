package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the PostgreSQL client.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class PostgreSQLConfig {

  /** The reader configuration. */
  private BaseConfig readerConfig;

  /** The writer configuration. */
  private BaseConfig writerConfig;

  /** The base configuration for the PostgreSQL client. */
  @Data
  @NoArgsConstructor
  public static class BaseConfig {

    /** The connect options for the PostgreSQL client. */
    private ConnectOptions connectOptions;

    /** The pool options for the PostgreSQL client. */
    private PoolOptions poolOptions;

    /** The retry count for the PostgreSQL client. */
    private Integer retryCount;
  }

  /** The connect options for the PostgreSQL client. */
  @Data
  @NoArgsConstructor
  public static class ConnectOptions {

    /** The host for the PostgreSQL client. */
    private String host;

    /** The port for the PostgreSQL client. */
    private Integer port;

    /** The user for the PostgreSQL client. */
    private String user;

    /** The password for the PostgreSQL client. */
    private String password;

    /** The database for the PostgreSQL client. */
    private String database;

    /** The connect timeout for the PostgreSQL client. */
    private Integer connectTimeout;

    /** Whether to cache prepared statements for the PostgreSQL client. */
    private Boolean cachePreparedStatements;
  }

  /** The pool options for the PostgreSQL client. */
  @Data
  @NoArgsConstructor
  public static class PoolOptions {

    /** The maximum size of the pool. */
    private Integer maxSize;

    /** The maximum wait queue size. */
    private Integer maxWaitQueueSize;
  }

  /**
   * Get the provider for the PostgreSQL config.
   *
   * @return the provider for the PostgreSQL config
   * @see ConfigProvider
   */
  public static ConfigProvider<PostgreSQLConfig> provider() {
    return new ConfigProvider<>("postgresql", PostgreSQLConfig.class);
  }
}
