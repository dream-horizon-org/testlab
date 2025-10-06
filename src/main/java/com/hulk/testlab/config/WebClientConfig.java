package com.hulk.testlab.config;

import com.hulk.testlab.config.provider.ConfigProvider;
import com.typesafe.config.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  @Optional private int connectTimeout = DEFAULT_CONNECTION_TIMEOUT;
  @Optional private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
  @Optional private boolean logActivity = DEFAULT_LOG_ACTIVITY;
  @Optional private boolean keepAlive = DEFAULT_KEEP_ALIVE;
  @Optional private int keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
  @Optional private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;
  @Optional private boolean pipelining = DEFAULT_PIPELINING;
  @Optional private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  public static ConfigProvider<WebClientConfig> provider() {
    return new ConfigProvider<>("webclient", WebClientConfig.class);
  }
}
