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

  /** The default project key to use when no project key is provided in the request headers. */
  private String projectKey;

  /** Configuration class for external service connection details. */
  @Data
  @NoArgsConstructor
  public static class ServiceConfig {
    /** The base URL of the external service. */
    private String serviceURL;

    /** The API endpoint path for the service. */
    private String apiEndPoint;

    /** The timeout in milliseconds for API calls to the service. */
    private int apiTimeoutMS;

    /** The retry count for API calls to the service. */
    @Optional private Integer retryCount = 3;

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
