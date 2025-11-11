package com.ascend.testlab.config;

import com.ascend.testlab.config.provider.ConfigProvider;
import com.typesafe.config.Optional;
import io.vertx.core.http.HttpClientOptions;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the application config properties.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class ApplicationConfig {

  /** The cohort service URL for fetching user cohorts. */
  private ServiceConfig cohortsConfig;

  @Data
  @NoArgsConstructor
  public static class ServiceConfig {
    private String serviceURL;
    private String apiEndPoint;
    private int apiTimeoutMS;
    @Optional private String token;
    @Optional private Integer port = HttpClientOptions.DEFAULT_DEFAULT_PORT;
  }

  /**
   * Get the provider for the application config.
   *
   * @return the provider for the application config
   * @see ConfigProvider
   */
  public static ConfigProvider<ApplicationConfig> provider() {
    return new ConfigProvider<>("application", ApplicationConfig.class);
  }
}
