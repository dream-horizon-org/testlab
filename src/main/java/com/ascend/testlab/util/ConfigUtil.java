package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.typesafe.config.*;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for loading config from config files.
 *
 * @author Nikhil Tummidi
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@UtilityClass
public final class ConfigUtil {

  /**
   * Get the application environment. If not set, the default environment (dev) is used.
   *
   * @return the application environment
   */
  private static String getAppEnvironment() {
    return System.getProperty(Constants.APP_ENV_KEY, Constants.DEFAULT_APP_ENV);
  }

  /**
   * Get the config from the config file.
   *
   * @param configFilePathFormat the format of the config file path
   * @return the config
   */
  public static Config getConfigFromConfigFile(@NonNull String configFilePathFormat) {
    ConfigFactory.invalidateCaches();
    String envFile = String.format(configFilePathFormat, getAppEnvironment());
    String defaultFile = String.format(configFilePathFormat, "default");
    Config config =
        ConfigFactory.load(envFile)
            .withFallback(
                ConfigFactory.load(
                    defaultFile,
                    ConfigParseOptions.defaults().setAllowMissing(true),
                    ConfigResolveOptions.defaults().setAllowUnresolved(true)))
            .resolve();
    log.debug("Loading config from file {} : {}", configFilePathFormat, config);
    return config;
  }

  /**
   * Get the typed config from the config file.
   *
   * @param configFilePathFormat the format of the config file path
   * @param clazz the class of the config
   * @param <T> the type of the config
   * @return the typed config
   */
  public static <T> T getTypedConfigFromConfigFile(
      @NonNull String configFilePathFormat, Class<T> clazz) {
    Config config = getConfigFromConfigFile(configFilePathFormat);
    T typedConfig = ConfigBeanFactory.create(config, clazz);
    log.debug("Loaded Config: {}", typedConfig);
    return typedConfig;
  }
}
