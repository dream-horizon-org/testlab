package com.hulk.testlab.config;

import com.hulk.testlab.config.provider.ConfigProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MySQLConfig {

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
    private String user;
    private String password;
    private String database;
    private Integer connectTimeout;
    private Boolean useAffectedRows;
    private Boolean cachePreparedStatements;
  }

  @Data
  @NoArgsConstructor
  public static class PoolOptions {
    private Integer maxSize;
    private Integer maxWaitQueueSize;
  }

  public static ConfigProvider<MySQLConfig> provider() {
    return new ConfigProvider<>("mysql", MySQLConfig.class);
  }
}
