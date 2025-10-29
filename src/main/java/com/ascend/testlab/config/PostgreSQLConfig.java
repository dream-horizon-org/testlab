package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostgreSQLConfig {

  private BaseConfig readerConfig;
  private BaseConfig writerConfig;

  @Data
  @NoArgsConstructor
  public static class BaseConfig {
    private ConnectOptions connectOptions;
    private PoolOptions poolOptions;
    private Integer retryCount;
  }

  @Data
  @NoArgsConstructor
  public static class ConnectOptions {
    private String host;
    private Integer port;
    private String database;
    private String user;
    private String password;
    private Integer connectTimeout;
    private Boolean cachePreparedStatements;
  }

  @Data
  @NoArgsConstructor
  public static class PoolOptions {
    private Integer maxSize;
    private Integer maxWaitQueueSize;
  }

  public static ConfigProvider<PostgreSQLConfig> provider() {
    return new ConfigProvider<>("postgresql", PostgreSQLConfig.class);
  }
}
