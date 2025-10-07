package com.ascend.testlab.util;

import com.ascend.testlab.constants.Constants;
import com.typesafe.config.*;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class ConfigUtil {

  private static String getAppEnvironment() {
    return System.getProperty(Constants.APP_ENV_KEY, Constants.DEFAULT_APP_ENV);
  }

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

  public static <T> T getTypedConfigFromConfigFile(
      @NonNull String configFilePathFormat, Class<T> clazz) {
    Config config = getConfigFromConfigFile(configFilePathFormat);
    T typedConfig = ConfigBeanFactory.create(config, clazz);
    log.debug("Loaded Config: {}", typedConfig);
    return typedConfig;
  }
}
