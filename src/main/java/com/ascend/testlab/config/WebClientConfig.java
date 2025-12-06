package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import com.typesafe.config.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the web client.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class WebClientConfig {
  private static final Integer DEFAULT_CONNECTION_TIMEOUT = 1000;
  private static final Integer DEFAULT_MAX_POOL_SIZE = 32;
  private static final Boolean DEFAULT_LOG_ACTIVITY = Boolean.FALSE;
  private static final Boolean DEFAULT_KEEP_ALIVE = Boolean.TRUE;
  private static final Integer DEFAULT_KEEP_ALIVE_TIMEOUT = 10;
  private static final Integer DEFAULT_MAX_WAIT_QUEUE_SIZE = 100;
  private static final Boolean DEFAULT_PIPELINING = Boolean.FALSE;
  private static final Integer DEFAULT_PIPELINING_LIMIT = 8;

  /** The connect timeout for the web client. */
  @Optional private int connectTimeout = DEFAULT_CONNECTION_TIMEOUT;

  /** The maximum pool size for the web client. */
  @Optional private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

  /** Whether log activity is enabled for the web client. */
  @Optional private boolean logActivity = DEFAULT_LOG_ACTIVITY;

  /** Whether keep alive is enabled for the web client. */
  @Optional private boolean keepAlive = DEFAULT_KEEP_ALIVE;

  /** The keep alive timeout for the web client. */
  @Optional private int keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;

  /** The maximum wait queue size for the web client. */
  @Optional private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;

  /** Whether pipelining is enabled for the web client. */
  @Optional private boolean pipelining = DEFAULT_PIPELINING;

  /** The pipelining limit for the web client. */
  @Optional private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  /**
   * Get the provider for the web client config.
   *
   * @return the provider for the web client config
   * @see ConfigProvider
   */
  public static ConfigProvider<WebClientConfig> provider() {
    return new ConfigProvider<>("webclient", WebClientConfig.class);
  }
}
