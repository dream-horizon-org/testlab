package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the MySQL client.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class MySQLConfig {
  /** The reader configuration. */
  private BaseConfig readerConfig;

  /** The writer configuration. */
  private BaseConfig writerConfig;

  /** The base configuration for the MySQL client. */
  @Data
  @NoArgsConstructor
  public static class BaseConfig {
    /** The connect options for the MySQL client. */
    private ConnectOptions connectOptions;

    /** The pool options for the MySQL client. */
    private PoolOptions poolOptions;

    /** The retry count for the MySQL client. */
    private Integer retryCount;
  }

  /** The connect options for the MySQL client. */
  @Data
  @NoArgsConstructor
  public static class ConnectOptions {
    /** The host for the MySQL client. */
    private String host;

    /** The port for the MySQL client. */
    private Integer port;

    /** The user for the MySQL client. */
    private String user;

    /** The password for the MySQL client. */
    private String password;

    /** The database for the MySQL client. */
    private String database;

    /** The connect timeout for the MySQL client. */
    private Integer connectTimeout;

    /** Whether to use affected rows for the MySQL client. */
    private Boolean useAffectedRows;

    /** Whether to cache prepared statements for the MySQL client. */
    private Boolean cachePreparedStatements;
  }

  /** The pool options for the MySQL client. */
  @Data
  @NoArgsConstructor
  public static class PoolOptions {
    /** The maximum size of the pool. */
    private Integer maxSize;

    /** The maximum wait queue size. */
    private Integer maxWaitQueueSize;
  }

  /**
   * Get the provider for the MySQL config.
   *
   * @return the provider for the MySQL config
   * @see ConfigProvider
   */
  public static ConfigProvider<MySQLConfig> provider() {
    return new ConfigProvider<>("mysql", MySQLConfig.class);
  }
}
